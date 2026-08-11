package com.eurobuddha.futurecashnext;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Building and posting a transaction.
 *
 * <p>The sequence here is copied from the proven native collect in
 * {@code apks/futurecash CollectActivity.buildAndCollect}, NOT from the MiniDapp's — the two differ
 * in ways that matter, and only one of them works over this transport:
 *
 * <ul>
 *   <li><b>{@code scriptmmr:true} on the input.</b> This is what attaches the coin's script and MMR
 *       proof. The MiniDapp gets that from {@code txnbasics} instead (which is why a node with the
 *       covenant missing from its script table produced a transaction the chain silently rejected —
 *       see Wallet.registerContract).</li>
 *   <li><b>No {@code txnbasics} on the base path.</b> A whole-coin single-shot spend is
 *       self-balancing: one input, one output, same amount, no fee. {@code txnbasics} is there to
 *       scaffold an extra MINIMA input to fund a burn, and this app never burns. Observed: with it
 *       in the chain, a real collect on a real node failed AT that step; the two proven native
 *       sequences in this family (apks/futurecash collect, apks/openly settle) both omit it and both
 *       work. The precise reason core rejects it here has NOT been established — what is established
 *       is that the working sequences don't include it and this one did.</li>
 *   <li><b>{@code txnpost} is async-mined.</b> Build steps must report {@code status:true}, but a
 *       post legitimately comes back without it ({@code istransaction:false} is mining in progress,
 *       not failure). Treating that as an error meant a perfectly good post was logged as failed and
 *       retried — posting the same coin twice.</li>
 * </ul>
 *
 * <p>Every path out of here runs {@code txndelete}, success or failure. Classic 2.7.1 chains the
 * whole thing as one command, so a failure at any step aborts before the delete and leaks the
 * half-built transaction into the node's table.
 */
public final class Tx {

    private static final AtomicLong SEQ = new AtomicLong(0);

    private Tx() {}

    /** Collision-free per-process transaction id (classic used Math.random(), which can repeat). */
    public static String id(String kind) {
        return "FC" + kind + System.currentTimeMillis() + "_" + (SEQ.incrementAndGet() % 1000000);
    }

    public static final class Result {
        public final boolean ok;
        public final String error;
        public final boolean notPaired;   // the node parked it for approval / we aren't enabled

        private Result(boolean ok, String error, boolean notPaired) {
            this.ok = ok; this.error = error; this.notPaired = notPaired;
        }
        public static Result ok() { return new Result(true, null, false); }
        public static Result err(String e) { return new Result(false, e, false); }
        public static Result unpaired(String e) { return new Result(false, e, true); }
    }

    /**
     * Run the steps in order, deleting the transaction afterwards whatever happens.
     *
     * <p>{@code %ID%} in a step stands in for the transaction id. The LAST step is treated as the
     * post (async-mined; see the class comment); every earlier step must report success.
     */
    private static Result run(Node node, String kind, List<String> steps) {
        final String id = id(kind);
        try {
            for (int i = 0; i < steps.size(); i++) {
                final String cmd = steps.get(i).replace("%ID%", id);
                final boolean isPost = cmd.startsWith("txnpost");
                final JSONObject r = node.cmd(cmd);

                if (r == null) return Result.err("Minima Core didn't respond (at " + stage(cmd) + ")");
                if (Node.pending(r)) {
                    return Result.unpaired("Minima Core is holding this for approval — the guardian has "
                            + "to act unattended, so enable Future Cash Next in Minima Core → Apps.");
                }
                // Build steps must be status:true. A post may omit status while it is being mined.
                if (!r.optBoolean("status", isPost)) {
                    final String e = r.optString("error", "");
                    return Result.err((e.isEmpty() ? "failed" : e) + " (at " + stage(cmd) + ")");
                }
            }
            return Result.ok();
        } finally {
            node.cmd("txndelete id:" + id);
        }
    }

    /**
     * Refuse to build a command from malformed values.
     *
     * <p>The destination and token id of a collect come from the coin's ON-CHAIN state, and anyone
     * can put arbitrary state on a coin at the shared covenant address. VERIFYOUT would reject a bad
     * output on-chain anyway, but unvalidated data must never reach the node's command parser —
     * Minima chains commands on ';' (see the class comment).
     *
     * @return an error Result if anything is malformed, or null when all parts are well-formed
     */
    private static Result checkParts(String coinid, String address, String amount, String tokenid) {
        if (!Node.validHexAddr(coinid)) return Result.err("Refusing to spend a malformed coin id.");
        if (!Node.validAddr(address)) return Result.err("This payment's destination looks malformed — refusing.");
        if (!Node.validHexAddr(tokenid)) return Result.err("This payment's token id looks malformed — refusing.");
        if (amount == null || !amount.matches("^[0-9]+(\\.[0-9]+)?$")) {
            return Result.err("This payment's amount looks malformed — refusing.");
        }
        // ...and never post a ZERO output. Node.outAmount falls back to "0" when the field it wants is
        // absent (a token coin with no `tokenamount`), which would build a transaction paying nothing
        // while consuming the whole coin. VERIFYOUT would reject it on-chain, so it costs a wasted
        // attempt rather than the funds — but it is a transaction that should never leave here.
        if (amount.matches("^0(\\.0+)?$")) {
            return Result.err("Refusing to post a zero-amount output — the coin's amount didn't parse.");
        }
        return null;
    }

    /** First word of the command — names the stage without dumping a whole command line into the log. */
    private static String stage(String cmd) {
        final int sp = cmd.indexOf(' ');
        return sp > 0 ? cmd.substring(0, sp) : cmd;
    }

    /**
     * Collect: spend the stake coin to its pinned payout.
     *
     * <p>No signature — the covenant has no SIGNEDBY, and VERIFYOUT pins the destination, so a
     * collect can only ever pay the address committed at lock time. That is what makes collecting
     * WOTS-safe in itself.
     */
    public static Result collect(Node node, String coinid, String payout, String amount, String tokenid) {
        final Result bad = checkParts(coinid, payout, amount, tokenid);
        if (bad != null) return bad;
        final List<String> steps = new ArrayList<>();
        steps.add("txncreate id:%ID%");
        steps.add("txninput id:%ID% coinid:" + coinid + " scriptmmr:true");
        steps.add("txnoutput id:%ID% address:" + payout + " amount:" + amount
                + " tokenid:" + tokenid + " storestate:false");
        steps.add("txnpost id:%ID%");
        return run(node, "COL", steps);
    }

    /**
     * Sweep: move a collected coin off its (reused) address to safety.
     *
     * <p>Also a whole-coin single-shot spend, so it needs no {@code txnbasics} either — it just
     * needs a signature, which the collect doesn't.
     *
     * <p>Signs with the address's REAL key, taken from its SIGNEDBY script, rather than
     * {@code publickey:auto}. Auto reads the script row's pubkey, which retiring an address nulls to
     * 0x00 — so auto cannot sign a retired address, while the explicit key is still in the keys
     * table and works either way.
     */
    public static Result sweep(Node node, String coinid, String dest, String amount,
                               String tokenid, String signKey) {
        final Result bad = checkParts(coinid, dest, amount, tokenid);
        if (bad != null) return bad;
        if (!"auto".equals(signKey) && !Node.validHexAddr(signKey)) {
            return Result.err("Refusing to sign with a malformed public key.");
        }
        final List<String> steps = new ArrayList<>();
        steps.add("txncreate id:%ID%");
        steps.add("txninput id:%ID% coinid:" + coinid + " scriptmmr:true");
        steps.add("txnoutput id:%ID% address:" + dest + " amount:" + amount
                + " tokenid:" + tokenid + " storestate:false");
        steps.add("txnsign id:%ID% publickey:" + signKey);
        steps.add("txnpost id:%ID%");
        return run(node, "SWP", steps);
    }
}
