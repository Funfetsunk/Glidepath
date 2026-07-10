# PLAN.md — Glidepath Build Plan

A phased plan for building Glidepath with Claude Code. Work through phases in order — each one should leave the app in a runnable state. Check items off as completed; add a short note if a decision deviates from CLAUDE.md, and update CLAUDE.md's "Working notes" section when that happens.

Do not start a phase until the previous one builds and runs cleanly.

---

## Phase 0 — Project setup

- [ ] Create new Android Studio project: Kotlin, Jetpack Compose, package `com.glidepath.app`, min SDK 26.
- [ ] Add dependencies: Compose BOM, Material 3, Hilt, Room, WorkManager, Glance, Play Billing, Turbine (test).
- [ ] Set up Hilt (`GlidepathApp`, `@HiltAndroidApp`).
- [ ] Set up Git repo, `.gitignore` for Android/Gradle, initial commit.
- [ ] Confirm empty app builds and runs on an emulator.

**Exit condition:** blank app launches, shows a placeholder screen, no crashes.

## Phase 1 — Data layer

- [ ] Define domain models: `Goal`, `GoalType`, `Contribution` (per CLAUDE.md data model — `BigDecimal`/minor-units for money, never Float/Double).
- [ ] Room entities + DAOs for `Goal` and `Contribution`.
- [ ] Repository interface + implementation: create goal, get active goal (as `Flow`), add/edit/delete contribution, mark goal complete.
- [ ] Unit tests: repository logic, especially remaining-amount and percent-complete calculations for both DEBT and SAVINGS goal types.

**Exit condition:** repository tests pass; can create/read/update goal + contributions from a test harness (no UI needed yet).

## Phase 2 — Core UI: onboarding + the Runway screen

- [ ] Build the Glidepath theme (colors, type, shapes) per the Design System brief in CLAUDE.md — this is the one phase where visual direction should be proposed and reviewed before wiring up every screen, since it sets the tone for everything after.
- [ ] Onboarding flow: create first goal (name, type, amount, currency, optional monthly target).
- [ ] Goal screen: odometer (remaining amount), runway path with 25/50/75/100 markers + runner position, key stats row (paid so far / remaining / projected date).
- [ ] "Log a payment" quick action (bottom sheet or dedicated screen) wired to the repository.
- [ ] Milestone celebration banner, with reduced-motion fallback.
- [ ] Light + dark mode both verified.

**Exit condition:** a real goal can be created, a payment logged, progress visibly updates, and a milestone triggers the banner at least once in manual testing.

## Phase 3 — History, settings, edit/delete

- [ ] Payment history screen: list all contributions, edit/delete individual entries, goal figures recalculate correctly after edits.
- [ ] Settings screen: edit goal name/amount/target/currency, delete/reset goal, link out to notification prefs and export/import (built in later phases, can stub the entry points now).
- [ ] Handle the "goal complete" state distinctly (per CLAUDE.md's `isComplete` flag) — a clear, satisfying completed-state screen, not just a runway stuck at 100%.

**Exit condition:** full CRUD on goal + contributions works from the UI; completing a goal shows a distinct state.

## Phase 4 — Notifications

- [ ] WorkManager periodic worker for reminders (daily or weekly, per user setting).
- [ ] Notification permission handling (Android 13+ runtime permission).
- [ ] Settings toggle: on/off, frequency choice.
- [ ] Reminder should be skippable/smart — e.g. don't nag if a payment was logged in the last day, per CLAUDE.md's "no reminder spam" requirement.

**Exit condition:** reminder fires on schedule on a test device/emulator, respects the on/off and frequency setting, and suppresses itself appropriately after a recent log.

## Phase 5 — Home screen widget

- [ ] Glance widget: goal name, remaining amount, progress bar.
- [ ] Widget updates when goal data changes (not just on a timer).
- [ ] Tapping widget opens the app to the Goal screen.
- [ ] Handle the empty state (no goal yet) gracefully in the widget.

**Exit condition:** widget can be added to home screen, reflects real data, and stays in sync after logging a payment in-app.

## Phase 6 — Export / backup

- [ ] Export: serialize goal + all contributions to JSON, write via Storage Access Framework (user picks save location).
- [ ] Import: pick a file, validate/parse, restore state (with a clear warning if this will overwrite an existing goal).
- [ ] Test the round trip: export, delete app data, import, confirm identical state.

**Exit condition:** export-then-import round trip is verified to restore identical goal + contribution data.

## Phase 7 — Billing (support purchase)

- [ ] Integrate Play Billing Library, single non-consumable product ("Support Glidepath").
- [ ] Simple settings-screen entry point; purchase confirmation; handle already-purchased state gracefully (don't show the option once bought, or show a thank-you state).
- [ ] Verify this gates nothing — app is fully functional without ever purchasing.

**Exit condition:** test purchase flow completes in a licensed test track; already-purchased state persists correctly.

## Phase 8 — Polish & pre-launch

- [ ] Accessibility pass: font scaling, screen reader labels on key elements (odometer, log payment button, milestone banner), reduced-motion fallback verified.
- [ ] Empty/error states reviewed (no goal yet, invalid input on log payment, import failure, etc.) per the interface-copy guidance in CLAUDE.md — clear, plain, no blame.
- [ ] App icon, splash screen, Play Store listing assets (screenshots, short/long description, privacy policy page — required even for a no-account app that collects no data, since it touches billing).
- [ ] Manual test pass on at least two screen sizes / one tablet if feasible.
- [ ] Internal test track release on Play Console before any public release.

**Exit condition:** app is feature-complete, polished, and manually verified — ready for release builds, but not yet submitted anywhere.

---

## Phase 9 — Publish (do not start automatically)

**This phase is gated. Do not begin any task in this phase unless the user has explicitly said they're ready to publish.** Completing Phase 8 is not itself permission to proceed — wait for an explicit instruction such as "let's publish" or "start Phase 9" in a session before touching signing, store listings, or submission.

- [ ] Generate a release keystore via `keytool` (command-line, no Android Studio required); store it and its passwords somewhere safe outside the repo (never commit a keystore or its credentials to Git).
- [ ] Configure Gradle signing config to reference the keystore via environment variables or a local, gitignored `keystore.properties` file.
- [ ] Build the signed release bundle: `./gradlew bundleRelease`.
- [ ] Sanity-check the release build on a physical device (`bundletool` or `adb install` of a generated APK) before uploading anywhere.
- [ ] Create the Google Play Console account/app listing (one-time $25 developer fee, done in-browser).
- [ ] Write the store listing: short/long description, screenshots, feature graphic, privacy policy page (required — the app touches billing even though it collects no personal data).
- [ ] Complete Play Console's data safety and content rating questionnaires.
- [ ] Upload the signed AAB to the **internal testing track** first — never straight to production.
- [ ] Test the internally-distributed build end to end (including the real billing flow) before considering promotion to a public track.
- [ ] Only once the user confirms the internal test is good: promote to production release.

**Exit condition:** app is live on Play Store, or deliberately parked at whichever track the user chose to stop at.

---

## Notes for whoever (human or Claude Code session) picks this up

- Re-read CLAUDE.md before resuming work in a new session — it holds the architectural decisions this plan assumes.
- If a phase reveals that an earlier decision doesn't hold up (e.g. Glance can't do something needed, min SDK needs bumping), fix it at the source in CLAUDE.md rather than working around it silently in code.
- It's fine to reorder Phase 4/5/6/7 relative to each other — they're independent of each other and of Phase 1-3, which must come first.
- Phase 9 is intentionally separate from Phase 8 and gated behind explicit user instruction — see Phase 9's note before starting any of it.
