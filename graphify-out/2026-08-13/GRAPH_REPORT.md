# Graph Report - futurecash-next  (2026-08-13)

## Corpus Check
- 32 files · ~38,127 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 542 nodes · 1619 edges · 26 communities (11 shown, 15 thin omitted)
- Extraction: 89% EXTRACTED · 11% INFERRED · 0% AMBIGUOUS · INFERRED: 178 edges (avg confidence: 0.8)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `66e22ccf`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- Wallet
- org.junit.Test
- Store
- GuardianService
- JSONArray
- Util
- Copy
- FutureCashContract
- Node
- gradlew
- AuditApi
- Guardian
- android.content.Context
- JSONArray
- Cfg
- MainActivity
- TextView
- Sha3
- BroadcastReceiver
- Node
- NodeApi
- Override
- Result
- Store

## God Nodes (most connected - your core abstractions)
1. `MainActivity` - 45 edges
2. `Design` - 42 edges
3. `Wallet` - 39 edges
4. `Guardian` - 35 edges
5. `Store` - 33 edges
6. `Node` - 26 edges
7. `Ui` - 25 edges
8. `Cfg` - 24 edges
9. `GuardianService` - 21 edges
10. `AuditApi` - 19 edges

## Surprising Connections (you probably didn't know these)
- `GuardianService` --references--> `Cfg`  [EXTRACTED]
  app/src/main/java/com/eurobuddha/futurecashnext/GuardianService.java → app/src/main/java/com/eurobuddha/futurecashnext/Cfg.java
- `GuardianService` --references--> `Guardian`  [EXTRACTED]
  app/src/main/java/com/eurobuddha/futurecashnext/GuardianService.java → app/src/main/java/com/eurobuddha/futurecashnext/Guardian.java
- `Audit` --references--> `Node`  [EXTRACTED]
  app/src/main/java/com/eurobuddha/futurecashnext/Audit.java → app/src/main/java/com/eurobuddha/futurecashnext/Node.java
- `AuditRunner` --references--> `Node`  [EXTRACTED]
  app/src/main/java/com/eurobuddha/futurecashnext/AuditRunner.java → app/src/main/java/com/eurobuddha/futurecashnext/Node.java
- `AuditRunner` --references--> `AuditApi`  [EXTRACTED]
  app/src/main/java/com/eurobuddha/futurecashnext/AuditRunner.java → app/src/main/java/com/eurobuddha/futurecashnext/AuditApi.java

## Import Cycles
- None detected.

## Communities (26 total, 15 thin omitted)

### Community 0 - "Wallet"
Cohesion: 0.07
Nodes (18): CoinState, GONE, SPENT, UNKNOWN, UNSPENT, Node, Result, Tx (+10 more)

### Community 1 - "org.junit.Test"
Cohesion: 0.07
Nodes (22): KeyAudit, LocalKey, Result, Reuse, Row, Status, AT_RISK, OK (+14 more)

### Community 4 - "Store"
Cohesion: 0.09
Nodes (8): android.database.Cursor, android.database.sqlite.SQLiteDatabase, android.database.sqlite.SQLiteOpenHelper, Status, CollectRow, Override, Store, SweepRow

### Community 5 - "GuardianService"
Cohesion: 0.09
Nodes (19): android.app.Service, android.content.BroadcastReceiver, android.content.Intent, android.os.IBinder, androidx.annotation.NonNull, androidx.work.Worker, androidx.work.WorkerParameters, BootReceiver (+11 more)

### Community 12 - "gradlew"
Cohesion: 0.60
Nodes (3): gradlew script, die(), warn()

### Community 15 - "AuditApi"
Cohesion: 0.09
Nodes (9): android.app.Activity, android.os.Handler, AuditApi, Cb, Cb, NodeApi, PairingListener, MinimaAPI (+1 more)

### Community 16 - "Guardian"
Cohesion: 0.06
Nodes (25): android.content.SharedPreferences, Audit, JSONArray, Mode, FRESH, NONE, PARTIAL, RECONFIRMED (+17 more)

### Community 17 - "android.content.Context"
Cohesion: 0.08
Nodes (22): android.content.Context, android.graphics.drawable.GradientDrawable, android.graphics.drawable.RippleDrawable, android.graphics.Typeface, android.view.View, android.widget.Button, android.widget.EditText, android.widget.LinearLayout (+14 more)

### Community 20 - "MainActivity"
Cohesion: 0.10
Nodes (17): android.os.Bundle, androidx.appcompat.app.AppCompatActivity, MainActivity, Notifier, AuditRunner, BroadcastReceiver, Button, Cfg (+9 more)

## Knowledge Gaps
- **18 isolated node(s):** `ONYX`, `DAYLIGHT`, `UNSPENT`, `SPENT`, `GONE` (+13 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **15 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `Guardian` connect `Guardian` to `Wallet`, `android.content.Context`, `Store`, `GuardianService`?**
  _High betweenness centrality (0.072) - this node is a cross-community bridge._
- **Why does `MainActivity` connect `MainActivity` to `android.content.Context`, `GuardianService`?**
  _High betweenness centrality (0.071) - this node is a cross-community bridge._
- **Why does `Cfg` connect `Guardian` to `android.content.Context`, `GuardianService`?**
  _High betweenness centrality (0.062) - this node is a cross-community bridge._
- **What connects `ONYX`, `DAYLIGHT`, `UNSPENT` to the rest of the system?**
  _18 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `Wallet` be split into smaller, more focused modules?**
  _Cohesion score 0.07016229712858926 - nodes in this community are weakly interconnected._
- **Should `org.junit.Test` be split into smaller, more focused modules?**
  _Cohesion score 0.06582278481012659 - nodes in this community are weakly interconnected._
- **Should `Store` be split into smaller, more focused modules?**
  _Cohesion score 0.0946969696969697 - nodes in this community are weakly interconnected._