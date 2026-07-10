# Glidepath — Android Implementation Spec

> Paste this file into your project (as `CLAUDE.md` or under `/docs`) and point Claude Code at it. It is self-sufficient: a developer who wasn't in the design conversation can build the app from this alone.

---

## 1. What we're building

**Glidepath** is an Android app that helps someone pay off a debt or save toward a target, **one goal at a time**. You set a number, log payments/contributions, and watch progress glide toward the goal.

- **Audience:** everyday people (not accountants). Encouraging, warm, a little fun — never guilt-driven.
- **Signature metaphor:** a goal is a **glide slope**. Debt is a plane *descending to touchdown* at £0; savings is a plane *climbing to cruising altitude* at the target. A little plane marker rides a curved path with milestone markers at 25/50/75/100%. **This is the one thing the app is remembered by — get it right.**

### Design files in this bundle
- `glidepath-prototype.html` — the **hi-fi interactive prototype**. Open it in a browser. It's the source of truth for look, motion, copy, and behaviour. Click the grey toolbar above the phone to jump between screens; log a payment to see the glide animate; cross a milestone to see the celebration.
- `Glidepath App.dc.html`, `Glide Card.dc.html`, `Glidepath Home Directions.dc.html` — the design source (option explorations + final app). Reference only.

**These are design references, not production code.** Recreate them natively in Kotlin/Jetpack Compose using idiomatic patterns. Fidelity is **high** — match colours, type, spacing, radii, and motion precisely.

---

## 2. Tech stack & architecture

- **Language/UI:** Kotlin + Jetpack Compose, Material 3 (**themed, not default Material** — do not ship default M3 purple/teal, and disable Dynamic Color / Material You so our palettes always win).
- **Min/target SDK:** minSdk 26, target latest stable.
- **Persistence:** **local-first, no account, fully offline.** Room for goal + payments; DataStore (Preferences) for theme choice + notification toggles + per-goal `highestMilestoneCelebrated`.
- **Notifications:** WorkManager for the monthly nudge + streak encouragement; immediate local notifications for milestone/complete.
- **Widget:** Jetpack **Glance** app widget.
- **Architecture:** single-Activity, MVVM. Compose Navigation for screen routing. Repository over Room DAO. `GlidepathViewModel` exposes a `StateFlow<UiState>`.
- **Structure:** designed for a single goal now, but model it so **multiple goals can be added later** — `Goal` is its own entity with an id; UI currently loads the single active goal (`activeGoalId` in DataStore). Don't hardcode a singleton.

---

## 3. Data model

```kotlin
enum class GoalType { DEBT, SAVINGS }

@Entity data class Goal(
    @PrimaryKey val id: Long = 0,
    val name: String,
    val type: GoalType,
    val total: Long,          // pennies. Debt = original owed; Savings = target.
    val currency: String,     // ISO code, e.g. "GBP"
    val monthlyTarget: Long,  // pennies, 0 = none set
    val createdAt: Long,
    val completedAt: Long? = null
)

@Entity data class Payment(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val goalId: Long,
    val amount: Long,         // pennies, always positive
    val date: Long,           // epoch millis
    val note: String = ""
)
```

Store money in **integer minor units (pennies)** to avoid float drift; format on display.

### Derived values (compute, never store)
```
paid       = sum(payments.amount)
remaining  = max(0, goal.total - paid)
progress   = if (total>0) min(1f, paid / total) else 0f     // 0..1
hero       = if (type==DEBT) remaining else paid            // the big number
```

### Projections (Home shows BOTH)
- **On target:** `monthsLeft = ceil(remaining / monthlyTarget)` → add to today → `"MMM yyyy"`. If no monthly target, show `—`.
- **At your pace:** `avg = mean(last up to 3 payments.amount)`; `monthsLeft = ceil(remaining / avg)` → date. If no payments, `—`.

---

## 4. Theme system (6 palettes × light/dark)

The user picks a **theme** and a **mode** (Light / Dark / System) in Settings → Appearance. **Default = Pinewood, Dark.** Persist both in DataStore. Model semantic roles, not raw M3 slots.

```kotlin
data class GlideColors(
  val bg: Color, val surface: Color, val line: Color,
  val text: Color, val muted: Color,
  val accent: Color, val onAccent: Color,
  val track: Color, val track2: Color, val accentSoft: Color, val bezel: Color
)
```
Expose via `CompositionLocal` (e.g. `LocalGlide.current`). `accentSoft` is the accent at ~14% alpha (chips, fills).

### Colour tokens (hex)

**Pinewood** (default) — Green + gold
- Dark: bg `#0D201B` · surface `#16302A` · line `#274B42` · text `#EEF4F1` · muted `#93B0A6` · accent `#E6B455` · onAccent `#1C2B12` · track `#26463D` · track2 `#3A5B52` · bezel `#050F0C`
- Light: bg `#E9F1ED` · surface `#FFFFFF` · line `#D7E4DE` · text `#14231E` · muted `#5A726B` · accent `#9C6F1C` · onAccent `#FFFFFF` · track `#DCE8E2` · track2 `#C3D4CC` · bezel `#20302B`

**Harbor** — Navy + amber
- Dark: bg `#0F1826` · surface `#182437` · line `#26344A` · text `#EEF2F8` · muted `#93A4BD` · accent `#E8B24A` · onAccent `#20180A` · track `#26344A` · track2 `#3A4A63` · bezel `#070D16`
- Light: bg `#EEF1F6` · surface `#FFFFFF` · line `#D6DDE8` · text `#141B26` · muted `#5B6678` · accent `#A9781C` · onAccent `#FFFFFF` · track `#DCE2EC` · track2 `#C3CCDB` · bezel `#1C2636`

**Oxblood** — Wine + brass
- Dark: bg `#221116` · surface `#31191F` · line `#472530` · text `#F6ECEF` · muted `#C69BA7` · accent `#E0A86A` · onAccent `#2A1A0C` · track `#3D2029` · track2 `#5A2F3B` · bezel `#160A0E`
- Light: bg `#F6EEF0` · surface `#FFFFFF` · line `#ECD9DE` · text `#2A141B` · muted `#8A6470` · accent `#A86A2C` · onAccent `#FFFFFF` · track `#EEDDE2` · track2 `#DCC3CA` · bezel `#2A141B`

**Deepsea** — Teal + coral
- Dark: bg `#0C1F20` · surface `#123032` · line `#204648` · text `#E9F4F2` · muted `#8FB3B0` · accent `#F08D6B` · onAccent `#2A140C` · track `#204648` · track2 `#315E60` · bezel `#061313`
- Light: bg `#E9F2F1` · surface `#FFFFFF` · line `#D3E4E2` · text `#0F2422` · muted `#547370` · accent `#CF6A44` · onAccent `#FFFFFF` · track `#DBE9E7` · track2 `#C0D6D3` · bezel `#12302E`

**Moss** — Olive + clay
- Dark: bg `#1A1D12` · surface `#272B1A` · line `#3C4229` · text `#F0F2E6` · muted `#A8AC92` · accent `#E9A86A` · onAccent `#2A1C0C` · track `#353A24` · track2 `#4C5334` · bezel `#0E100A`
- Light: bg `#F1F2E8` · surface `#FFFFFF` · line `#DFE1CF` · text `#1E2114` · muted `#666B52` · accent `#A67A34` · onAccent `#FFFFFF` · track `#E6E8D6` · track2 `#D0D3BC` · bezel `#22261A`

**Slate** — Graphite + sky
- Dark: bg `#14171C` · surface `#1E232B` · line `#2F3742` · text `#EEF1F5` · muted `#98A2B0` · accent `#6FB2E0` · onAccent `#0A1A26` · track `#2B333D` · track2 `#414B58` · bezel `#0A0D11`
- Light: bg `#EEF1F5` · surface `#FFFFFF` · line `#D8DEE6` · text `#171B21` · muted `#5C6672` · accent `#2F7FB8` · onAccent `#FFFFFF` · track `#DDE3EA` · track2 `#C5CDD7` · bezel `#1A1F26`

`accentSoft` = accent @ 0x29 (~16%) on dark, ~0x1F (~12%) on light.

---

## 5. Typography

Two fonts (bundle as resources; don't rely on network):
- **Hanken Grotesk** — all UI text. Weights 400/500/600/700/800.
- **Space Mono** — every **number** (the odometer, stats, keypad, dates in mono contexts), weight 700. **Use tabular / fixed-width numerals** so digits don't jitter as they change — this is a hard requirement for the counter.

Type scale (sp): hero counter 54 · screen title (Home goal name) 20 · big screen titles (History/Settings) 24 (w800) · section label 12 (w700, letterspacing 1, uppercase, muted) · body 15 · secondary 13 · caption 11–12. Milestone overlay % is 64 mono; onboarding headline 24–28 w800.

Letter-spacing: large mono numbers `-2`px equivalent (`-0.04em`); big titles `-0.5`px.

---

## 6. The runway component (signature)

A `Canvas` (or `Path` + `drawPath`) inside a 300×150dp box.

**Curve (cubic Bézier), normalized (x,y) with y pointing down, ×(300,150):**
- **DEBT (descending):** `M (0.02,0.147) C (0.40,0.20) (0.633,0.613) (0.98,0.853)`
- **SAVINGS (ascending):** `M (0.02,0.853) C (0.367,0.813) (0.60,0.40) (0.98,0.147)`

**Layers (back → front):**
1. **Area fill** under the curve → baseline, colour `accentSoft`.
2. **Full path** stroke `track2`, width 3dp, round caps (the runway ahead).
3. **Traveled path** stroke `accent`, width 3dp, round caps, drawn from start for `progress` of its length. Use `PathMeasure.getSegment(0, progress*length, …)`.
4. **Milestone dots** at t = 0.25 / 0.5 / 0.75 (Ø8dp) and 1.0 (11dp rounded square). Colour = `accent` if `progress ≥ t` else `track2`. Position via `PathMeasure.getPosTan`.
5. **Plane marker** — a small triangle (Ø ~14dp, points along travel). Position = `getPosTan(progress*length)`; **rotate the triangle to the tangent angle** so it banks. Drop shadow.
6. Dashed **ground line** at the baseline (y=128/150), `track2`, ~50% alpha.

**Labels under it:** left `Start` / `Ground`; centre `NN% down` / `NN% up` (accent); right `Touchdown · £0` / `Goal · £8,500`.

**Animation:** on data change, animate `progress` to its new value over **900ms, EaseOutCubic** (`cubic-bezier(.22,1,.36,1)`). The traveled stroke and plane position both interpolate off the same animated `progress`. The hero number animates in parallel (see below).

---

## 7. Screens (all 8)

Phone canvas in the mock is 360×760. Content padding 22dp horizontal. Cards: `surface`, radius 16dp. Primary button: full-width, `accent` bg, `onAccent` text, radius 18dp, padding 17dp, weight 700, 16sp. Bottom nav (Home/History/Settings) sits above a gesture pill; icons are 22dp rounded squares, active tint `accent`.

### 7.1 Onboarding (first run + "Edit goal")
Multi-step, progress dots (3). Steps: **Welcome** (plane on a dashed climb, "One number, one runway.", CTA "Set up your goal") → **Details** (name `TextField`; type choice = two selectable cards "Paying down" / "Saving up") → **Amount** (currency chip that opens a currency bottom sheet; big mono amount; custom number keypad) → **Monthly pace** (optional, keypad, "Skip for now"; finish button "Start gliding"). Edit-goal reuses steps 2–4 prefilled, skips Welcome, finish label "Save changes", and **keeps existing payments** (fresh onboarding wipes payments + resets milestone tracking).

### 7.2 Home
Header (goal name + "Paying down"/"Saving toward" + DEBT/SAVINGS chip). **Hero odometer** (`£` accent + mono value). Sub line ("of £8,500 to touchdown" / "£3,240 to £8,500"). **Runway** (§6). Two stat cards: "Paid so far / Saved so far" and "On target" (with "at your pace · …" secondary). Full-width CTA "Log a payment" / "Add to savings".

### 7.3 Log a payment (fast — ~5s)
Header (Cancel / title). Big mono amount (muted `0` until typed). Hint. **Quick chips** £50 / £100 / monthly-target. Date field ("Today") + optional Note `TextField`. **Custom 3×4 keypad** (1–9, blank, 0, ⌫), keys = `surface` tiles 52dp, mono 22sp. Confirm button (50% opacity until amount > 0). On confirm: insert payment, recompute, return Home, then run milestone check.

### 7.4 Payment history
Title "History". Two stat cards (total paid/saved, entries count). List rows: leading 38dp rounded-square icon (`accentSoft`/`accent`, "↓"), `+ £450` headline, `date · note` sub (ellipsize), "Delete" action. Empty state: calm ("No payments yet. Your first one starts the glide."). Deleting recomputes progress (does **not** re-trigger past milestones).

### 7.5 Milestone celebration (full-screen takeover, restrained)
Triggered when progress crosses 25/50/75/100% for the first time (track highest celebrated per goal). Full-bleed `bg` overlay: a mini runway where **the plane animates past the marker** (a pulsing ring at the marker), the big mono `NN%`, a warm title + one line, and "Keep going". Copy: 25 "A quarter of the way / The hardest part is starting, and you already have." · 50 "Halfway there / …the far end is closer than the start now." · 75 "Three quarters down / Home stretch. You can see the runway from here." · 100 routes to Goal complete. Enter with a gentle pop (450ms, slight overshoot). **One celebratory moment — no scattered animation elsewhere.** Fire the milestone notification here if enabled.

### 7.6 Goal complete (distinct finish line)
Not just 100% on the runway — a dedicated screen: plane at a **checkered finish flag**, restrained confetti (few accent/track2 pieces falling), "Touchdown" eyebrow, "{goal} is paid off" / "{goal} — fully funded", warm message, two stats (total paid/saved, payments count), CTAs "Start a new goal" (→ onboarding) and "Back to my goal". Set `goal.completedAt`.

### 7.7 Settings
Title "Settings". **Your goal** card (name + "Debt · £8,500 · £450/mo") → opens edit. **App** group: Appearance row (shows "Pinewood · Dark" →), and three toggles — **Milestone alerts**, **Monthly nudge**, **Keep-it-up encouragement** (Material switch, `accent` when on). **Data** group: "Back up your data — Export everything to a file (JSON)". A soft **support card** (`accentSoft`): "Glidepath is free, and stays that way… No pressure, no locked features." + "Support the app" button (opens Play billing / one-off IAP). Footer: "Glidepath v1.0 · made local-first, no account needed".

### 7.8 Appearance (sub-screen of Settings)
Back chevron + "Appearance". **Mode** segmented control (Light / Dark / System). **Theme** 2-column grid of 6 tiles: each tile has a mini swatch (bg field with accent + surface dots), name + descriptor, and a check + 2px accent ring when selected. Selecting re-themes the whole app immediately.

### 7.9 Home-screen widget (Glance)
Card (`surface`, radius 24, `line` border): app dot (accent) + goal name + "NN% down/up"; big mono hero number; sub line; thin progress bar (`track` bg, `accent` fill = progress). Support 4×1 and 4×2. Tapping opens the app. Update on payment changes.

---

## 8. Interactions, motion & state

- **Odometer:** `animateIntAsState` (or manual) over **900ms EaseOutCubic** whenever hero changes. Mono tabular figures.
- **Runway:** progress animation 900ms EaseOutCubic (§6).
- **Milestone overlay:** pop-in 450ms with mild overshoot; the pass-the-marker plane ~1.6s ease; marker ring pulse loops.
- **Screen transitions:** quick rise/fade (~350–400ms). Keep subtle.
- **Reduced motion:** honour `Settings.Global.ANIMATOR_DURATION_SCALE` / accessibility — when reduced, **snap to final states** (no glide tween, no confetti, no pulsing). The prototype does this via `prefers-reduced-motion`.
- **UiState (ViewModel):** `goal`, `payments`, `paid/remaining/progress/hero` (derived), `theme{palette, mode}`, `notif{milestone,monthly,streak}`, transient `overlayMilestone: Int?`, `toast: String?`, plus per-screen entry/onboarding form state. Milestone detection compares new progress against `highestMilestoneCelebrated` (DataStore); 100 → set complete + route.
- **Overpayment:** allow logging more than remaining; clamp `progress` at 1.0, `remaining` floors at 0, and it triggers the 100% / complete flow.

---

## 9. Currency

Support a **currency picker** (symbol + formatting only, **no exchange rates**). Codes/symbols used: GBP `£`, USD `$`, EUR `€`, JPY `¥`, AUD `A$`, CAD `C$`, INR `₹`. Format with grouping separators, no decimals for whole amounts (pennies → display rounded). Symbol precedes the number; in the hero the symbol is smaller and `accent`-coloured.

---

## 10. Notifications (all opt-in-able; defaults: milestone ON, monthly ON, streak OFF)
- **Milestone reached** — fired at the moment a threshold is crossed.
- **Goal completed** — on reaching 100%.
- **Monthly nudge** — WorkManager periodic; gentle reminder to log progress.
- **Keep-it-up / streak** — when the user maintains a steady payment rhythm.
Copy stays warm and non-nagging. Never shame missed payments.

## 11. Export / backup (JSON, full restore)
Single JSON via the Android share sheet (SAF). Suggested shape:
```json
{ "version": 1, "exportedAt": 0,
  "goal": { "name":"", "type":"DEBT", "total":850000, "currency":"GBP", "monthlyTarget":45000, "createdAt":0 },
  "payments": [ { "amount":45000, "date":0, "note":"" } ],
  "settings": { "palette":"pine", "mode":"dark", "notifications":{ "milestone":true, "monthly":true, "streak":false } } }
```
Amounts in pennies. Import restores everything.

## 12. Accessibility
- Tabular numerals on all changing numbers.
- Touch targets ≥ 48dp (keypad keys, toggles, list actions).
- Respect system font scaling (use sp, avoid fixed heights on text).
- Verify contrast for both modes across all 6 palettes (muted-on-bg and accent-on-bg were tuned for this; re-check if you alter hexes).
- Honour reduced-motion (§8). Content-describe the plane/progress ("62% paid off").

## 13. Suggested build order
1. Theme system (GlideColors + 6 palettes + CompositionLocal + DataStore) & fonts.
2. Room (Goal, Payment) + repository + ViewModel/UiState.
3. Runway composable (§6) + odometer.
4. Home → Log → History (the core loop).
5. Onboarding (+ edit-goal reuse) + currency sheet.
6. Milestone overlay + Goal complete.
7. Settings + Appearance.
8. Notifications (WorkManager) + JSON export.
9. Glance widget.
10. Reduced-motion + accessibility pass.
