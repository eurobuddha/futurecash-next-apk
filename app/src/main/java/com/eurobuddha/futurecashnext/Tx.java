package com.eurobuddha.futurecashnext;

import org.json.JSONObject;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Building and posting a transaction, with the cleanup guarantee the family requires:
 * <b>every path out of here runs {@code txndelete}</b>, success or failure.
 *
 * <p>Classic FutureCash 2.7.1 chains the whole thing as one command
 * ({@code txncreate;txninput;txnoutput;txnbasics;txnpost;txndelete}), so a failure at any step
 * aborts the chain before the delete and leaks the half-built transaction into the node's txn table.
 * Steps run separately here so the delete can live in a finally-equivalent.
 */
public final class Tx {

    private static final AtomicLong SEQ = new AtomicLong(0);

    private Tx() {}

    /** Collision-free per-process transaction id (classic used Math.random(), which can repeat). */
    public static String id(String kind) {
        return "FC" + kind + System.currentTimeMillis() + "_" + (SEQ.incrementAndGet() % 1000000);
    }

    /** Result of a post: ok, or an error with the flag that says the node wants a human. */
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
     * @param steps commands with {@code %ID%} standing in for the transaction id
     */
    public static Result buildAndPost(Node node, String kind, String... steps) {
        final String id = id(kind);
        try {
            for (String step : steps) {
                final String cmd = step.replace("%ID%", id);
                final JSONObject r = node.cmd(cmd);
                if (Node.pending(r)) {
                    return Result.unpaired("Minima Core is holding this for approval — the guardian has to act "
                            + "unattended, so enable Future Cash Next in Minima Core → Apps.");
                }
                if (!Node.ok(r)) {
                    // First word of the failing step tells the user which stage broke without
                    // dumping a whole command line into the log.
                    final String stage = cmd.split(" ")[0];
                    return Result.err(Node.error(r) + " (at " + stage + ")");
                }
            }
            return Result.ok();
        } finally {
            node.cmd("txndelete id:" + id);
        }
    }

    /**
     * Collect: spend the stake coin to its pinned payout. No signature — the covenant has no
     * SIGNEDBY, and VERIFYOUT pins the destination, so a collect can only ever pay the address
     * committed at lock time. That is what makes collecting WOTS-safe in itself.
     */
    public static Result collect(Node node, String coinid, String payout, String amount, String tokenid) {
        return buildAndPost(node, "COL",
                "txncreate id:%ID%",
                "txninput id:%ID% coinid:" + coinid,
                "txnoutput id:%ID% address:" + payout + " amount:" + amount
                        + " tokenid:" + tokenid + " storestate:false",
                "txnbasics id:%ID%;txnpost id:%ID%");
    }

    /**
     * Sweep: move a collected coin off its (reused) address to safety.
     *
     * <p>Signs with the address's REAL key, taken from its SIGNEDBY script, rather than
     * {@code publickey:auto}. Auto reads the script row's pubkey, which retiring an address nulls to
     * 0x00 — so auto cannot sign a retired address, while the explicit key is still in the keys
     * table and works either way.
     */
    public static Result sweep(Node node, String coinid, String dest, String amount,
                               String tokenid, String signKey) {
        return buildAndPost(node, "SWP",
                "txncreate id:%ID%",
                "txninput id:%ID% coinid:" + coinid,
                "txnoutput id:%ID% address:" + dest + " amount:" + amount
                        + " tokenid:" + tokenid + " storestate:false",
                "txnsign id:%ID% publickey:" + signKey,
                "txnbasics id:%ID%;txnpost id:%ID%");
    }
}
