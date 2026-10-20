# Graph Report - apks/futurecash-next  (2026-09-04)

## Corpus Check
- 34 files · ~39,405 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 530 nodes · 1639 edges · 19 communities (13 shown, 6 thin omitted)
- Extraction: 89% EXTRACTED · 11% INFERRED · 0% AMBIGUOUS · INFERRED: 180 edges (avg confidence: 0.8)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `ae711fcf`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- Future Cash Next (native Android)
- org.junit.Test
- android.content.Context
- Wallet
- GuardianService
- Util
- Copy
- FutureCashContract
- User instructions — AUTHORITATIVE. These override default behavior and must be followed exactly.
- gradlew
- Cfg
- AuditApi
- MainActivity
- install.sh
- pre-commit
- Node
- Sha3

## God Nodes (most connected - your core abstractions)
1. `MainActivity` - 46 edges
2. `Design` - 42 edges
3. `Wallet` - 42 edges
4. `Store` - 39 edges
5. `Guardian` - 37 edges
6. `Node` - 34 edges
7. `Cfg` - 30 edges
8. `Ui` - 25 edges
9. `GuardianService` - 23 edges
10. `AuditApi` - 19 edges

## Surprising Connections (you probably didn't know these)
- `Audit` --references--> `Node`  [EXTRACTED]
  apks/futurecash-next/app/src/main/java/com/eurobuddha/futurecashnext/Audit.java → apks/futurecash-next/app/src/main/java/com/eurobuddha/futurecashnext/Node.java
- `AuditRunner` --references--> `Audit`  [EXTRACTED]
  apks/futurecash-next/app/src/main/java/com/eurobuddha/futurecashnext/AuditRunner.java → apks/futurecash-next/app/src/main/java/com/eurobuddha/futurecashnext/Audit.java
- `Guardian` --references--> `Audit`  [EXTRACTED]
  apks/futurecash-next/app/src/main/java/com/eurobuddha/futurecashnext/Guardian.java → apks/futurecash-next/app/src/main/java/com/eurobuddha/futurecashnext/Audit.java
- `MainActivity` --references--> `Verdict`  [EXTRACTED]
  apks/futurecash-next/app/src/main/java/com/eurobuddha/futurecashnext/MainActivity.java → apks/futurecash-next/app/src/main/java/com/eurobuddha/futurecashnext/Audit.java
- `AuditRunner` --references--> `AuditApi`  [EXTRACTED]
  apks/futurecash-next/app/src/main/java/com/eurobuddha/futurecashnext/AuditRunner.java → apks/futurecash-next/app/src/main/java/com/eurobuddha/futurecashnext/AuditApi.java

## Import Cycles
- None detected.

## Communities (19 total, 6 thin omitted)

### Community 0 - "Future Cash Next (native Android)"
Cohesion: 0.25
Nodes (7): Architecture, Build, Detection scans `coins relevant:true` only, Future Cash Next (native Android), Safety notes, The `minimaapi.aar` matters here, Why "next"

### Community 1 - "org.junit.Test"
Cohesion: 0.07
Nodes (22): KeyAudit, LocalKey, Result, Reuse, Row, Status, AT_RISK, OK (+14 more)

### Community 2 - "android.content.Context"
Cohesion: 0.08
Nodes (24): android.content.Context, android.graphics.drawable.GradientDrawable, android.graphics.drawable.RippleDrawable, android.graphics.Typeface, android.view.View, android.widget.Button, android.widget.EditText, android.widget.LinearLayout (+16 more)

### Community 4 - "Wallet"
Cohesion: 0.06
Nodes (18): android.database.Cursor, android.database.sqlite.SQLiteDatabase, android.database.sqlite.SQLiteOpenHelper, Guardian, Stake, Status, BroadcastReceiver, JSONArray (+10 more)

### Community 5 - "GuardianService"
Cohesion: 0.07
Nodes (19): android.app.Notification, android.app.Service, android.content.BroadcastReceiver, android.content.Intent, android.os.Bundle, android.os.IBinder, androidx.annotation.NonNull, androidx.appcompat.app.AppCompatActivity (+11 more)

### Community 11 - "User instructions — AUTHORITATIVE. These override default behavior and must be followed exactly."
Cohesion: 0.50
Nodes (3): RULE 0 (highest priority) — Follow the user's explicit instructions. They are BLOCKING, not suggestions., User instructions — AUTHORITATIVE. These override default behavior and must be followed exactly., Versioning guardrail — every code change ships with a version bump

### Community 12 - "gradlew"
Cohesion: 0.60
Nodes (3): gradlew script, die(), warn()

### Community 15 - "Cfg"
Cohesion: 0.07
Nodes (16): android.content.SharedPreferences, Audit, JSONArray, Mode, FRESH, NONE, PARTIAL, RECONFIRMED (+8 more)

### Community 16 - "AuditApi"
Cohesion: 0.09
Nodes (8): android.app.Activity, android.os.Handler, AuditApi, Cb, Cb, NodeApi, PairingListener, MinimaAPI

### Community 17 - "MainActivity"
Cohesion: 0.15
Nodes (6): AuditRunner, Cb, BroadcastReceiver, Override, MainActivity, LongConsumer

### Community 24 - "Node"
Cohesion: 0.16
Nodes (8): CoinState, GONE, SPENT, UNKNOWN, UNSPENT, Node, Result, Tx

## Knowledge Gaps
- **26 isolated node(s):** `install.sh script`, `FRESH`, `RECONFIRMED`, `STALE`, `PARTIAL` (+21 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **6 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `MainActivity` connect `MainActivity` to `org.junit.Test`, `android.content.Context`, `Wallet`, `GuardianService`, `Cfg`, `AuditApi`, `Node`?**
  _High betweenness centrality (0.231) - this node is a cross-community bridge._
- **Why does `Result` connect `org.junit.Test` to `MainActivity`, `GuardianService`?**
  _High betweenness centrality (0.139) - this node is a cross-community bridge._
- **Why does `Node` connect `Node` to `Wallet`, `GuardianService`, `Cfg`, `AuditApi`, `MainActivity`?**
  _High betweenness centrality (0.096) - this node is a cross-community bridge._
- **What connects `install.sh script`, `FRESH`, `RECONFIRMED` to the rest of the system?**
  _26 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `org.junit.Test` be split into smaller, more focused modules?**
  _Cohesion score 0.06582278481012659 - nodes in this community are weakly interconnected._
- **Should `android.content.Context` be split into smaller, more focused modules?**
  _Cohesion score 0.07705871980250396 - nodes in this community are weakly interconnected._
- **Should `Wallet` be split into smaller, more focused modules?**
  _Cohesion score 0.05526083112290009 - nodes in this community are weakly interconnected._