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
            when(node.cmd(startsWith("cointrack enable:true coinid:"))).thenReturn(ok("tracked"));
            when(node.cmd("block")).thenReturn(ok(new JSONObject().put("block","90")));
        }
        Guardian guardian() {return new Guardian(node,cfg,store,(a,b)->{});}
        Wallet wallet() {return new Wallet(node,cfg);}
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
            when(f.node.cmd("status")).thenReturn(bad);assertTrue(f.wallet().vaultLocked());
        }
        when(f.node.cmd("status")).thenReturn(ok(new JSONObject().put("locked",false)));
        assertFalse(f.wallet().vaultLocked());
    }
}
