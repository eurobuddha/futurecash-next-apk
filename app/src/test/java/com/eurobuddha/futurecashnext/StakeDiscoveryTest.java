package com.eurobuddha.futurecashnext;

import org.json.*;
import org.junit.Test;
import org.mockito.MockedConstruction;
import java.util.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/** Synthetic chain replies only. No node, funds, seeds, signing counters or network. */
public class StakeDiscoveryTest {
    static final String A="0x"+"11".repeat(32), B="0x"+"22".repeat(32), C="0x"+"33".repeat(32);
    static final MinimaAddress.Result NODE=MinimaAddress.fromPublicKey(A), WALLET=MinimaAddress.fromPublicKey(B);
    static JSONObject ok(Object response) throws Exception {return new JSONObject().put("status",true).put("response",response);}
    static JSONArray rows(Object... rows) {JSONArray out=new JSONArray();for(Object row:rows)out.put(row);return out;}
    static JSONObject script(MinimaAddress.Result address,String key) throws Exception {
        return new JSONObject().put("address",address.hex).put("miniaddress",address.mx).put("publickey",key).put("script",address.script);
    }
    static JSONObject coin(String id,String payout) throws Exception {
        return new JSONObject().put("coinid",id).put("address",FutureCashContract.KNOWN_ADDRESS)
                .put("spent",false).put("amount","5000").put("tokenid","0x00").put("created","40")
                .put("state",rows(new JSONObject().put("port",1).put("data","100"),
                        new JSONObject().put("port",2).put("data",payout),new JSONObject().put("port",4).put("data","20")));
    }
    static class Fixture {
        final Node node=mock(Node.class);final Cfg cfg=mock(Cfg.class);final Store store=mock(Store.class);
        Fixture() throws Exception {
            when(store.readyTotals(anyLong(),anyBoolean())).thenReturn(new double[]{0,0});
            when(store.pendingAtRisk(anyLong())).thenReturn(new double[]{0,0,0});
            when(cfg.get(Cfg.SCRIPT_ADDRESS,null)).thenReturn(FutureCashContract.KNOWN_ADDRESS);
            when(cfg.atRisk()).thenReturn(new JSONArray());
            when(node.cmd("scripts")).thenReturn(ok(rows(script(NODE,A),script(WALLET,"0x00"))));
            when(node.cmd("keys action:list")).thenReturn(ok(new JSONObject().put("keys",rows(new JSONObject().put("publickey",A)))));
            when(node.cmd("coins relevant:true")).thenReturn(ok(rows(coin(A,NODE.hex))));
            when(node.cmd("coins address:"+FutureCashContract.KNOWN_ADDRESS)).thenReturn(ok(rows(coin(A,NODE.hex),coin(B,WALLET.hex),coin(C,MinimaAddress.fromPublicKey(C).hex))));
            when(node.cmd("block")).thenReturn(ok(new JSONObject().put("block","90")));
        }
        Guardian guardian() {return new Guardian(node,cfg,store,(a,b)->{});}
        Wallet wallet() {return new Wallet(node,cfg);}
        /** Rescue ON to an external safe, with one sweep already queued PENDING on our own address. */
        void rescueWithPendingSweep() throws Exception {
            when(cfg.is(Cfg.ENABLED,false)).thenReturn(true);
            when(cfg.get(Cfg.SAFE_ADDRESS,null)).thenReturn(MinimaAddress.fromPublicKey(C).mx);
            Store.SweepRow pending=new Store.SweepRow();
            pending.coinid=B;pending.address=NODE.hex;pending.tokenid="0x00";pending.amount="5000";pending.status=Store.PENDING;
            when(store.sweepsWithStatus(Store.PENDING)).thenReturn(Collections.singletonList(pending));
            when(store.claimSweepForPost(eq(B),anyLong())).thenReturn(true);
            when(node.cmd("scripts address:"+NODE.hex)).thenReturn(ok(rows(script(NODE,A))));
        }
    }
    @Test public void onePassScansTheNodeOnceAndHandsBackTheStakeList() throws Exception {
        Fixture f=new Fixture();
        List<Guardian.Stake> stakes=f.guardian().reconcile(90);
        assertEquals(1,stakes.size());assertTrue(stakes.get(0).ready);
        verify(f.node,times(1)).cmd("coins relevant:true");verify(f.node,times(1)).cmd("keys action:list");
    }
    @Test public void flakyKeyListAfterTheRiskReloadCannotStrandAQueuedSweep() throws Exception {
        Fixture f=new Fixture();f.rescueWithPendingSweep();
        when(f.node.cmd("status")).thenReturn(ok(new JSONObject().put("locked",false)));
        // First answer (the risk reload) is good; any later one would fail. The pass must not ask again.
        when(f.node.cmd("keys action:list")).thenReturn(
                ok(new JSONObject().put("keys",rows(new JSONObject().put("publickey",A)))),new JSONObject().put("status",false));
        f.guardian().reconcile(90);
        verify(f.node,times(1)).cmd("keys action:list");
        verify(f.node,times(1)).cmd(startsWith("txncreate"));   // sweepPending was reached and posted
    }
    @Test public void unknownVaultStateGatesLikeLockedButIsWordedAsUnknown() throws Exception {
        Fixture f=new Fixture();f.rescueWithPendingSweep();
        when(f.node.cmd("status")).thenReturn(new JSONObject());   // not a usable answer
        Guardian g=f.guardian();g.reconcile(90);
        assertTrue(g.status(90).vaultUnknown);assertFalse(g.status(90).vaultLocked);
        verify(f.node,never()).cmd(startsWith("txncreate"));   // canSweep=false: the queued sweep waits
        verify(f.cfg,never()).log(eq(Cfg.LVL_ERROR),startsWith("Rescue is OFF"));
        verify(f.cfg,never()).log(eq(Cfg.LVL_WARN),startsWith("Vault locked"));
        verify(f.cfg).log(eq(Cfg.LVL_WARN),startsWith("Couldn't confirm your node's vault"));
    }
    @Test public void mxAddressesAreCaseInsensitiveAndStoredUppercase() throws Exception {
        assertEquals(Wallet.canonAddr("Mxabc"),Wallet.canonAddr("MXABC"));
        Fixture f=new Fixture();final String external=MinimaAddress.fromPublicKey(C).mx;
        final String typed="Mx"+external.substring(2).toLowerCase(Locale.ROOT);   // Node.validMx needs the Mx prefix
        // The node's REAL answer for an address not in its table is status:false "not found" —
        // an external destination must be ACCEPTED on that reply, never refused.
        when(f.node.cmd(startsWith("scripts address:"))).thenReturn(new JSONObject().put("status",false).put("error","Script with that address not found"));
        when(f.cfg.get(Cfg.SAFE_ADDRESS,null)).thenReturn(external);   // what the persisted read-back returns
        Wallet.Res r=f.wallet().setSafeExternal(typed,0);
        assertTrue(r.error,r.ok);assertEquals(external,r.value);assertTrue(r.value.startsWith("Mx"));
        assertEquals("Mx"+r.value.substring(2).toUpperCase(Locale.ROOT),r.value);
        verify(f.cfg).set(Cfg.SAFE_ADDRESS,external);
        // And a node that can't say whether the address is local is a refusal, not an acceptance.
        when(f.node.cmd("scripts")).thenReturn(new JSONObject().put("status",false));
        assertFalse(f.wallet().setSafeExternal(typed,0).ok);
    }
    @Test public void nodeListFiltersUnrelatedContractsAndOtherOwnersAndUsesCoinAge() throws Exception {
        Fixture f=new Fixture();JSONObject spoof=coin(B,NODE.hex).put("address",C), foreign=coin(C,WALLET.hex);
        when(f.node.cmd("coins relevant:true")).thenReturn(ok(rows(coin(A,NODE.hex),spoof,foreign)));
        List<Guardian.Stake> stakes=f.guardian().listStakes();assertEquals(1,stakes.size());assertTrue(stakes.get(0).ready);
        verify(f.node,never()).cmd(startsWith("coins address:"));
    }
    @Test public void failedReplyCannotLookLikeAnEmptySuccessfulScan() throws Exception {
        Fixture f=new Fixture();when(f.node.cmd("coins relevant:true")).thenReturn(new JSONObject().put("status",false));
        assertThrows(IllegalStateException.class,()->f.guardian().listStakes());
    }
    @Test public void unrelatedContractWithMatchingStateCannotBeCollected() throws Exception {
        Fixture f=new Fixture();when(f.node.cmd("coins coinid:"+B)).thenReturn(ok(rows(coin(B,NODE.hex).put("address",C))));
        assertFalse(f.guardian().collectNow(B).ok);verify(f.node,never()).cmd(startsWith("txncreate"));
    }

    @Test public void riskReloadRetainsRetiredNodeKeysEvenWhenAuditCacheIsEmpty() throws Exception {
        Fixture f=new Fixture();
        when(f.node.cmd("scripts")).thenReturn(ok(rows(script(NODE,"0x00"))));
        Guardian g=f.guardian();g.reloadAtRisk();
        assertTrue(g.atRiskSet().contains(Wallet.canonAddr(NODE.hex)));
    }
    @Test public void unavailableOrMalformedVaultStatusCannotAuthorizeSigning() throws Exception {
        Fixture f=new Fixture();
        for(JSONObject bad:new JSONObject[]{new JSONObject(),ok(new JSONObject()),
                ok(new JSONObject().put("locked",false)).put("status",false),
                ok(new JSONObject().put("locked",false)).put("status","true"),
                ok(new JSONObject().put("locked",false)).put("pending",true),
                ok(new JSONObject().put("locked","false"))}) {
            // Not LOCKED (the fix is not "unlock it"), but never UNLOCKED either.
            when(f.node.cmd("status")).thenReturn(bad);assertEquals(Wallet.VaultState.UNKNOWN,f.wallet().vaultState());
        }
        when(f.node.cmd("status")).thenReturn(ok(new JSONObject().put("locked",true)));
        assertEquals(Wallet.VaultState.LOCKED,f.wallet().vaultState());
        when(f.node.cmd("status")).thenReturn(ok(new JSONObject().put("locked",false)));
        assertEquals(Wallet.VaultState.UNLOCKED,f.wallet().vaultState());
    }
    @Test public void unreadableAtRiskSourcesMakeManualCollectRefuseRatherThanPost() throws Exception {
        Fixture f=new Fixture();
        when(f.node.cmd("coins coinid:"+A)).thenReturn(ok(rows(coin(A,NODE.hex))));
        // keys action:list fails: the retired set cannot be known, so the at-risk gate cannot be trusted.
        when(f.node.cmd("keys action:list")).thenReturn(new JSONObject().put("status",false));
        Guardian g=f.guardian();
        assertThrows(IllegalStateException.class,g::reloadAtRisk);
        Wallet.Res r=g.collectNow(A);
        assertFalse(r.ok);assertEquals("Could not read the complete key list. Refresh and retry.",r.error);
        // A corrupt stored at-risk set is the same refusal.
        when(f.node.cmd("keys action:list")).thenReturn(ok(new JSONObject().put("keys",rows(new JSONObject().put("publickey",A)))));
        when(f.cfg.atRisk()).thenThrow(new IllegalStateException("Could not read the stored at-risk address list. Re-run the key audit."));
        r=g.collectNow(A);
        assertFalse(r.ok);assertEquals("Could not read the stored at-risk address list. Re-run the key audit.",r.error);
        verify(f.node,never()).cmd(startsWith("txncreate"));
    }
}
