package com.eurobuddha.futurecashnext;

import org.json.JSONArray;
import org.json.JSONObject;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Set;

/** One discovery path for the list and guardian. Never treats watch-only scripts as ownership. */
final class StakeScan {
    private StakeScan() {}
    static Set<String> contracts(Wallet wallet) {
        Set<String> out = new LinkedHashSet<>();
        out.add(FutureCashContract.KNOWN_ADDRESS);
        String local = wallet.contractAddress();
        if (!MinimaAddress.isValidPublicKey(local))
            throw new IllegalStateException("Could not register the FutureCash contract.");
        out.add(local);
        return out;
    }
    static boolean isStake(JSONObject coin, Set<String> contracts) {
        if (coin == null || coin.optBoolean("spent", false)) return false;
        String address = coin.optString("address", "");
        boolean contract = false;
        for (String allowed : contracts) contract |= allowed.equalsIgnoreCase(address);
        return contract && MinimaAddress.isValidPublicKey(coin.optString("coinid", ""))
                && Guardian.looksLikeFutureCash(coin)
                && Node.validAddr(Node.state(coin, FutureCashContract.ST_RECIPIENT));
    }
    /** Relevant-only, read-only. The minimaWallet-owned `coins address:` + `cointrack` branch lives in
     *  Pandamonium's copy (apks/base/integrated/futurecash-next/.../StakeScan.java); this app never
     *  has walletOwned addresses, so it is not carried here. */
    static JSONArray read(Node node, Wallet.AddressSets owners, Set<String> contracts) {
        LinkedHashMap<String,JSONObject> found = new LinkedHashMap<>();
        add(found, node.cmd("coins relevant:true"), owners, contracts);
        JSONArray out = new JSONArray();
        for (JSONObject coin : found.values()) out.put(coin);
        return out;
    }
    private static void add(LinkedHashMap<String,JSONObject> out, JSONObject reply,
                            Wallet.AddressSets owners, Set<String> contracts) {
        if (reply == null || !Boolean.TRUE.equals(reply.opt("status")) || Node.pending(reply)
                || reply.optJSONArray("response") == null)
            throw new IllegalStateException("Could not read the complete stake list. Refresh and retry.");
        JSONArray coins = reply.optJSONArray("response");
        for (int i=0;i<coins.length();i++) {
            JSONObject coin=coins.optJSONObject(i);
            if (coin == null) throw new IllegalStateException("Could not read the complete stake list. Refresh and retry.");
            if (isStake(coin,contracts) && Wallet.owns(owners.ownDefault,
                    Node.state(coin,FutureCashContract.ST_RECIPIENT)))
                out.put(coin.optString("coinid").toLowerCase(java.util.Locale.ROOT),coin);
        }
    }
}
