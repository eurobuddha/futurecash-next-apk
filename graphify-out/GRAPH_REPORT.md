# Graph Report - futurecash-next  (2026-08-12)

## Corpus Check
- 31 files · ~35,616 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 469 nodes · 1321 edges · 21 communities (12 shown, 9 thin omitted)
- Extraction: 87% EXTRACTED · 13% INFERRED · 0% AMBIGUOUS · INFERRED: 174 edges (avg confidence: 0.8)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `894bcb21`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- Wallet
- org.junit.Test
- MainActivity
- Cfg
- Store
- GuardianService
- JSONArray
- Util
- Copy
- FutureCashContract
- Node
- Result
- gradlew
- NodeApi
- Mode
- CoinState
- JSONArray
- Cfg
- Store

## God Nodes (most connected - your core abstractions)
1. `MainActivity` - 43 edges
2. `Wallet` - 39 edges
3. `Guardian` - 37 edges
4. `Store` - 33 edges
5. `Cfg` - 26 edges
6. `Node` - 26 edges
7. `GuardianService` - 21 edges
8. `AuditApi` - 19 edges
9. `LocalKey` - 19 edges
10. `KeyAuditTest` - 18 edges

## Surprising Connections (you probably didn't know these)
- `Guardian` --references--> `Cfg`  [EXTRACTED]
  app/src/main/java/com/eurobuddha/futurecashnext/Guardian.java → app/src/main/java/com/eurobuddha/futurecashnext/Cfg.java
- `GuardianService` --references--> `Cfg`  [EXTRACTED]
  app/src/main/java/com/eurobuddha/futurecashnext/GuardianService.java → app/src/main/java/com/eurobuddha/futurecashnext/Cfg.java
- `MainActivity` --references--> `Cfg`  [EXTRACTED]
  app/src/main/java/com/eurobuddha/futurecashnext/MainActivity.java → app/src/main/java/com/eurobuddha/futurecashnext/Cfg.java
- `GuardianService` --references--> `Guardian`  [EXTRACTED]
  app/src/main/java/com/eurobuddha/futurecashnext/GuardianService.java → app/src/main/java/com/eurobuddha/futurecashnext/Guardian.java
- `MainActivity` --references--> `Guardian`  [EXTRACTED]
  app/src/main/java/com/eurobuddha/futurecashnext/MainActivity.java → app/src/main/java/com/eurobuddha/futurecashnext/Guardian.java

## Import Cycles
- None detected.

## Communities (21 total, 9 thin omitted)

### Community 0 - "Wallet"
Cohesion: 0.07
Nodes (20): Guardian, Node, Store, Notifier, Stake, Node, AddressSets, Cfg (+12 more)

### Community 1 - "org.junit.Test"
Cohesion: 0.06
Nodes (23): KeyAudit, LocalKey, Result, Reuse, Row, Status, AT_RISK, OK (+15 more)

### Community 2 - "MainActivity"
Cohesion: 0.09
Nodes (25): android.content.Context, android.os.Bundle, android.view.View, android.widget.Button, android.widget.EditText, android.widget.LinearLayout, android.widget.TextView, androidx.appcompat.app.AppCompatActivity (+17 more)

### Community 3 - "Cfg"
Cohesion: 0.05
Nodes (21): android.app.Activity, android.content.SharedPreferences, android.os.Handler, androidx.annotation.NonNull, androidx.work.Worker, androidx.work.WorkerParameters, Audit, JSONArray (+13 more)

### Community 4 - "Store"
Cohesion: 0.10
Nodes (7): android.database.Cursor, android.database.sqlite.SQLiteDatabase, android.database.sqlite.SQLiteOpenHelper, CollectRow, Override, Store, SweepRow

### Community 5 - "GuardianService"
Cohesion: 0.12
Nodes (12): android.app.Service, android.content.BroadcastReceiver, android.content.Intent, android.os.IBinder, BootReceiver, GuardianService, BroadcastReceiver, Node (+4 more)

### Community 12 - "gradlew"
Cohesion: 0.60
Nodes (3): gradlew script, die(), warn()

### Community 15 - "NodeApi"
Cohesion: 0.22
Nodes (5): Cb, NodeApi, PairingListener, MinimaAPI, org.minimarex.minimaapi.MinimaAPI

### Community 16 - "Mode"
Cohesion: 0.29
Nodes (7): Mode, FRESH, NONE, PARTIAL, RECONFIRMED, STALE, Verdict

### Community 17 - "CoinState"
Cohesion: 0.40
Nodes (5): CoinState, GONE, SPENT, UNKNOWN, UNSPENT

## Knowledge Gaps
- **16 isolated node(s):** `UNSPENT`, `SPENT`, `GONE`, `UNKNOWN`, `FRESH` (+11 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **9 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `Guardian` connect `Wallet` to `MainActivity`, `Cfg`, `Store`, `GuardianService`?**
  _High betweenness centrality (0.104) - this node is a cross-community bridge._
- **Why does `Cfg` connect `Cfg` to `Wallet`, `MainActivity`, `GuardianService`?**
  _High betweenness centrality (0.086) - this node is a cross-community bridge._
- **Why does `MainActivity` connect `MainActivity` to `Wallet`, `Cfg`, `GuardianService`?**
  _High betweenness centrality (0.080) - this node is a cross-community bridge._
- **What connects `UNSPENT`, `SPENT`, `GONE` to the rest of the system?**
  _16 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `Wallet` be split into smaller, more focused modules?**
  _Cohesion score 0.06772277227722773 - nodes in this community are weakly interconnected._
- **Should `org.junit.Test` be split into smaller, more focused modules?**
  _Cohesion score 0.059370725034199726 - nodes in this community are weakly interconnected._
- **Should `MainActivity` be split into smaller, more focused modules?**
  _Cohesion score 0.09477477477477478 - nodes in this community are weakly interconnected._