# CLAUDE.md — Glidepath

Project instructions for Claude Code. Read this in full before writing code. Keep it updated as decisions change — this file is the source of truth for how this project is built, not just what it is.

## What this is

**Glidepath** is a native Android app that helps someone pay off a debt or save toward a target amount, one goal at a time. You set a number (what you owe, or what you're saving toward), log payments/contributions as they happen, and the app tracks progress toward zero (debt) or toward the target (savings) with a visual "runway" — a path with milestone markers you move along.

It started as a personal tool for one person's debt payoff and is being generalised into a real app, potentially for Play Store release. Build it to that standard: clean architecture, no throwaway shortcuts, but don't over-engineer for scale this app will never need (it's a single-user, local-first, single-active-goal app).

## Tech stack

- **Language:** Kotlin
- **UI:** Jetpack Compose (Material 3 as a base, but themed — see Design System below, do not ship default Material look)
- **Architecture:** MVVM. `ViewModel` + `StateFlow` for UI state. Repository pattern between ViewModel and data layer.
- **Local persistence:** Room (SQLite). No cloud, no backend, no login.
- **Async:** Kotlin Coroutines + Flow throughout. No callbacks, no RxJava.
- **DI:** Hilt. Keep modules small and obvious.
- **Widget:** Glance (Jetpack Glance for Compose-based widgets), not legacy RemoteViews, unless Glance proves insufficient for a requirement.
- **Notifications:** WorkManager for scheduled local reminders. No push infra needed — everything is local.
- **Billing:** Play Billing Library v6+, for a single one-time non-consumable "Support Glidepath" purchase. No subscriptions, no ads, ever.
- **Min SDK:** 26 (Android 8.0) unless a required library forces higher — flag it if so.
- **Target/compile SDK:** latest stable at time of writing.
- **Testing:** JUnit + Turbine for Flow testing on ViewModels/repository logic. Compose UI tests for critical flows only (create goal, log payment, milestone trigger) — don't chase 100% UI test coverage.

## Package structure

```
com.glidepath.app/
  data/
    local/          Room database, DAOs, entities
    repository/      Repository implementations, interfaces
    export/          Backup/export/import logic (JSON file)
  domain/
    model/           Plain Kotlin domain models (not Room entities)
    usecase/         Small, single-purpose use cases if logic doesn't fit cleanly in repository
  ui/
    theme/           Color, type, shape — the Glidepath design system
    goal/            Main goal/progress screen (the "runway")
    logpayment/       Add payment/contribution flow
    history/         Payment log list
    settings/        Currency, target amount, edit goal, export/import, notification prefs
    onboarding/      First-run: create your first goal
    components/       Shared composables (RunwayPath, Odometer, MilestoneBanner, StatCard)
  widget/            Glance widget: goal name, progress, remaining/target figure
  notifications/     WorkManager workers + notification builder
  billing/           Play Billing client wrapper for the support purchase
  MainActivity.kt
  GlidepathApp.kt    Application class, Hilt entry point
```

## Data model (core)

A single **active Goal** at a time (multiple goals may come later — don't block it, but don't build UI for it now):

```
Goal
  id: Long
  name: String                 // e.g. "Credit card payoff", "Japan trip fund"
  type: GoalType                // DEBT or SAVINGS
  startingAmount: BigDecimal     // the debt owed, or the savings target
  currentAmount: BigDecimal      // derived from contributions, but may be cached
  currencyCode: String           // ISO 4217, e.g. "GBP" — set once in Settings
  monthlyTarget: BigDecimal?     // optional, drives ETA projection
  createdAt: Instant
  isComplete: Boolean

Contribution
  id: Long
  goalId: Long
  amount: BigDecimal
  date: Instant
  note: String?
```

Use `BigDecimal` for all money values — never `Float`/`Double`. Store as string or minor units (integer pence/cents) in Room, not as floating point.

For a **DEBT** goal: progress = contributions paid toward `startingAmount`; "remaining" shrinks toward zero.
For a **SAVINGS** goal: progress = contributions toward `startingAmount` (the target); "remaining" is the gap still to save.
The runway visual, odometer, and stats reuse the same components for both — only labels and one or two icons/copy strings differ. Don't fork the UI into two separate screens.

## Design system

This is a **fresh, native-Android-specific design**, not a port of the web prototype. Brief for whoever designs/implements the theme:

- **Concept:** aviation/runway metaphor — a goal is a runway you're travelling down (debt) or climbing toward (savings), with milestone markers at 25/50/75/100%. Keep the runway/path visual as the one signature element carried over from the original web version; everything else should be redesigned for Android Material 3 conventions (dynamic color support optional, but ship a strong default theme regardless).
- **Do not** default to generic Material 3 baseline purple/teal, and don't reuse cliché AI-generated palettes (cream + terracotta, near-black + neon accent). Pick a real palette and justify it — something in the "instrument panel meets ledger" space fits the metaphor (deep panel colors, one warm metallic accent for progress/gold, a second accent for celebration moments).
- **Typography:** a distinct display face for the big remaining-amount "odometer" number (tabular/mono numerals matter — the number must not visually jitter in width as digits change), a clean body face for everything else. Respect Android's dynamic type / accessibility font scaling — do not hardcode text that breaks with larger system font sizes.
- **Motion:** the runway fill and runner marker should animate smoothly on progress change. Milestone hits (25/50/75/100%) get a short celebratory moment — respect the system "reduce motion" accessibility setting by falling back to a simple fade/checkmark instead of a full animation.
- Support light and dark mode properly — don't assume dark-only.

## Features (v1 scope)

1. **Onboarding:** first launch → create a goal (name, type debt/savings, amount, currency, optional monthly target).
2. **Goal screen (the Runway):** odometer showing remaining/progress, runway path with milestones, quick "log a payment" action, key stats (paid so far / remaining / projected date if monthly target set).
3. **Log payment/contribution:** amount + optional date + optional note. Updates goal instantly.
4. **Payment history:** list of all logged contributions, editable/deletable.
5. **Milestone celebrations:** in-app banner/animation at 25/50/75/100%, respecting reduced-motion.
6. **Settings:** edit goal amount/target/currency, notification preferences, export/import data, "Support Glidepath" one-time purchase, reset/delete goal.
7. **Notifications:** local reminders (daily or weekly, user's choice) nudging the user to log a payment if they haven't recently. No reminder spam — respect a quiet setting and let the user turn it off entirely.
8. **Home screen widget:** shows goal name, remaining amount, and progress bar. Tapping opens the app.
9. **Export/Backup:** export all data to a JSON file (Storage Access Framework, user picks location); import restores it. This is the only "cloud-adjacent" feature — it's a manual file, not sync.
10. **One-time support purchase:** a single non-consumable IAP ("Buy me a coffee" style), entirely optional, gates nothing.

Out of scope for v1 (don't build, but don't architect against them either): multiple simultaneous goals, cloud sync/accounts, multi-currency per goal, social/sharing features, ads.

## Coding conventions

- Prefer immutable data classes and `sealed interface`/`sealed class` for UI state (`GoalUiState`) over booleans-and-nullables soup.
- ViewModels expose a single `StateFlow<UiState>` per screen where practical, plus one-off `Channel`/`SharedFlow` for events (snackbars, navigation).
- No business logic in Composables. Composables read state and dispatch events; ViewModels/use cases decide what happens.
- Every public function/class gets a short KDoc if its purpose isn't obvious from the name.
- Write tests alongside the code that needs them, not as an afterthought pass at the end — but don't let test-writing block visible progress on core screens early on.
- Commit in small, working increments. Don't let uncommitted work pile up across an entire phase of PLAN.md.

## What "done" looks like for any task

Before considering a task complete: it builds, it runs on a device/emulator, dark mode doesn't break it, rotating/resizing doesn't crash it, and money values are never displayed or stored as raw floats.

## Working notes

Use this section to jot down decisions made mid-build that aren't captured above (e.g. "chose Glance over RemoteViews because X", "min SDK bumped to 28 because of Y"), so future sessions don't re-litigate settled choices.
