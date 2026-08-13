# Graph Report - futurecash-next  (2026-08-13)

## Corpus Check
- 32 files · ~38,082 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 541 nodes · 1612 edges · 32 communities (14 shown, 18 thin omitted)
- Extraction: 89% EXTRACTED · 11% INFERRED · 0% AMBIGUOUS · INFERRED: 178 edges (avg confidence: 0.8)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `5da599bc`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- Wallet
- org.junit.Test
- .renderStakes
- AuditRunner
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
- Audit
- android.content.Context
- JSONArray
- Cfg
- MainActivity
- Design
- AuditApi
- Cfg
- Notifier
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
- `Audit` --references--> `Cfg`  [EXTRACTED]
  app/src/main/java/com/eurobuddha/futurecashnext/Audit.java → app/src/main/java/com/eurobuddha/futurecashnext/Cfg.java
- `AuditRunner` --references--> `Cfg`  [EXTRACTED]
  app/src/main/java/com/eurobuddha/futurecashnext/AuditRunner.java → app/src/main/java/com/eurobuddha/futurecashnext/Cfg.java
- `Guardian` --references--> `Cfg`  [EXTRACTED]
  app/src/main/java/com/eurobuddha/futurecashnext/Guardian.java → app/src/main/java/com/eurobuddha/futurecashnext/Cfg.java
- `GuardianService` --references--> `Cfg`  [EXTRACTED]
  app/src/main/java/com/eurobuddha/futurecashnext/GuardianService.java → app/src/main/java/com/eurobuddha/futurecashnext/Cfg.java
- `GuardianService` --references--> `Guardian`  [EXTRACTED]
  app/src/main/java/com/eurobuddha/futurecashnext/GuardianService.java → app/src/main/java/com/eurobuddha/futurecashnext/Guardian.java

## Import Cycles
- None detected.

## Communities (32 total, 18 thin omitted)

### Community 0 - "Wallet"
Cohesion: 0.06
Nodes (27): JSONArray, Guardian, Node, Store, Notifier, Stake, CoinState, GONE (+19 more)

### Community 1 - "org.junit.Test"
Cohesion: 0.07
Nodes (22): KeyAudit, LocalKey, Result, Reuse, Row, Status, AT_RISK, OK (+14 more)

### Community 2 - ".renderStakes"
Cohesion: 0.26
Nodes (4): android.view.View, android.widget.LinearLayout, LinearLayout, Status

### Community 3 - "AuditRunner"
Cohesion: 0.15
Nodes (4): android.app.Activity, AuditRunner, Cb, LongConsumer

### Community 4 - "Store"
Cohesion: 0.09
Nodes (8): android.database.Cursor, android.database.sqlite.SQLiteDatabase, android.database.sqlite.SQLiteOpenHelper, Status, CollectRow, Override, Store, SweepRow

### Community 5 - "GuardianService"
Cohesion: 0.09
Nodes (19): android.app.Service, android.content.BroadcastReceiver, android.content.Intent, android.os.IBinder, androidx.annotation.NonNull, androidx.work.Worker, androidx.work.WorkerParameters, BootReceiver (+11 more)

### Community 12 - "gradlew"
Cohesion: 0.60
Nodes (3): gradlew script, die(), warn()

### Community 15 - "NodeApi"
Cohesion: 0.19
Nodes (6): android.os.Handler, Cb, NodeApi, PairingListener, MinimaAPI, org.minimarex.minimaapi.MinimaAPI

### Community 16 - "Audit"
Cohesion: 0.19
Nodes (9): Audit, Mode, FRESH, NONE, PARTIAL, RECONFIRMED, STALE, Verdict (+1 more)

### Community 17 - "android.content.Context"
Cohesion: 0.14
Nodes (10): android.content.Context, android.graphics.drawable.GradientDrawable, android.widget.Button, android.widget.TextView, TextView, TextView, Ui, Button (+2 more)

### Community 20 - "MainActivity"
Cohesion: 0.13
Nodes (15): android.os.Bundle, android.widget.EditText, androidx.appcompat.app.AppCompatActivity, MainActivity, AuditRunner, BroadcastReceiver, Cfg, Guardian (+7 more)

### Community 21 - "Design"
Cohesion: 0.10
Nodes (9): android.graphics.drawable.RippleDrawable, android.graphics.Typeface, Design, Mode, DAYLIGHT, ONYX, TextView, EditText (+1 more)

## Knowledge Gaps
- **18 isolated node(s):** `ONYX`, `DAYLIGHT`, `UNSPENT`, `SPENT`, `GONE` (+13 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **18 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `Guardian` connect `Wallet` to `.renderStakes`, `Store`, `GuardianService`, `Cfg`?**
  _High betweenness centrality (0.072) - this node is a cross-community bridge._
- **Why does `MainActivity` connect `MainActivity` to `.renderStakes`, `AuditRunner`, `GuardianService`, `android.content.Context`, `Design`?**
  _High betweenness centrality (0.071) - this node is a cross-community bridge._
- **Why does `Cfg` connect `Cfg` to `Wallet`, `.renderStakes`, `AuditRunner`, `GuardianService`, `Audit`?**
  _High betweenness centrality (0.062) - this node is a cross-community bridge._
- **What connects `ONYX`, `DAYLIGHT`, `UNSPENT` to the rest of the system?**
  _18 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `Wallet` be split into smaller, more focused modules?**
  _Cohesion score 0.059038901601830666 - nodes in this community are weakly interconnected._
- **Should `org.junit.Test` be split into smaller, more focused modules?**
  _Cohesion score 0.06582278481012659 - nodes in this community are weakly interconnected._
- **Should `AuditRunner` be split into smaller, more focused modules?**
  _Cohesion score 0.14705882352941177 - nodes in this community are weakly interconnected._