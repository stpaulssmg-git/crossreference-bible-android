# Apostolic Reference Bible 📖

An advanced, feature-rich Android Bible application built with Kotlin, Jetpack components, SQLite, and an optimized WebView HTML engine. Designed for deep study, cross-referencing, multi-translation comparison, and fast, intelligent searching.

---

## ✨ Key Features

### 1. 📖 Multilingual Reader & Ergonomic Navigation
* **Spacious Header & Right-Hand Action Cluster**:
  * **Left**: Prominent passage title (`Genesis 1`) with expanded room for long book names.
  * **Right**: Grouped action chips: `[🔍]` Search, `[KJV]` Active Translation Badge, and `[W]` Theme Picker.
* **Long-Press Quick Primary Switcher**:
  * Single-click on the translation badge opens full About/Manage settings.
  * **Long-press** on the circular translation chip (`[KJV]`) triggers haptic vibration and opens a 1-tap **Quick Primary Bible Selector** sheet.
* **On-the-Fly Top Chapter Swipe HUD**:
  * Top-positioned floating HUD card displaying localized book name and chapter number on the fly during horizontal swipe gestures, featuring high-contrast Dark Mode styling.
* **Pinch-to-Zoom Font Scaling**: Dynamic pinch gesture zooming with persistent font size preferences.
* **Font Family Selector**: Choose between **Sans-Serif (Modern)**, **Serif (Georgia Book Style)**, **Monospace**, and **Rounded** fonts directly in the About sheet.
* **BiDi Hebrew & Multilingual Support**: Right-To-Left (`RTL`) text directionality and Unicode directional isolates (`\u2067`) for Hebrew book names, references, and verse previews.
* **Startup Splash Screen**: Centered app logo and title during cold launch, smoothly fading out when content finishes loading.

---

### 2. 🔍 Advanced Full-Screen Search Engine
* **Smart Direct Reference Jump**:
  * Supports 1-letter, 2-letter, and 3-letter prefix matches (e.g., `h 3 2`, `ho 3 2`, or `hos 3 2` instantly targets **Hosea 3:2**; `ma 3 2` targets **Matthew 3:2**, **Mark 3:2**, **Malachi 3:2**).
  * Direct jump cards showing reference and total chapter count with one-tap verse scrolling and temporary highlight glow.
* **Whole-Word & Wildcard Word Search**:
  * **Exact Word Search**: Case-insensitive exact word matching (e.g., searching `love` matches *Love*, *love*, *LOVE*, but excludes *glove* or *clover*).
  * **Wildcard Searching**: Append `..` for prefix/stem matching (e.g., `love..` matches *love, loved, loves, lovingkindness*).
* **Testament Breakdown & Modal Stats Filter**:
  * Displays real-time Old Testament and New Testament occurrence counts (`KJV: OT - 124 [ + ] | NT - 86 [ + ]`).
  * Tapping `[ + ]` opens a 2-column modal sheet to filter search results by specific Bible books.
* **Pagination & Swipe Gestures**:
  * Displays 10 verses per page with total occurrence counts (`Showing 10 verses for "love" (Total 280)`).
  * Smooth horizontal swipe gestures or `Prev / Next` controls to navigate between result pages.
* **Search State Retention & Back Button Return**:
  * Preserves search query, pagination page, filter settings, and scroll position when navigating to the reader. Pressing the Android **Back button** on the main reader returns directly to your search page.

---

### 3. 🔗 Interactive Cross References & Study Navigation
* **Anchor Word Highlighting**: Interactive anchor words dynamically highlighted within King James Version (KJV) verses.
* **Interactive Cross Reference Sheet**:
  * Tapping highlighted anchor words or the `xref` tag opens a bottom sheet with verse previews for all linked cross-references.
  * **Clickable Individual Verse Numbers**: Tapping individual verse numbers in ranges or comma lists (e.g., tapping `8` in `James 3:4, 8, 10` or `17` in `James 3:17-18`) jumps directly to that specific verse in the reader.
* **Floating Return Navigation Bar**:
  * When jumping to a cross-reference, a floating bottom bar (`Return to xref (Job 11:1)`) allows one-tap return to your source passage and re-opens the cross-reference preview sheet.
  * Includes a quick `✕` clear button to dismiss the return bar.

---

### 4. 🔀 Parallel Multi-Translation Comparison
* **Inline Verse Comparison**:
  * Tap any verse in the main reader to toggle inline comparison for all active secondary translations.
  * Tap translation tags (`KJV`, `ABE`, `LXX`, etc.) to switch primary translations instantly.
* **Long-Press Drag & Drop Reordering**:
  * Press and hold any translation short name (`[LXX]`, `[ABE]`, `[NAS]`) inside the inline comparison block to drag and reorder comparison versions with haptic touch feedback.
  * Custom compare orders persist across all app chapters.
* **Verse Compare Modal**:
  * Detailed modal comparing selected verses across all installed translations.
* **Missing Verse Handling**:
  * If a verse is missing or omitted in a translation (e.g., Thomson Septuagint LXX variations), it explicitly displays *"Text is not available"* in theme-matched italics.

---

### 5. 🎨 Custom Reader Themes
Four unified themes applying consistent styling across status bar, header, reader view, and modal dialogs with a 1-tap **Crimson $\rightarrow$ Warm $\rightarrow$ Teal $\rightarrow$ Dark** cycle order (Crimson set as default first-install theme):
* 🍷 **Crimson** *(Default First-Install)*
* 📜 **Warm** (Parchment)
* 🌲 **Teal**
* 🌙 **Dark**

---

## 🛠️ Technical Stack & Architecture

* **Language**: Kotlin 100%
* **Architecture**: Android Native (AppCompat, Material Design 3, RecyclerView, CardView, BottomSheetDialog, RelativeLayout)
* **Local Database**: Embedded SQLite (`verses`, `cross_references`, multi-db comparison instances)
* **Rendering Engine**: Custom `HtmlEngine` injecting dynamic CSS, JavaScript bridges, and HTML DOM elements into a high-performance `WebView`.
* **Concurrency**: Executors single-thread background tasks for thread-safe DB reads.

---

## 🚀 Getting Started

### Prerequisites
* Android Studio Jellyfish / Ladybug or newer.
* Android SDK Minimum Version: `API 24` (Android 7.0 Nougat).
* Target SDK Version: `API 34` (Android 14).
* JDK 17+.

### Building from Source
1. Clone the repository:
   ```bash
   git clone https://github.com/stpaulssmg-git/crossreference-bible-android.git
   ```
2. Open the project in **Android Studio**.
3. Sync Gradle and build the debug APK:
   ```bash
   ./gradlew assembleDebug
   ```

---

## 📜 License & Copyright Disclosures

This application, its source code, and bundled resources are licensed under the **[Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International License (CC BY-NC-SA 4.0)](LICENSE)**.

### 🚫 Non-Commercial Notice:
Commercial use, monetization, or sale of this application, its source code, or bundled database files is **strictly prohibited**.

### 📚 Content Copyright Disclosures:
* **NASB (New American Standard Bible)**: Copyright © The Lockman Foundation. All rights reserved. Used strictly for non-commercial personal study.
* **ABP (Apostolic Bible Polyglot)**: Copyright © Charles Van der Pool. All rights reserved. Restricted to non-commercial personal study.
* **KJV & Public Domain Texts**: Public Domain.
