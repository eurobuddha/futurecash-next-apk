# Graph Report - futurecash-next  (2026-08-11)

## Corpus Check
- 31 files · ~34,291 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 454 nodes · 1293 edges · 15 communities (10 shown, 5 thin omitted)
- Extraction: 87% EXTRACTED · 13% INFERRED · 0% AMBIGUOUS · INFERRED: 167 edges (avg confidence: 0.8)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `29146927`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- Wallet
- org.junit.Test
- MainActivity
- Cfg
- Store
- android.content.Context
- JSONArray
- Util
- Copy
- FutureCashContract
- Node
- Node
- gradlew

## God Nodes (most connected - your core abstractions)
1. `Wallet` - 42 edges
2. `MainActivity` - 40 edges
3. `Guardian` - 37 edges
4. `Store` - 37 edges
5. `Node` - 30 edges
6. `Cfg` - 23 edges
7. `GuardianService` - 21 edges
8. `AuditApi` - 19 edges
9. `LocalKey` - 19 edges
10. `KeyAuditTest` - 18 edges

## Surprising Connections (you probably didn't know these)
- `Audit` --references--> `Node`  [EXTRACTED]
  app/src/main/java/com/eurobuddha/futurecashnext/Audit.java → app/src/main/java/com/eurobuddha/futurecashnext/Node.java
- `AuditRunner` --references--> `Node`  [EXTRACTED]
  app/src/main/java/com/eurobuddha/futurecashnext/AuditRunner.java → app/src/main/java/com/eurobuddha/futurecashnext/Node.java
- `GuardianService` --references--> `Node`  [EXTRACTED]
  app/src/main/java/com/eurobuddha/futurecashnext/GuardianService.java → app/src/main/java/com/eurobuddha/futurecashnext/Node.java
- `MainActivity` --references--> `Node`  [EXTRACTED]
  app/src/main/java/com/eurobuddha/futurecashnext/MainActivity.java → app/src/main/java/com/eurobuddha/futurecashnext/Node.java
- `GuardianService` --references--> `Guardian`  [EXTRACTED]
  app/src/main/java/com/eurobuddha/futurecashnext/GuardianService.java → app/src/main/java/com/eurobuddha/futurecashnext/Guardian.java

## Import Cycles
- None detected.

## Communities (15 total, 5 thin omitted)

### Community 0 - "Wallet"
Cohesion: 0.07
Nodes (18): JSONArray, Guardian, Cfg, Node, Notifier, Stake, AddressSets, Cfg (+10 more)

### Community 1 - "org.junit.Test"
Cohesion: 0.06
Nodes (23): KeyAudit, LocalKey, Result, Reuse, Row, Status, AT_RISK, OK (+15 more)

### Community 2 - "MainActivity"
Cohesion: 0.12
Nodes (16): android.os.Bundle, android.view.View, android.widget.Button, android.widget.EditText, android.widget.LinearLayout, android.widget.TextView, androidx.appcompat.app.AppCompatActivity, BroadcastReceiver (+8 more)

### Community 3 - "Cfg"
Cohesion: 0.06
Nodes (21): android.app.Activity, android.content.SharedPreferences, android.os.Handler, Audit, JSONArray, Mode, FRESH, NONE (+13 more)

### Community 4 - "Store"
Cohesion: 0.09
Nodes (8): android.database.Cursor, android.database.sqlite.SQLiteDatabase, android.database.sqlite.SQLiteOpenHelper, Status, CollectRow, Override, Store, SweepRow

### Community 5 - "android.content.Context"
Cohesion: 0.07
Nodes (22): android.app.Service, android.content.BroadcastReceiver, android.content.Context, android.content.Intent, android.os.IBinder, androidx.annotation.NonNull, androidx.work.Worker, androidx.work.WorkerParameters (+14 more)

### Community 11 - "Node"
Cohesion: 0.14
Nodes (9): CoinState, GONE, SPENT, UNKNOWN, UNSPENT, Node, Result, Tx (+1 more)

### Community 12 - "gradlew"
Cohesion: 0.60
Nodes (3): gradlew script, die(), warn()

## Knowledge Gaps
- **16 isolated node(s):** `UNSPENT`, `SPENT`, `GONE`, `UNKNOWN`, `FRESH` (+11 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **5 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `MainActivity` connect `MainActivity` to `Wallet`, `org.junit.Test`, `Cfg`, `Store`, `android.content.Context`, `Node`?**
  _High betweenness centrality (0.243) - this node is a cross-community bridge._
- **Why does `Result` connect `org.junit.Test` to `MainActivity`, `Cfg`, `android.content.Context`?**
  _High betweenness centrality (0.155) - this node is a cross-community bridge._
- **Why does `Guardian` connect `Wallet` to `MainActivity`, `Store`, `android.content.Context`?**
  _High betweenness centrality (0.111) - this node is a cross-community bridge._
- **What connects `UNSPENT`, `SPENT`, `GONE` to the rest of the system?**
  _16 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `Wallet` be split into smaller, more focused modules?**
  _Cohesion score 0.07252345001143903 - nodes in this community are weakly interconnected._
- **Should `org.junit.Test` be split into smaller, more focused modules?**
  _Cohesion score 0.059370725034199726 - nodes in this community are weakly interconnected._
- **Should `MainActivity` be split into smaller, more focused modules?**
  _Cohesion score 0.11898466419883659 - nodes in this community are weakly interconnected._