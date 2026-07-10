# DESIGN_BRIEF.md — Glidepath

A visual brief for **Claude Design**. This is deliberately shorter and less technical than CLAUDE.md — it describes what the app should look and feel like, not how it's built. Paste this in directly, or use it as a starting prompt, then iterate through chat/inline comments as normal.

If a reference asset exists (e.g. the original web prototype, `debt-runway-tracker.jsx` or a screenshot of it), attach it and say "match this visual direction" — Claude Design can read existing assets as a style reference.

---

## What we're building

**Glidepath** — a mobile app that helps someone pay off a debt or save toward a target amount, one goal at a time. You set a number (what's owed, or what you're saving toward), log payments/contributions as they happen, and watch progress build toward the goal.

**Platform:** Android phone (Material 3 conventions apply — but themed, not default Material look).

**Audience:** everyday people trying to hit a specific money goal — clearing a credit card, saving for a holiday, paying off a friend/family loan. Not a professional finance tool; not aimed at accountants or power users. Should feel encouraging and a little bit fun, not clinical or guilt-inducing.

## The core metaphor

A goal is a **runway** — you're travelling down it (debt shrinking toward zero) or climbing toward the far end (savings building toward a target). Progress is shown as a path with milestone markers at 25%, 50%, 75%, 100%, and a position marker that moves along it. This is the one signature visual element that should carry through everything else — screens can be redesigned freely, but this metaphor is the thing the app is remembered by.

## Screens needed

1. **Onboarding** — first-time setup: name the goal, choose debt or savings, set the amount, set currency, optionally set a monthly target.
2. **Goal screen (home)** — the main screen. Shows: remaining amount (big, prominent — like an odometer/counter), the runway path with markers, key stats (paid so far / remaining / projected date if a monthly target is set), and a clear way to log a new payment.
3. **Log a payment** — quick entry: amount, optional date, optional note. Should feel fast, like it takes five seconds.
4. **Payment history** — a simple list of past entries, editable/deletable.
5. **Milestone celebration** — a short, satisfying moment when 25/50/75/100% is hit. Not over the top — genuine and warm, not cartoonish.
6. **Goal complete state** — distinct from "at 100% but still on the runway screen." This should feel like an actual finish line, not just a maxed-out progress bar.
7. **Settings** — edit goal details, notification preferences, export/backup, a small optional "support the app" purchase link.
8. **Home screen widget** — goal name, remaining amount, progress bar, at a glance.

## Tone of voice

Plain, warm, encouraging — never guilt-driven or shame-based about debt. No exclamation-mark-heavy hype either. Copy should sound like a friend who's good with money, not a bank and not a motivational poster. Empty states and errors are calm and clear, never apologetic or vague.

## Visual direction (starting point — open to Claude Design's own exploration)

- Avoid generic default Material 3 purple/teal, and avoid the current AI-generated-design clichés (cream + terracotta, near-black + neon accent).
- Something in the territory of an **instrument panel meets a ledger** — a sense of tracking something real and mechanical (like an odometer counting down) paired with warmth, not cold fintech minimalism.
- The "remaining amount" number should read like a physical counter — tabular/mono numerals so digits don't jitter in width as they change.
- Support both light and dark mode properly.
- Motion should be purposeful: the runway fill and position marker animate smoothly; milestones get one genuine celebratory moment, not scattered animation throughout.

## Reference

The original concept was prototyped as a web artifact (deep pine/green background, gold accent, a mono "odometer" number, a horizontal runway with mile markers). That's a starting reference, not a constraint — the Android version is meant to be a fresh, native-feeling design in its own right, built around the same runway metaphor.
