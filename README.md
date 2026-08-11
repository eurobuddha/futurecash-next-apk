# Future Cash Next (native Android)

`com.eurobuddha.futurecashnext` · companion APK for the Minima Core node app.

Native port of the **futurecash-next** MiniDapp (`mds/futurecash-next` v3.2.1) — time-locked payments
on [Minima](https://minima.global) **with WOTS key-reuse protection and an unattended guardian**.

This is the protected sibling of [`apks/futurecash`](../futurecash) (`com.eurobuddha.futurecash`),
which remains the simple lock-and-collect app. Both can be installed at once; they share the same
covenant address and interoperate with the web dapp.

## Why "next"

A collect is pinned by the covenant — `VERIFYOUT` forces the funds to the payout address committed at
lock time — so **a collect can only ever pay that address and is WOTS-safe in itself**. The danger is
what happens next: the funds now sit on a payout address whose one-time key may have been *reused*,
and a reused Winternitz leaf leaks its secret.

So this app:

| payout address | what the guardian does |
|---|---|
| **clean** (audit says so) | collects it — once you've opted in |
| **reused** (at risk) | collects **and immediately sweeps** to a safe destination, then verifies it arrived |
| **can't tell yet** | leaves it alone — an uncollected coin sits in the covenant where it is not at risk at all, so waiting is strictly safer than acting |

## Architecture

Companion APK — it does **not** embed a node. Java + classic Views, talking to Minima Core
(`org.minimarex.minimacore`) over broadcast-Intent IPC via `app/libs/minimaapi.aar`.

```
GuardianService (foreground, specialUse)          MainActivity (4 tabs)
   └── single worker thread                          └── single worker thread
        └── Guardian.reconcile(tip)  ── on every NEWBLOCK
              detect → collect → queue sweep → sweep → verify
                 │        │                      │
              Store    Tx (txndelete always)   Wallet (addresses, harden, recover)
                 │                                │
             SQLite                            Node → NodeApi → IPC → Minima Core
```

- **`Guardian`** — the reconcile engine. Blocking calls on a worker thread, so it reads as the
  straight sequence it is; the MiniDapp's nested-callback shape is an artifact of MDS having no
  synchronous command, not of the logic.
- **`Store`** — `fc_collect` / `fc_sweep`, same columns and status vocabulary as the MiniDapp, but
  with bound parameters and **atomic row claiming** (`UPDATE … WHERE status=?` + `changes()`), so two
  passes can never both post for the same coin.
- **`Audit` / `AuditRunner` / `KeyAudit`** — the verdict and its freshness rules. `KeyAudit`,
  `MinimaAddress`, `Sha3` and `AuditApi` are lifted from [`apks/keyreuses`](../keyreuses) with their
  unit tests (28 tests, all passing).
- **`Wallet`** — address ownership, covenant registration, the rescue destination, Harden (retire
  reused change addresses) and Recover (coins stranded on retired ones).

### The `minimaapi.aar` matters here

This uses the **August** aar from `apks/keyreuses`, not the June one in `apks/futurecash`. Only the
newer build has `readResponseUri()` / `deliverResult()` / `mFileExecutor` — the `content://` file
hand-off for large replies. The guardian reads `coins relevant:true`, which measured **248KB** on a
node with many stakes; an inline reply that size overflows the broadcast parcel and kills the
receiving app uncatchably.

### Detection scans `coins relevant:true` only

The covenant is registered `trackall:false`, so core keeps a stake coin only when one of its state
variables is an address this node tracks — i.e. only when state port 2 pays out to **us**. Core's
relevance filter *is* the ownership filter.

Measured on a node with 47 stakes at the covenant address: all 33 belonging to that node were in the
relevant set. The same measurement found 14 **strangers'** stakes there too, because relevance is
stamped when a block is processed and never re-evaluated — anything that arrived while the node had
the covenant registered `trackall:true` stays relevant for good. So `ownsAddress` is still
load-bearing, not decoration.

## Build

Requires a JDK 17/21 (the Android Studio JBR works):

```sh
JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" ./gradlew assembleRelease
./gradlew testDebugUnitTest      # 28 unit tests, no device needed
```

Install, then enable **Future Cash Next** in Minima Core → Apps to authorize the IPC. Start the
guardian on the Guardian tab.

## Safety notes

- `allowBackup=false` is a fund-safety setting, not a preference — see the manifest comment. The data
  directory holds a resumable spend state machine and the audit verdict that licenses a bare collect.
- Every transaction path runs `txndelete`, success or failure.
- Only **public** keys and addresses ever leave the device, over https, no redirects, bounded reads.
