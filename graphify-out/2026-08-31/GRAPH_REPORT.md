# Graph Report - futurecash-next  (2026-08-31)

## Corpus Check
- 34 files · ~39,267 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 559 nodes · 1641 edges · 31 communities (13 shown, 18 thin omitted)
- Extraction: 89% EXTRACTED · 11% INFERRED · 0% AMBIGUOUS · INFERRED: 177 edges (avg confidence: 0.8)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `96c99902`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- Wallet
- org.junit.Test
- android.content.Context
- Result
- Store
- GuardianService
- JSONArray
- Util
- Copy
- FutureCashContract
- Node
- User instructions — AUTHORITATIVE. These override default behavior and must be followed exactly.
- gradlew
- NodeApi
- Cfg
- MainActivity
- JSONArray
- Cfg
- install.sh
- TextView
- pre-commit
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
8. `GuardianService` - 23 edges
9. `Cfg` - 22 edges
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

## Communities (31 total, 18 thin omitted)

### Community 0 - "Wallet"
Cohesion: 0.06
Nodes (23): Guardian, Node, Store, Notifier, Stake, Node, Tx, AddressSets (+15 more)

### Community 1 - "org.junit.Test"
Cohesion: 0.06
Nodes (23): KeyAudit, LocalKey, Result, Reuse, Row, Status, AT_RISK, OK (+15 more)

### Community 2 - "android.content.Context"
Cohesion: 0.08
Nodes (23): android.content.Context, android.graphics.drawable.GradientDrawable, android.graphics.drawable.RippleDrawable, android.graphics.Typeface, android.view.View, android.widget.Button, android.widget.EditText, android.widget.LinearLayout (+15 more)

### Community 4 - "Store"
Cohesion: 0.09
Nodes (8): android.database.Cursor, android.database.sqlite.SQLiteDatabase, android.database.sqlite.SQLiteOpenHelper, Status, CollectRow, Override, Store, SweepRow

### Community 5 - "GuardianService"
Cohesion: 0.07
Nodes (24): android.app.Notification, android.app.Service, android.content.BroadcastReceiver, android.content.Intent, android.os.IBinder, androidx.annotation.NonNull, androidx.work.Worker, androidx.work.WorkerParameters (+16 more)

### Community 11 - "User instructions — AUTHORITATIVE. These override default behavior and must be followed exactly."
Cohesion: 0.50
Nodes (3): RULE 0 (highest priority) — Follow the user's explicit instructions. They are BLOCKING, not suggestions., User instructions — AUTHORITATIVE. These override default behavior and must be followed exactly., Versioning guardrail — every code change ships with a version bump

### Community 12 - "gradlew"
Cohesion: 0.60
Nodes (3): gradlew script, die(), warn()

### Community 15 - "NodeApi"
Cohesion: 0.19
Nodes (6): android.os.Handler, Cb, NodeApi, PairingListener, MinimaAPI, org.minimarex.minimaapi.MinimaAPI

### Community 16 - "Cfg"
Cohesion: 0.06
Nodes (18): android.app.Activity, android.content.SharedPreferences, Audit, JSONArray, Mode, FRESH, NONE, PARTIAL (+10 more)

### Community 17 - "MainActivity"
Cohesion: 0.12
Nodes (15): android.os.Bundle, androidx.appcompat.app.AppCompatActivity, BroadcastReceiver, Cfg, Guardian, Node, NodeApi, Override (+7 more)

### Community 27 - "CoinState"
Cohesion: 0.40
Nodes (5): CoinState, GONE, SPENT, UNKNOWN, UNSPENT

## Knowledge Gaps
- **21 isolated node(s):** `install.sh script`, `RULE 0 (highest priority) — Follow the user's explicit instructions. They are BLOCKING, not suggestions.`, `Versioning guardrail — every code change ships with a version bump`, `ONYX`, `DAYLIGHT` (+16 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **18 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `MainActivity` connect `MainActivity` to `android.content.Context`, `GuardianService`?**
  _High betweenness centrality (0.072) - this node is a cross-community bridge._
- **Why does `Guardian` connect `Wallet` to `Cfg`, `android.content.Context`, `Store`?**
  _High betweenness centrality (0.057) - this node is a cross-community bridge._
- **Why does `Cfg` connect `Cfg` to `Wallet`, `MainActivity`, `GuardianService`?**
  _High betweenness centrality (0.047) - this node is a cross-community bridge._
- **What connects `install.sh script`, `RULE 0 (highest priority) — Follow the user's explicit instructions. They are BLOCKING, not suggestions.`, `Versioning guardrail — every code change ships with a version bump` to the rest of the system?**
  _21 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `Wallet` be split into smaller, more focused modules?**
  _Cohesion score 0.06054960409874243 - nodes in this community are weakly interconnected._
- **Should `org.junit.Test` be split into smaller, more focused modules?**
  _Cohesion score 0.059370725034199726 - nodes in this community are weakly interconnected._
- **Should `android.content.Context` be split into smaller, more focused modules?**
  _Cohesion score 0.07985347985347985 - nodes in this community are weakly interconnected._