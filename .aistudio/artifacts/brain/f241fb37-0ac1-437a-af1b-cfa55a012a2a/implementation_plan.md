# MATHGATE Chat Studio — WhatsApp Feature Completion Plan

This plan completes the missing WhatsApp feature sets you selected—**Core & Media Polish**, **Group Admin, Events with RSVP & `@Mentions`**, and **Privacy & Security (Chat Lock PIN, Disappearing Messages, Block/Report)**—while keeping the clean single **Chats** view with header drawers for Status Updates & Call Logs in the platform's blue-and-white theme.

## User Review & Critical Decisions

> [!IMPORTANT]
> The following product and UX choices were confirmed during clarification and govern this implementation:

- **Confirmed Feature Scope**:
  1. **Core & Media Polish**: Inline rich-text formatting (`*bold*`, `_italic_`, `~strikethrough~`, `` `monospace` ``), live typing & last-seen indicators, pre-send media caption & preview dialog, **View Once** self-destructing media, automatic URL link preview cards, timed **Live Location** sharing, and a per-chat **Media / Docs / Links Gallery**.
  2. **Group Admin, Events with RSVP & `@Mentions`**: Interactive **Group Info & Admin Drawer** (add/remove participants, promote/demote admins, toggle *"Only admins can send messages"*, shareable invite link & group rules), **Group Events** card with **Going / Maybe / Can't Go** RSVP tracking (accessible right from the WhatsApp 2x3 attachment tray's **Event** icon), and `@member` autocomplete mentions when typing `@`.
  3. **Privacy & Security**: **Chat Lock** (PIN-protected **"Locked Chats"** folder at the top of the Chats list), per-chat **Disappearing Messages** timer (`Off`, `24h`, `7d`, `90d`), **Block & Report Contact** controls, and a per-chat **Security Verification Code** sheet.
- **Confirmed Navigation Structure**: Keep the streamlined single **Chats** view as the primary workspace, with lightweight header drawers/sheets for **Status Updates** and **Call History** rather than adding extra bottom navigation bars.

---

## 1. Overview & Core Concept

- **What It Does**: Elevates the standalone Chat workspace into a complete WhatsApp-grade messaging experience tailored for technical and collaborative communication, operating offline-first on local SQLite/Room storage without requiring internet permissions.
- **Target Audience / Persona**: Learners and engineers using GOVINDA (MATHGATE) who enable the **Chat** workspace in Settings and expect familiar WhatsApp ergonomics, rich media controls, study group management, event scheduling, and conversation privacy.
- **Key Value**: Brings all essential WhatsApp interactions—rich text, View Once media, link cards, group admin permissions, event RSVPs, `@mentions`, and PIN-locked chats—into one cohesive, responsive interface that matches the platform's blueprint aesthetic.

---

## 2. User Experience & Visual Design

### Key User Flows
1. **Chats Home & Locked Chats Flow**:
   - At the top of the **Chats** list, users can pull or tap the **"Locked Chats"** row (when any conversation is locked) and enter their 4-digit Chat Lock PIN to reveal private threads.
   - Header action icons open quick drawers for **Status Updates** (24h text/formula status rings), **Call Log History**, **QR / 6-Digit OTP Pairing**, and **New Chat / Group**.
2. **Rich Text, `@Mentions` & Pre-Send Media Preview Flow**:
   - Typing `*bold*`, `_italic_`, `~strike~`, or `` `code` `` renders styled typography inside message bubbles.
   - Typing `@` in a group chat pops up a floating member selector pill bar above the composer to insert `@Arjun`, `@Priya`, etc.
   - Selecting a photo/video or tapping the **Pre-Send Caption & View Once** toggle lets the user attach a caption and mark the media as **View Once (1️⃣)** before sending. Tapping a View Once bubble opens it once and transitions its state to *"Opened"*.
3. **WhatsApp 2x3 Attachment Tray & Group Event RSVP Flow**:
   - Tapping `+` or the Paperclip inside the pill composer expands the 2x3 attachment grid (**Photos**, **Camera**, **Location / Live Location**, **Document**, **Poll**, **Event**, plus **Formula/Code**).
   - Selecting **Event** opens a Group Event creator (Title, Date/Time, Description, Location link) and posts an interactive **Event Card** in the chat where members can tap **Going**, **Maybe**, or **Not Going**.
4. **Contact / Group Info, Media Gallery & Privacy Flow**:
   - Tapping the top conversation header (Avatar / Name) opens the **Contact & Group Info Sheet**, containing:
     - **Media, Links & Docs** tabs browsing all shared items in that chat.
     - **Disappearing Messages** selector (`Off`, `24 Hours`, `7 Days`, `90 Days`).
     - **Chat Lock** toggle (protects the conversation with a 4-digit PIN) & **Encryption / Security Code Verification** card.
     - **Group Admin Controls** (add/remove members, toggle admin role, toggle *"Only admins can send"*, copy group invite link).
     - **Block & Report** actions at the bottom of the sheet.

### Visual Identity & Theme
- **Aesthetic Direction**: Modern WhatsApp spatial layout paired with MATHGATE's crisp technical blueprint palette.
- **Color Palette & Mood**:
  - Primary Accent (`#003DA5`): Header actions, unread badges, active filter pills, outgoing double-checkmarks (`✓✓`), and the circular Mic/Send button.
  - Secondary Accent (`#1647BB`): Online/typing status, event badges, and link preview accents.
  - Outgoing Bubble Surface (`#DCE8FF`) & Incoming Bubble Surface (`#FFFFFF`) over a subtle patterned `#F3F7FE` chat canvas.
  - Destructive / Security Alert (`#D93025`) for Block, Report, and Delete actions.
- **Typography & Hierarchy**: Bold `28sp` "Chats" display header, `16sp` conversation titles, `14.5sp` rich-formatted message body text (supporting bold, italic, strikethrough, and monospace spans), and `11sp` inline timestamps.

---

## 3. Key Product Decisions & Trade-Offs

- **Decision 1: Single Chats View with Header Drawers & Tappable Contact/Group Info Header**
  - *Chosen Approach*: Keep the clean WhatsApp **Chats** list and **Conversation Thread** as the two primary views, placing **Group Info / Media Gallery / Disappearing / Chat Lock / Block** inside the conversation header sheet (triggered by tapping the contact/group title bar, just like WhatsApp) and **Status Updates** in a header drawer.
  - *Why*: Preserves the clean layout from your reference screenshot without cluttering the screen with extra navigation bars.
- **Decision 2: Local-Only SQLite Persistence for Events, RSVPs, View Once & Chat Lock**
  - *Chosen Approach*: Store all new states (View Once opened flags, Disappearing timer settings, Locked chat flags & PIN hash, Blocked states, Group Events & RSVPs, and Group Admin permissions) in the local Room database.
  - *Why*: Guarantees instant, deterministic behavior and 100% offline compliance without requiring `INTERNET` permissions.

---

## 4. Technical Architecture & Data Strategy *(Technical Reference)*

### Architecture & Component Diagram
```
┌───────────────────────────────────────────────────────────────────────────┐
│                         MATHGATE Chat Studio UI                           │
│                                                                           │
│  ┌──────────────────────────────┐     ┌────────────────────────────────┐  │
│  │     Chats Home List Pane     │     │   WhatsApp Conversation Pane   │  │
│  │  • Header: Status Drawer,    │     │  • Tappable Header -> Opens    │  │
│  │    QR/OTP, Camera, + FAB     │────▶│    Contact / Group Info Sheet  │  │
│  │  • Locked Chats Folder (PIN) │     │    (Media/Docs/Links, Admin,   │  │
│  │  • Pill Search & Filters     │     │     Disappearing, Lock, Block) │  │
│  │  • Rows: Typing / Last Seen, │     │  • Bubbles: Rich Text Parser,  │  │
│  │    ✓✓ Preview, Unread Badge  │     │    Link Preview, View Once,    │  │
│  └──────────────────────────────┘     │    Live Location, Event RSVP   │  │
│                                       │  • Composer: @Mention Popup,   │  │
│                                       │    Pre-Send Caption/ViewOnce,  │  │
│                                       │    2x3 Attachment Tray (Event) │  │
│                                       └────────────────────────────────┘  │
└───────────────────────────────────────────────────────┬───────────────────┘
                                                        │
                                                        ▼
┌───────────────────────────────────────────────────────────────────────────┐
│                   ChatViewModel & ChatRepository Layer                    │
│  • RichText & URL Parser (*bold*, _italic_, ~strike~, `code`, http://)    │
│  • Group Event & RSVP Manager (Going / Maybe / Not Going)                 │
│  • Group Member & Admin Permission Controller                             │
│  • Privacy Engine (Chat Lock PIN, Disappearing Timer, Block/Report,       │
│    View Once consumption, Live Location countdown, Status Updates)        │
└───────────────────────────────────────────────────────┬───────────────────┘
                                                        │
                                                        ▼
┌───────────────────────────────────────────────────────────────────────────┐
│                    Local Room Database (GovindaDatabase)                  │
│  • ConversationEntity (+ isLocked, disappearingHours, isBlocked,          │
│    onlyAdminsCanSend, inviteLink, lastSeenText)                           │
│  • MessageEntity (+ isViewOnce, isViewOnceOpened, linkPreviewUrl/Title,   │
│    eventPayloadJson, liveLocationExpiresAt)                               │
│  • ConversationMemberEntity (role: ADMIN / MEMBER)                        │
│  • ChatSettingsEntity (+ chatLockPin)                                     │
└───────────────────────────────────────────────────────────────────────────┘
```

### Data Model & Interactive State Mapping
1. **Rich Text, Link Previews & View Once (`MessageEntity`)**:
   - Inline parser converts `*...*`, `_..._`, `~...~`, and `` `...` `` into `AnnotatedString` spans inside every message bubble and automatically extracts URLs to render a compact **Link Preview Card** above the message text.
   - `isViewOnce` and `isViewOnceOpened` fields on `MessageEntity` render a WhatsApp-style **1️⃣ Photo / Voice / Video** pill; tapping it once opens the media preview modal and marks `isViewOnceOpened = true` so subsequent taps show *"1️⃣ Opened"*.
2. **Group Events & RSVP (`ChatMessageType.GROUP_EVENT`)**:
   - Added to the 2x3 WhatsApp Attachment Grid Tray (**Photos**, **Camera**, **Location**, **Document**, **Poll**, **Event**).
   - Stores event title, date/time, location, and member RSVP states (`GOING`, `MAYBE`, `NOT_GOING`) with live RSVP counters inside the bubble.
3. **Contact / Group Info Sheet & Admin Permissions (`ConversationEntity` + `ConversationMemberEntity`)**:
   - Tapping the conversation top bar opens a full sheet displaying:
     - **Media, Links & Docs** tabs filtered from the current conversation's messages.
     - **Group Participants & Admin Controls**: Add new member, promote/demote `ADMIN`, remove member, copy group invite link, and toggle `onlyAdminsCanSend`.
     - **Privacy & Security Controls**: Set **Disappearing Messages** (`Off`, `24h`, `7d`, `90d`), toggle **Chat Lock** (with 4-digit PIN verification), view **60-digit Security Verification Code**, and **Block / Report** the contact.
4. **Locked Chats Folder & Status Updates Drawer**:
   - Conversations with `isLocked = true` are hidden from the main list and grouped into a **"🔒 Locked Chats"** row at the top of the Chats list that requires the user's 4-digit PIN (default `1234`, customizable) to unlock.
   - A Status ring button in the Chats header opens the **Status Updates** drawer where users can view contact status updates and post their own 24-hour text/equation status.
