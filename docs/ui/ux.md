# UI / UX Design System

**GOVINDA** pairs a crisp engineering-blueprint aesthetic with full dynamic theming via `GovindaDesignTokens` (`LocalGovindaTokens`).

---

## 1. Visual Identity & Dynamic Token Architecture

All composables read styling exclusively from `MaterialTheme` and `LocalGovindaTokens.current` rather than hardcoding hex colors:

| Token | Blueprint Default | Role |
| :--- | :--- | :--- |
| `primary` | `#003DA5` | Mathematical operators, primary CTAs, active borders |
| `secondary` | `#1647BB` | Secondary highlights and accents |
| `background` | `#FFFFFF` | Primary canvas & equation card interior |
| `surface` | `#F7F9FE` | Top app bars, docked keypad deck, dialog panels |
| `selectedState` | `#EAF1FF` | Active chips, selected app rows, backspace tile |
| `border` | `#A9C1EF` | High-clarity structural outlines (`1.5.dp – 2.dp`) |
| `primaryText` | `#12336F` | High-contrast numerals, prompts, and headings |
| `secondaryText` | `#6480B6` | Captions, metadata, helper descriptions |

26 built-in themes (12 Light, 14 Dark) are validated to maintain **>= 4.5:1 WCAG AA contrast** between `primaryText` and `background`. High-Contrast accessibility mode overrides to pure `#000000` / `#FFFFFF`.

---

## 2. Screen Architecture & Navigation

- **Minimal Navigation**: Single top bar on Home with logo mark, `GOVINDA` wordmark, user avatar, and a single `48.dp+` Settings button—no bottom navigation bar, tabs, or distractor chrome.
- **Home (`ChallengeScreen`)**:
  - **Compact Portrait**: Centered mathematical equation card with operator highlighting (`+`, `−`, `×`, `÷`, `%`, `√`, `∛`, `^`, `=`, `□`) above a docked bottom answer display + 4×4 keypad.
  - **Wide / Tablet / Split-Panel (`>= 700.dp`)**: Two-column side-by-side layout placing the question & explanation deck on the left and the keypad on the right.
- **Settings (`SettingsScreen`)**:
  - Modular blueprint cards constrained to `max = 720.dp` width on large screens:
    1. Personalization Studio Card
    2. App Quiz Lock Card
    3. Adaptive Mechanism Card (with live telemetry & parameter chips)
    4. Question Difficulty Card
    5. Permissions & Access Card (interactive disclosure dialogs)
    6. Local Data Portability Card (Export JSON / Reset)
    7. Help & Support Card (Help Guide, FAQ, About GOVINDA)
    8. Legal & Licenses Card (Privacy Policy, Terms & Conditions, Open-Source Licenses)

---

## 3. Accessibility & Ergonomics

- **Touch Targets**: All interactive buttons, chips, switches, and settings rows enforce `>= 48.dp` height/size (`54.dp–62.dp` when Extra Large Targets is enabled).
- **Typography**: Supports `0.85x – 1.35x` font scaling and tabular numerals (`fontFeatureSettings = "tnum"`) so digits stay aligned while typing.
- **Motion Respect**: Animation durations scale smoothly (`1.0x`, `0.5x`, `0.0x` Reduced Motion).
- **Edge-to-Edge**: Uses `enableEdgeToEdge()` with `WindowInsets.statusBars` and `WindowInsets.navigationBars` padding on top bars and docked decks.
