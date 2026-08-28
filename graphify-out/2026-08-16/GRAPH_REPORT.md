# Graph Report - futurecash-next  (2026-08-13)

## Corpus Check
- 32 files · ~38,651 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 548 nodes · 1624 edges · 35 communities (18 shown, 17 thin omitted)
- Extraction: 89% EXTRACTED · 11% INFERRED · 0% AMBIGUOUS · INFERRED: 177 edges (avg confidence: 0.8)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `64e3b6ed`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- Wallet
- org.junit.Test
- Design
- Result
- Store
- GuardianService
- JSONArray
- Util
- Copy
- FutureCashContract
- Node
- android.content.Context
- gradlew
- NodeApi
- Cfg
- MainActivity
- JSONArray
- Cfg
- MainActivity.java
- TextView
- .field
- .doWork
- android.widget.TextView
- Sha3
- Mode
- CoinState
- BroadcastReceiver
- Cfg
- Result
- Guardian
- Node
- Override
- Store

## God Nodes (most connected - your core abstractions)
1. `MainActivity` - 46 edges
2. `Design` - 42 edges
3. `Wallet` - 39 edges
4. `Guardian` - 33 edges
5. `Store` - 33 edges
6. `Node` - 26 edges
7. `Ui` - 25 edges
8. `Cfg` - 22 edges
9. `GuardianService` - 21 edges
10. `AuditApi` - 19 edges

## Surprising Connections (you probably didn't know these)
- `Guardian` --references--> `Cfg`  [EXTRACTED]
  app/src/main/java/com/eurobuddha/futurecashnext/Guardian.java → app/src/main/java/com/eurobuddha/futurecashnext/Cfg.java
- `Audit` --references--> `Node`  [EXTRACTED]
  app/src/main/java/com/eurobuddha/futurecashnext/Audit.java → app/src/main/java/com/eurobuddha/futurecashnext/Node.java
- `AuditRunner` --references--> `Node`  [EXTRACTED]
  app/src/main/java/com/eurobuddha/futurecashnext/AuditRunner.java → app/src/main/java/com/eurobuddha/futurecashnext/Node.java
- `Audit` --references--> `Cfg`  [EXTRACTED]
  app/src/main/java/com/eurobuddha/futurecashnext/Audit.java → app/src/main/java/com/eurobuddha/futurecashnext/Cfg.java
- `AuditRunner` --references--> `Cfg`  [EXTRACTED]
  app/src/main/java/com/eurobuddha/futurecashnext/AuditRunner.java → app/src/main/java/com/eurobuddha/futurecashnext/Cfg.java

## Import Cycles
- None detected.

## Communities (35 total, 17 thin omitted)

### Community 0 - "Wallet"
Cohesion: 0.06
Nodes (23): JSONArray, Guardian, Node, Store, Notifier, Stake, Node, AddressSets (+15 more)

### Community 1 - "org.junit.Test"
Cohesion: 0.07
Nodes (22): KeyAudit, LocalKey, Result, Reuse, Row, Status, AT_RISK, OK (+14 more)

### Community 2 - "Design"
Cohesion: 0.20
Nodes (4): android.graphics.drawable.GradientDrawable, Design, TextView, GradientDrawable

### Community 4 - "Store"
Cohesion: 0.09
Nodes (8): android.database.Cursor, android.database.sqlite.SQLiteDatabase, android.database.sqlite.SQLiteOpenHelper, Status, CollectRow, Override, Store, SweepRow

### Community 5 - "GuardianService"
Cohesion: 0.20
Nodes (8): GuardianService, BroadcastReceiver, Cfg, Guardian, Node, NodeApi, Override, Store

### Community 11 - "android.content.Context"
Cohesion: 0.23
Nodes (3): android.content.Context, Notifier, Intent

### Community 12 - "gradlew"
Cohesion: 0.60
Nodes (3): gradlew script, die(), warn()

### Community 15 - "NodeApi"
Cohesion: 0.22
Nodes (5): Cb, NodeApi, PairingListener, MinimaAPI, org.minimarex.minimaapi.MinimaAPI

### Community 16 - "Cfg"
Cohesion: 0.06
Nodes (18): android.app.Activity, android.content.SharedPreferences, android.os.Handler, Audit, Mode, FRESH, NONE, PARTIAL (+10 more)

### Community 17 - "MainActivity"
Cohesion: 0.09
Nodes (22): android.view.View, android.widget.Button, android.widget.EditText, android.widget.LinearLayout, BroadcastReceiver, Cfg, Guardian, Node (+14 more)

### Community 20 - "MainActivity.java"
Cohesion: 0.19
Nodes (8): android.app.Service, android.content.BroadcastReceiver, android.content.Intent, android.os.Bundle, android.os.IBinder, androidx.appcompat.app.AppCompatActivity, BootReceiver, Override

### Community 22 - ".field"
Cohesion: 0.29
Nodes (3): android.graphics.Typeface, TextView, EditText

### Community 23 - ".doWork"
Cohesion: 0.27
Nodes (7): androidx.annotation.NonNull, androidx.work.Worker, androidx.work.WorkerParameters, Intent, GuardianWorker, Override, Result

### Community 24 - "android.widget.TextView"
Cohesion: 0.33
Nodes (3): android.graphics.drawable.RippleDrawable, android.widget.TextView, RippleDrawable

### Community 26 - "Mode"
Cohesion: 0.40
Nodes (3): Mode, DAYLIGHT, ONYX

### Community 27 - "CoinState"
Cohesion: 0.40
Nodes (5): CoinState, GONE, SPENT, UNKNOWN, UNSPENT

## Knowledge Gaps
- **18 isolated node(s):** `ONYX`, `DAYLIGHT`, `UNSPENT`, `SPENT`, `GONE` (+13 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **17 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `MainActivity` connect `MainActivity` to `android.widget.TextView`, `MainActivity.java`?**
  _High betweenness centrality (0.074) - this node is a cross-community bridge._
- **Why does `Guardian` connect `Wallet` to `Cfg`, `MainActivity`, `Store`?**
  _High betweenness centrality (0.059) - this node is a cross-community bridge._
- **Why does `Cfg` connect `Cfg` to `Wallet`, `MainActivity`?**
  _High betweenness centrality (0.049) - this node is a cross-community bridge._
- **What connects `ONYX`, `DAYLIGHT`, `UNSPENT` to the rest of the system?**
  _18 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `Wallet` be split into smaller, more focused modules?**
  _Cohesion score 0.06383353905836713 - nodes in this community are weakly interconnected._
- **Should `org.junit.Test` be split into smaller, more focused modules?**
  _Cohesion score 0.06582278481012659 - nodes in this community are weakly interconnected._
- **Should `Store` be split into smaller, more focused modules?**
  _Cohesion score 0.08888888888888889 - nodes in this community are weakly interconnected._