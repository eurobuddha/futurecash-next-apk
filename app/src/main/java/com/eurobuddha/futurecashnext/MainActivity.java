package com.eurobuddha.futurecashnext;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import org.json.JSONArray;
import org.json.JSONObject;
import org.minimarex.minimaapi.MinimaAPI;
import org.minimarex.minimaapi.MinimaAPIMessages;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * The four tabs: Guardian (health + rescue setup + Harden), Stakes (lock and collect),
 * Activity (what the guardian did), Help.
 *
 * <p>All node and network work happens on a single worker thread — {@link Node} blocks, and the IPC
 * reply arrives on the main thread, so calling it from the UI thread would deadlock until timeout.
 */
public class MainActivity extends AppCompatActivity {

    /** The service stands down while this is visible so both don't post for the same coin. */
    public static volatile boolean FOREGROUND = false;

    private LinearLayout content, tabsBar;
    private TextView pairBanner;

    private NodeApi nodeApi;
    private Node node;
    private Cfg cfg;
    private Store store;
    private Guardian guardian;
    private AuditRunner auditRunner;
    private ExecutorService worker;
    private BroadcastReceiver receiver;

    private String tab = "guardian";
    private boolean paired = true;
    private KeyAudit.Result lastAudit;
    private long tip = 0;
    private List<Guardian.Stake> stakes = new ArrayList<>();
    private boolean auditRunning = false;

    @Override protected void onCreate(Bundle saved) {
        super.onCreate(saved);
        setContentView(R.layout.activity_main);
        content = findViewById(R.id.content);
        tabsBar = findViewById(R.id.tabs);
        pairBanner = findViewById(R.id.pairBanner);

        cfg = new Cfg(this);
        store = new Store(this);
        worker = Executors.newSingleThreadExecutor();
        nodeApi = new NodeApi(this, enabled -> { paired = enabled; runOnUiThread(this::renderPairBanner); });
        node = new Node(nodeApi);
        guardian = new Guardian(node, cfg, store,
                (t, b) -> { if (cfg.is(Cfg.NOTIFY, true)) Notifier.alert(this, t, b); });
        auditRunner = new AuditRunner(node, cfg, this);

        Notifier.ensureChannels(this);
        askNotificationPermission();
        buildTabs();
        render();

        receiver = new BroadcastReceiver() {
            @Override public void onReceive(Context c, Intent intent) {
                if (!MinimaAPI.checkMinimaID(MainActivity.this, intent)) return;
                final String data = intent.getStringExtra(MinimaAPIMessages.MINIMA_API_NOTIFY_DATA);
                if (data == null) return;
                try {
                    final String ev = new JSONObject(data).optString("event", "");
                    if ("NEWBLOCK".equals(ev) || "NEWBALANCE".equals(ev)) refresh();
                } catch (Exception ignored) {}
            }
        };
        ContextCompat.registerReceiver(this, receiver,
                new IntentFilter(MinimaAPIMessages.MINIMA_API_NOTIFY), ContextCompat.RECEIVER_EXPORTED);

        // Assert the covenant at every launch, so simply opening the app repairs a node whose script
        // table lost it — the failure that only shows at spend time.
        submit(() -> {
            guardian.wallet().registerContract();
            guardian.reloadAtRisk();
            refresh();
            maybeAudit(false);
        });
    }

    @Override protected void onResume() {
        super.onResume();
        FOREGROUND = true;
        GuardianService.UI_FOREGROUND = true;
        refresh();
    }

    @Override protected void onPause() {
        super.onPause();
        FOREGROUND = false;
        GuardianService.UI_FOREGROUND = false;
    }

    @Override protected void onDestroy() {
        super.onDestroy();
        if (receiver != null) { try { unregisterReceiver(receiver); } catch (Exception ignored) {} }
        if (auditRunner != null) auditRunner.onDestroy();
        if (worker != null) worker.shutdownNow();
        if (nodeApi != null) nodeApi.onDestroy();
        if (store != null) store.close();
    }

    private void submit(Runnable r) {
        final ExecutorService w = worker;
        if (w != null && !w.isShutdown()) { try { w.execute(r); } catch (Exception ignored) {} }
    }

    private void askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= 33
                && checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 1);
        }
    }

    /* ================= chrome ================= */

    private void buildTabs() {
        tabsBar.removeAllViews();
        final String[][] defs = {{"guardian", "Guardian"}, {"lock", "Stakes"},
                {"activity", "Activity"}, {"help", "Help"}};
        for (String[] d : defs) {
            final Button b = Ui.ghost(this, d[1], v -> { tab = d[0]; buildTabs(); render(); });
            if (tab.equals(d[0])) {
                b.setTextColor(Color.BLACK);
                final android.graphics.drawable.GradientDrawable bg =
                        new android.graphics.drawable.GradientDrawable();
                bg.setColor(Ui.ACCENT);
                bg.setCornerRadius(Ui.dp(this, 10));
                b.setBackground(bg);
            }
            tabsBar.addView(b);
        }
    }

    private void renderPairBanner() {
        if (paired) { pairBanner.setVisibility(View.GONE); return; }
        pairBanner.setVisibility(View.VISIBLE);
        pairBanner.setText("⚠ Not enabled in Minima Core. The guardian must act unattended, so open "
                + "Minima Core → Apps and enable Future Cash Next. Until then nothing can be collected "
                + "or rescued.");
    }

    private void toast(String s) { runOnUiThread(() -> Toast.makeText(this, s, Toast.LENGTH_LONG).show()); }

    /** Pull fresh state on the worker, then redraw. */
    private void refresh() {
        submit(() -> {
            final long t = guardian.tip();
            final List<Guardian.Stake> s = guardian.listStakes();
            runOnUiThread(() -> { tip = t; stakes = s; render(); });
        });
    }

    private void render() {
        renderPairBanner();
        content.removeAllViews();
        switch (tab) {
            case "lock": renderStakes(); break;
            case "activity": renderActivity(); break;
            case "help": renderHelp(); break;
            default: renderGuardian();
        }
    }

    /* ================= Guardian tab ================= */

    private void renderGuardian() {
        content.addView(auditCard());
        content.addView(daemonCard());
        content.addView(safeCard());
        content.addView(riskCard());
        recoverCard();
        hardenCard();
        content.addView(keyTableCard());
    }

    private View auditCard() {
        final LinearLayout c = Ui.card(this, 0);
        final long at = cfg.getLong(Cfg.AUDIT_AT);
        final String err = cfg.get(Cfg.AUDIT_LAST_ERR, "");
        final Audit.Verdict v = new Audit(cfg, node).usable();
        final String[] bad = new Audit(cfg, node).safeReused();

        String msg;
        int colour;
        if (auditRunning) {
            msg = "Auditing your keys…"; colour = Ui.DIM;
        } else if (bad != null) {
            // Loudest state in the app: the place we sweep RESCUED money to is itself reused. Ranked
            // above key-reuse risk because it breaks the escape route, not just the front door.
            msg = "⚠ Your safe destination " + Ui.shortAddr(bad[0]) + " is a REUSED address (signed ×"
                    + bad[1] + "). Rescuing there would move your money onto another exposed key — "
                    + "set a fresh destination below.";
            colour = Ui.DANGER;
        } else if (lastAudit != null && lastAudit.worstReuse > 0) {
            msg = "⚠ Key-reuse risk detected (reused ×" + lastAudit.worstReuse + "). At-risk stakes "
                    + "are collected and swept to your safe address.";
            colour = lastAudit.worstReuse > 3 ? Ui.DANGER : Ui.WARN;
        } else if (!err.isEmpty()) {
            // Report the failure we actually had. Wrapping every error in "couldn't reach the audit
            // service" told the user to check their network when the real problem was a local one
            // (not enabled in Minima Core), which is the opposite of the fix they needed.
            msg = err + " "
                    + (v.ok ? "Using your last good audit (" + Ui.timeAgo(v.at) + ") — collecting continues."
                            : "Auto-collect of safe stakes is paused until an audit succeeds; your funds "
                              + "are untouched and stay locked in the contract.");
            colour = v.ok ? Ui.WARN : Ui.DANGER;
        } else if (at == 0) {
            msg = "No audit yet. Tap Re-audit to check your keys."; colour = Ui.DIM;
        } else {
            msg = "✓ No key-reuse risk. Your matured stakes can be collected safely.";
            colour = Ui.OK;
        }

        c.addView(Ui.text(this, "Key audit", Ui.TEXT, 16, true));
        c.addView(Ui.text(this, msg, colour, 13, false));
        if (at > 0) {
            c.addView(Ui.text(this, "audited " + Ui.timeAgo(at) + " · " + v.mode.name().toLowerCase()
                    + (tip > 0 ? " · block " + tip : ""), Ui.DIM, 11, false));
        }
        final LinearLayout row = Ui.row(this);
        row.addView(Ui.ghost(this, "Re-audit", v2 -> maybeAudit(true)));
        c.addView(row);
        return c;
    }

    private View daemonCard() {
        final boolean on = cfg.is(Cfg.GUARDIAN_ON, false);
        final LinearLayout c = Ui.card(this, on ? Ui.OK : Ui.WARN);
        c.addView(Ui.text(this, "Guardian daemon", Ui.TEXT, 16, true));
        c.addView(Ui.text(this, on
                ? "Running. Your stakes are watched even with the app closed — matured ones are "
                  + "collected, and any landing on a reused key are swept to safety."
                : "Off. Stakes are only checked while this app is open, so an at-risk payout could sit "
                  + "on an exposed key until you next look.", on ? Ui.OK : Ui.WARN, 13, false));
        final LinearLayout row = Ui.row(this);
        if (on) {
            row.addView(Ui.ghost(this, "Stop guardian", v -> {
                cfg.setBool(Cfg.GUARDIAN_ON, false);
                GuardianService.stop(this);
                render();
            }));
        } else {
            row.addView(Ui.button(this, "Start guardian", Ui.OK, v -> {
                cfg.setBool(Cfg.GUARDIAN_ON, true);
                GuardianService.start(this);
                render();
            }));
        }
        c.addView(row);
        return c;
    }

    private View safeCard() {
        final Guardian.Status st = guardian.status(tip);
        final LinearLayout c = Ui.card(this, 0);
        c.addView(Ui.text(this, "Ready to collect — SAFE", Ui.TEXT, 16, true));

        final String detail = st.readySafeN > 0
                ? Ui.amount(String.valueOf(st.readySafeA)) + " MINIMA in " + (int) st.readySafeN
                  + " matured stake" + (st.readySafeN == 1 ? "" : "s") + " on clean addresses."
                : "No safe matured stakes right now.";
        c.addView(Ui.text(this, detail, Ui.DIM, 13, false));

        final boolean auto = cfg.is(Cfg.AUTO_COLLECT_SAFE, false);
        final LinearLayout row = Ui.row(this);
        if (auto) {
            c.addView(Ui.text(this, "Auto-collect is ON — matured stakes are collected to your own "
                    + "address. There's no rush: until then they stay safely locked away.", Ui.OK, 12, false));
            row.addView(Ui.ghost(this, "Turn off", v -> {
                cfg.setBool(Cfg.AUTO_COLLECT_SAFE, false); render();
            }));
        } else {
            row.addView(Ui.button(this, "Turn on auto-collect", Ui.OK, v -> {
                cfg.setBool(Cfg.AUTO_COLLECT_SAFE, true); render();
            }));
        }
        if (st.readySafeN > 0) row.addView(Ui.ghost(this, "Collect all now", v -> collectAllSafe()));
        c.addView(row);
        return c;
    }

    private View riskCard() {
        final Guardian.Status st = guardian.status(tip);
        final boolean anyRisk = st.atRiskKnown > 0 || st.readyRiskN > 0 || st.pendRiskN > 0;
        final LinearLayout c = Ui.card(this, st.readyRiskN > 0 ? Ui.WARN : 0);
        c.addView(Ui.text(this, "At-risk stakes", Ui.TEXT, 16, true));

        String detail;
        if (st.readyRiskN > 0) {
            detail = Ui.amount(String.valueOf(st.readyRiskA)) + " MINIMA in " + (int) st.readyRiskN
                    + " matured stake(s) paying out to REUSED addresses — rescuing to safety now.";
        } else if (st.pendRiskN > 0) {
            detail = Ui.amount(String.valueOf(st.pendRiskA)) + " MINIMA in " + (int) st.pendRiskN
                    + " at-risk stake(s) maturing (soonest "
                    + Ui.maturesIn((long) st.pendRiskSoonest, tip) + "). The guardian collects and "
                    + "sweeps each the moment it matures.";
        } else {
            detail = anyRisk ? "No at-risk stakes right now — the guardian is watching and acts the "
                    + "moment one matures." : "No at-risk stakes detected.";
        }
        c.addView(Ui.text(this, detail, Ui.DIM, 13, false));

        final String safe = cfg.get(Cfg.SAFE_ADDRESS, null);
        final boolean rescueOn = cfg.is(Cfg.ENABLED, false);
        if (safe != null) {
            c.addView(Ui.text(this, (rescueOn ? "rescue ON → " : "destination: ") + Ui.shortAddr(safe)
                    + (guardian.wallet().sameNodeMode() ? " · this node" : " · external"),
                    rescueOn ? Ui.OK : Ui.DIM, 12, false));
        }
        if (st.vaultLocked) {
            c.addView(Ui.text(this, "🔒 vault locked — unlock your node to sweep", Ui.WARN, 12, false));
        }

        final LinearLayout row = Ui.row(this);
        row.addView(Ui.button(this, safe == null ? "Set up rescue…" : "Change destination", Ui.ACCENT,
                v -> rescueDialog()));
        if (rescueOn) row.addView(Ui.ghost(this, "Stop rescue", v -> {
            cfg.setBool(Cfg.ENABLED, false);
            cfg.log(Cfg.LVL_INFO, "At-risk rescue DISABLED");
            render();
        }));
        c.addView(row);
        return c;
    }

    /** Rescue setup: a fresh clean address on this node, or an external wallet. */
    private void rescueDialog() {
        final LinearLayout box = Ui.column(this);
        box.setPadding(Ui.dp(this, 20), Ui.dp(this, 8), Ui.dp(this, 20), Ui.dp(this, 8));
        box.addView(Ui.text(this, "Where should rescued funds land?", Ui.TEXT, 15, true));
        box.addView(Ui.text(this, "Best if you only have one device: funds stay on this node at a "
                + "brand-new unused address.", Ui.DIM, 12, false));
        final EditText ext = Ui.field(this, "…or an Mx address from a DIFFERENT wallet", false);
        box.addView(ext);

        final AlertDialog dlg = new AlertDialog.Builder(this)
                .setTitle("Rescue at-risk stakes")
                .setView(box)
                .setNegativeButton("Cancel", null)
                .create();

        final LinearLayout actions = Ui.row(this);
        actions.addView(Ui.button(this, "Fresh address on THIS node", Ui.OK, v -> {
            dlg.dismiss();
            submit(() -> {
                final Wallet.Res r = guardian.wallet().setSafeSameNode();
                if (r.ok) { cfg.setBool(Cfg.ENABLED, true); cfg.log(Cfg.LVL_INFO, "At-risk rescue ENABLED"); }
                toast(r.ok ? "Rescue ON → " + Ui.shortAddr(r.value) : r.error);
                runOnUiThread(this::render);
            });
        }));
        actions.addView(Ui.ghost(this, "Use external", v -> {
            final String addr = ext.getText().toString().trim();
            dlg.dismiss();
            toast("Checking that address is clean…");
            submit(() -> auditRunner.checkOne(addr, count -> submit(() -> {
                // count: >0 reused (refuse), 0 clean, -1 unverifiable (accept, re-checked on every audit)
                final Wallet.Res r = guardian.wallet().setSafeExternal(addr, count);
                if (r.ok) { cfg.setBool(Cfg.ENABLED, true); cfg.log(Cfg.LVL_INFO, "At-risk rescue ENABLED"); }
                toast(r.ok
                        ? "Rescue ON → external " + Ui.shortAddr(addr)
                          + (count == 0 ? " (verified clean)" : " (reuse check unverified — will re-check)")
                        : r.error);
                runOnUiThread(this::render);
            })));
        }));
        box.addView(actions);
        dlg.show();
    }

    /** Coins stranded on retired addresses — shown only when there are any. */
    private void recoverCard() {
        submit(() -> {
            final List<String[]> stuck = guardian.wallet().retiredWithCoins();
            if (stuck.isEmpty()) return;
            int total = 0;
            for (String[] s : stuck) total += Integer.parseInt(s[1]);
            final int n = total;
            runOnUiThread(() -> {
                final LinearLayout c = Ui.card(this, Ui.WARN);
                c.addView(Ui.text(this, "⚠ " + n + " coin(s) stuck on retired addresses", Ui.WARN, 15, true));
                c.addView(Ui.text(this, "Retiring nulled those addresses' keys, so ordinary sends can't "
                        + "spend them. Recover moves them to your clean address using each address's "
                        + "real key.", Ui.DIM, 12, false));
                c.addView(Ui.button(this, "Recover " + n + " coin(s)", Ui.WARN, v -> submit(() -> {
                    final Wallet.SweepReport r = guardian.wallet().recoverRetiredCoins();
                    toast(r.swept > 0 ? "Recovered " + r.swept + " coin(s) to " + Ui.shortAddr(r.dest)
                            : (r.error != null ? r.error : "Nothing to recover."));
                    refresh();
                })));
                content.addView(c, Math.min(2, content.getChildCount()));
            });
        });
    }

    /** Harden: retire reused addresses so the node stops routing change to them. */
    private void hardenCard() {
        submit(() -> {
            final JSONArray flagged = cfg.atRisk();
            if (flagged.length() == 0) return;
            final List<Wallet.ReusedAddr> list = guardian.wallet().reusedDefaults(flagged);
            if (list.isEmpty()) return;
            runOnUiThread(() -> {
                final LinearLayout c = Ui.card(this, Ui.WARN);
                c.addView(Ui.text(this, "Harden — retire reused addresses", Ui.TEXT, 16, true));
                c.addView(Ui.text(this, "Your node routes change to a random pick of its default "
                        + "addresses, so a reused one keeps receiving fresh money. Retiring drops it "
                        + "from that rotation for good — kept watch-only, moves no funds.", Ui.DIM, 12, false));
                int active = 0;
                for (Wallet.ReusedAddr ra : list) {
                    if (!ra.isDefault) continue;
                    active++;
                    final LinearLayout r = Ui.row(this);
                    final LinearLayout info = Ui.column(this);
                    info.addView(Ui.text(this, Ui.shortAddr(ra.mx.isEmpty() ? ra.hex : ra.mx), Ui.TEXT, 12, false));
                    info.addView(Ui.text(this, ra.coins == 0 ? "empty · ready to retire"
                            : "holds " + ra.coins + " coin(s) — sweep them off first",
                            ra.coins == 0 ? Ui.DIM : Ui.WARN, 11, false));
                    info.setLayoutParams(new LinearLayout.LayoutParams(0,
                            LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
                    r.addView(info);
                    if (ra.coins == 0) {
                        r.addView(Ui.ghost(this, "Retire", v -> submit(() -> {
                            final Wallet.Res res = guardian.wallet().retireAddress(ra.hex);
                            toast(res.ok ? "Retired — change will never land there again." : res.error);
                            refresh();
                        })));
                    } else {
                        r.addView(Ui.button(this, "Sweep", Ui.OK, v -> submit(() -> {
                            final Wallet.SweepReport res = guardian.wallet().sweepAddressToClean(ra.hex);
                            toast(res.swept > 0 ? "Swept " + res.swept + " coin(s) → " + Ui.shortAddr(res.dest)
                                    : (res.error != null ? res.error : "Nothing swept."));
                            refresh();
                        })));
                    }
                    c.addView(r);
                }
                if (active == 0) {
                    c.addView(Ui.text(this, "✓ All your reused addresses are already out of the change "
                            + "rotation.", Ui.OK, 12, false));
                }
                c.addView(Ui.text(this, "Retiring survives restarts but NOT a reinstall-from-seed — "
                        + "re-run it after any reseed. Restart your node afterwards to top the change "
                        + "pool back up.", Ui.DIM, 11, false));
                content.addView(c);
            });
        });
    }

    private View keyTableCard() {
        final LinearLayout c = Ui.card(this, 0);
        c.addView(Ui.text(this, "Key detail", Ui.TEXT, 16, true));
        if (lastAudit == null) {
            c.addView(Ui.text(this, "Run an audit to see every key's on-chain usage.", Ui.DIM, 12, false));
            return c;
        }
        for (KeyAudit.Row r : lastAudit.rows) {
            if (!r.risk && !r.reused) continue;   // only the interesting ones; the rest are noise
            final LinearLayout row = Ui.row(this);
            row.addView(Ui.text(this, "#" + r.index + "  " + Ui.shortAddr(r.address), Ui.TEXT, 12, false));
            final TextView chip = Ui.text(this, r.reused ? "  RE-USED ×" + r.reuseCount : "  AT RISK",
                    r.reused ? Ui.DANGER : Ui.WARN, 12, true);
            row.addView(chip);
            c.addView(row);
        }
        c.addView(Ui.text(this, "Only your PUBLIC keys are ever sent to the audit service — they can't "
                + "be used to take your money.", Ui.DIM, 11, false));
        return c;
    }

    private void maybeAudit(boolean force) {
        if (auditRunning) return;
        final long at = cfg.getLong(Cfg.AUDIT_AT);
        // Refresh once the verdict is nearing expiry, not on every open: the gate needs a verdict
        // under 30 minutes old, so auditing on every launch was load the service never needed.
        if (!force && at > 0 && System.currentTimeMillis() - at < 20 * 60_000L) return;
        auditRunning = true;
        runOnUiThread(this::render);
        submit(() -> auditRunner.run(new AuditRunner.Cb() {
            @Override public void onDone(KeyAudit.Result result, long archiveTip) {
                auditRunning = false;
                lastAudit = result;
                guardian.reloadAtRisk();
                runOnUiThread(() -> render());
            }
            @Override public void onError(String message) {
                auditRunning = false;
                runOnUiThread(() -> render());
            }
        }));
    }

    /* ================= Stakes tab ================= */

    private void renderStakes() {
        final LinearLayout listCard = Ui.card(this, 0);
        listCard.addView(Ui.text(this, "Your stakes", Ui.TEXT, 16, true));
        if (stakes.isEmpty()) {
            listCard.addView(Ui.text(this, "No stakes yet. Lock one below and it appears here.",
                    Ui.DIM, 12, false));
        }
        for (Guardian.Stake s : stakes) {
            final LinearLayout row = Ui.row(this);
            final LinearLayout info = Ui.column(this);
            info.addView(Ui.text(this, Ui.amount(s.amount) + ("0x00".equals(s.tokenid) ? " MINIMA" : " token")
                    + (s.atRisk ? "  ⚠ reused payout" : ""), s.atRisk ? Ui.WARN : Ui.TEXT, 14, true));
            info.addView(Ui.text(this, s.ready ? "ready to collect" : "matures " + Ui.maturesIn(s.matureBlock, tip),
                    s.ready ? Ui.OK : Ui.DIM, 11, false));
            info.addView(Ui.text(this, "→ " + Ui.shortAddr(s.payout), Ui.DIM, 11, false));
            info.setLayoutParams(new LinearLayout.LayoutParams(0,
                    LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
            row.addView(info);
            // No bare-collect button for an at-risk payout: those belong to the rescue path.
            if (s.ready && !s.atRisk) {
                row.addView(Ui.ghost(this, "Collect", v -> submit(() -> {
                    final Wallet.Res r = guardian.collectNow(s.coinid);
                    toast(r.ok ? "Collect posted — confirms in about a block." : r.error);
                    refresh();
                })));
            }
            listCard.addView(row);
        }
        content.addView(listCard);
        content.addView(lockCard());
    }

    private View lockCard() {
        final LinearLayout c = Ui.card(this, 0);
        c.addView(Ui.text(this, "Lock a new payment", Ui.TEXT, 16, true));
        final EditText amount = Ui.field(this, "Amount (MINIMA)", true);
        c.addView(amount);

        final long[] blocks = {1728};   // ~1 day
        final TextView preview = Ui.text(this, "Unlocks in ~1 day", Ui.DIM, 12, false);
        final LinearLayout presets = Ui.row(this);
        final int[][] opts = {{60, 72}, {1440, 1728}, {10080, 12096}, {43200, 51840}};
        final String[] labels = {"+1 hour", "+1 day", "+1 week", "+1 month"};
        for (int i = 0; i < opts.length; i++) {
            final int idx = i;
            presets.addView(Ui.ghost(this, labels[i], v -> {
                blocks[0] = opts[idx][1];
                preview.setText("Unlocks in ~" + labels[idx].substring(1) + " · " + blocks[0] + " blocks");
            }));
        }
        c.addView(presets);
        c.addView(preview);

        final EditText recipient = Ui.field(this, "Recipient Mx… (leave blank to pay yourself)", false);
        c.addView(recipient);

        c.addView(Ui.button(this, "Lock it", Ui.ACCENT, v -> {
            final String amt = amount.getText().toString().trim();
            final String rcp = recipient.getText().toString().trim();
            submit(() -> {
                String payoutHex;
                if (rcp.isEmpty()) {
                    // Pay ourselves at a FRESH clean address, never a reused default.
                    final Wallet.Res clean = guardian.wallet().cleanAddress();
                    if (!clean.ok) { toast(clean.error); return; }
                    payoutHex = guardian.wallet().cleanAddressHex();
                } else if (Node.validHexAddr(rcp)) {
                    payoutHex = rcp;
                } else {
                    final JSONObject r = node.cmd("checkaddress address:" + rcp);
                    final JSONObject resp = Node.obj(r);
                    payoutHex = resp == null ? null : resp.optString("0x", resp.optString("address", ""));
                    if (payoutHex == null || !Node.validHexAddr(payoutHex)) {
                        toast("Couldn't resolve that recipient address."); return;
                    }
                }
                final Wallet.Res res = guardian.createFutureCash(amt, "0x00", blocks[0], payoutHex);
                toast(res.ok ? "✓ Locked " + amt + " MINIMA · matures block " + res.value : res.error);
                refresh();
            });
        }));
        c.addView(Ui.text(this, "Once locked, nobody — not even you — can unlock it early. Collected "
                + "coins can ONLY go to the payout address; that is enforced on-chain.", Ui.DIM, 11, false));
        return c;
    }

    private void collectAllSafe() {
        submit(() -> {
            int done = 0;
            String lastErr = null;
            for (Guardian.Stake s : guardian.listStakes()) {
                if (!s.ready || s.atRisk) continue;
                final Wallet.Res r = guardian.collectNow(s.coinid);
                if (r.ok) done++;
                else {
                    lastErr = r.error;
                    // A blocking condition fails every remaining stake identically — stop.
                    if (r.error != null && (r.error.contains("audit") || r.error.contains("Apps"))) break;
                }
            }
            toast(done > 0 ? "Posted " + done + " collect(s) — they confirm in about a block."
                    : (lastErr != null ? lastErr : "Nothing to collect."));
            refresh();
        });
    }

    /* ================= Activity tab ================= */

    private void renderActivity() {
        final LinearLayout c = Ui.card(this, 0);
        c.addView(Ui.text(this, "Activity", Ui.TEXT, 16, true));
        final JSONArray log = cfg.logLines();
        if (log.length() == 0) {
            c.addView(Ui.text(this, "Nothing yet. Every collect, sweep and recovery shows here with "
                    + "full coin ids and addresses.", Ui.DIM, 12, false));
        }
        for (int i = 0; i < log.length(); i++) {
            final JSONObject e = log.optJSONObject(i);
            if (e == null) continue;
            final String lvl = e.optString("level", "info");
            final int col = "error".equals(lvl) ? Ui.DANGER : "warn".equals(lvl) ? Ui.WARN : Ui.TEXT;
            final String msg = e.optString("msg", "");
            final String[] parts = msg.split(" \\| ", 2);
            c.addView(Ui.text(this, parts[0], col, 12, false));
            if (parts.length > 1) c.addView(Ui.text(this, parts[1], Ui.DIM, 10, false));
        }
        c.addView(Ui.ghost(this, "Reset history", v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Reset history?")
                    .setMessage("Clears the log and completed records. Keeps in-flight rescues and your "
                            + "safe address. Moves no funds — your coins and locks live on the chain.")
                    .setPositiveButton("Reset", (d, w) -> submit(() -> {
                        store.resetHistory();
                        cfg.clearLog();
                        refresh();
                    }))
                    .setNegativeButton("Cancel", null)
                    .show();
        }));
        content.addView(c);
    }

    /* ================= Help tab ================= */

    private void renderHelp() {
        content.addView(helpCard("The 30-second version",
                "1. Enable Future Cash Next in Minima Core → Apps.\n"
              + "2. Start the guardian on the Guardian tab.\n"
              + "3. Stakes tab: enter an amount, pick when it unlocks, press Lock it.\n\n"
              + "When the date arrives the guardian collects it for you, safely."));
        content.addView(helpCard("Why there's a guardian",
                "Minima signs with one-time keys — like a stamp that fades each time you press it. "
              + "Using the same key twice weakens it.\n\n"
              + "Your money can never be stolen AT collection: the chain only lets a collected coin go "
              + "to the payout address you chose when locking. The risk is what happens after it "
              + "lands. So the guardian checks every key first:\n\n"
              + "• Clean payout → collected normally.\n"
              + "• Reused payout → collected AND immediately swept to your safe address, then verified."));
        content.addView(helpCard("If the audit service is down",
                "Collecting carries on. Your last good result stays in force, and the app re-checks it "
              + "against your node's own key counters — no internet needed. It only pauses if a key "
              + "has actually signed again since, or after 24 hours without a successful audit."));
        content.addView(helpCard("My stake matured but nothing happened",
                "In order of likelihood:\n"
              + "① Future Cash Next isn't enabled in Minima Core → Apps.\n"
              + "② The guardian isn't started.\n"
              + "③ At-risk stake: rescue isn't set up, or your vault is locked.\n"
              + "④ Your safe destination was flagged REUSED — set a fresh one.\n"
              + "⑤ Auto-collect is off — use Collect on the stake itself.\n\n"
              + "Nothing is lost by waiting: matured coins stay locked to your payout address until "
              + "collected."));
        content.addView(helpCard("The Harden panel",
                "Your node routes change to a random pick of its default addresses. If a reused one is "
              + "still in that rotation, fresh money keeps landing on it. Sweep the coins off, then "
              + "Retire the address, then restart your node. Two minutes."));
    }

    private View helpCard(String title, String body) {
        final LinearLayout c = Ui.card(this, 0);
        c.addView(Ui.text(this, title, Ui.TEXT, 15, true));
        c.addView(Ui.text(this, body, Ui.DIM, 12, false));
        return c;
    }
}
