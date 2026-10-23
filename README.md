# Apostolic Reference Bible 📖

An advanced, feature-rich Android Bible application built with Kotlin, Jetpack components, SQLite, and an optimized WebView HTML engine. Designed for deep study, cross-referencing, multi-translation comparison, dynamic online catalog downloads, and fast, intelligent searching.

---

## ✨ Key Features

### 1. 📖 Multilingual Reader & Ergonomic Navigation
* **Clean Two-Line Header & 5-Dot Action Badge**:
  * **Passage & Concise Translation Subtitle**: Displays the active passage (`Genesis 1`) alongside the short translation title (*King James Version*, *Bukiyip NT*, *Kannada (IRV)*, *Louis Segond 1910*, etc.) directly below the book title in a clean 11sp subtitle font.
  * **Top Action Cluster**: Grouped actions including `[🔍]` Search, `[:::]` Centered 5-Dot Cross Action Badge, and `[D]` Theme Picker.
  * **1-Tap / Long-Press Version Switcher**: Tapping or long-pressing the circular `[:::]` badge opens the full-screen translation management modal.
* **On-the-Fly Top Chapter Swipe HUD**:
  * Top-positioned floating HUD card displaying localized book name and chapter number on the fly during horizontal swipe gestures, featuring high-contrast Dark Mode styling.
* **Native Localized Book Names (3-Tier Engine)**:
  * **Downloaded Bibles**: Automatically extracts official USFM book titles (`\h`, `\toc1`) during import and stores them in a `book_names` table in the SQLite database (`ebible_${id}.db`).
  * **Core Built-In Bibles**: Loads native 66-book names for core non-English translations (Hebrew/Greek, Kannada, Telugu, Malayalam, Tamil, Hindi, Spanish) from `assets/core_book_names.json`.
  * **Fallback**: Default English names (`Genesis` to `Revelation`).
* **Partial & New Testament-Only Bible Navigation**:
  * Smart initial jump to **Matthew 1** when downloading or loading an NT-only Bible (e.g. Finnish `FIN`) while reading an Old Testament book.
  * Full freedom to navigate to any Old Testament book—renders chapter controls with a clean notice: *"Text is not available in [Code]"*.
* **Pinch-to-Zoom Font Scaling**: Dynamic pinch gesture zooming with persistent font size preferences.
* **Font Family Selector**: Choose between **Sans-Serif (Modern)**, **Serif (Georgia Book Style)**, **Monospace**, and **Rounded** fonts directly in the settings sheet.
* **BiDi Hebrew & Multilingual Support**: Right-To-Left (`RTL`) text directionality and Unicode directional isolates (`\u2067`) for Hebrew book names, references, and verse previews.
* **Startup Splash Screen**: Centered app logo and title during cold launch, smoothly fading out when content finishes loading.

---

### 2. 🌐 Dynamic ebible.org Online Catalog & Translation Manager
* **Configurable Catalog Filtering (`catalog_filters.properties`)**:
  * Driven by `ebible.org`'s official `translations.csv` registry bundled in `assets/translations.csv`.
  * Column filter rules configured externally in `assets/catalog_filters.properties` (e.g. `downloadable=true`, `Certified=true`), allowing easy addition of future rules without writing Kotlin code.
* **Expandable Metadata Rows**:
  * Tapping any Bible item row in the catalog expands an inline details panel displaying:
    * 📝 **Description**: Full translation description.
    * 📚 **Available Books**: Old Testament (`OT`) & New Testament (`NT`) book counts.
    * 📅 **Last Updated**: Last published/updated date.
    * ⚖️ **Copyright**: Copyright statement and publisher disclosures.
* **3.5x Fast Database Importer Engine**:
  * Optimized VPL/USFM importer using fast zero-regex string tokenizing, 64 KB stream buffers, and bulk-import SQLite PRAGMAs (`PRAGMA synchronous = OFF`).
* **Silent Non-Intrusive Operations**:
  * Background downloading, database extraction, and translation switches operate silently without interrupting the user with popup toast messages.
* **Inline Catalog Delete & Storage Limits**:
  * **Direct Delete Button**: Red trash icon (`btnDeleteEbible`) in the catalog list for downloaded versions—permanently deletes the `.db` file from storage and transforms the row back to an active Download button.
  * **20-Translation Limit**: Enforces a maximum limit of **20 downloaded translations** to manage storage space.
* **Ergonomic Touch Targets**:
  * Taller 48dp standard touch target height for "Download Bibles" and "Refresh List" buttons in settings.

---

### 3. 🔍 Advanced Live Verse Search Engine
* **Instant Live Verse Matching as You Type**:
  * **150ms Input Debounce**: Fast typing executes search with 150ms input debouncing, keeping keyboard input 100% fluid.
  * **Prefix Word Boundary Matching**: Typing `"te"` instantly matches words starting with `te` (*testimony*, *testified*, *tent*, *temple*).
* **2-Character & Punctuation Bypass Engine**:
  * **1 Letter (`a`, `t`)**: Shows prompt *"Type 2+ characters or punctuation (e.g. ?) to search..."*.
  * **Letter + Space (`a `, `I `)**: Bypasses 1-letter restriction to match standalone single-letter words (*"a man"*, *"I am"*).
  * **Single Punctuation (`?`, `.`, `!`, `"`)**: Bypasses 1-letter restriction to instantly search for questions, direct speech, or warnings.
* **Smart Direct Reference Jump**:
  * Supports 1-letter, 2-letter, and 3-letter prefix matches (e.g., `h 3 2`, `ho 3 2`, or `hos 3 2` instantly targets **Hosea 3:2**; `ma 3 2` targets **Matthew 3:2**, **Mark 3:2**, **Malachi 3:2**).
  * Direct jump cards showing reference and total chapter count with one-tap verse scrolling and temporary highlight glow.
* **Whole-Word & Wildcard Word Search**:
  * **Exact Word Search**: Case-insensitive exact word matching (e.g., searching `love` matches *Love*, *love*, *LOVE*, but excludes *glove* or *clover*).
  * **Wildcard Searching**: Append `..` for prefix/stem matching (e.g., `love..` matches *love, loved, loves, lovingkindness*).
* **Testament Breakdown & Modal Stats Filter**:
  * Displays real-time Old Testament and New Testament occurrence counts (`KJV: OT - 124 [ + ] | NT - 86 [ + ]`).
  * Tapping `[ + ]` opens a 2-column modal sheet to filter search results by specific Bible books.
* **ebible.org Search Fallback**:
  * If a search query yields 0 verse results (e.g. searching for a language like `Swahili` or `French`), an interactive action prompt appears: `"No certified downloadable Bible found matching 'Swahili'. Tap to search ebible.org catalog ➔"`.
* **Pagination & Swipe Gestures**:
  * Displays 10 verses per page with total occurrence counts (`Showing 10 verses for "love" (Total 280)`).
  * Smooth horizontal swipe gestures or `Prev / Next` controls to navigate between result pages.
* **Search State Retention & Back Button Return**:
  * Preserves search query, pagination page, filter settings, and scroll position when navigating to the reader. Pressing the Android **Back button** on the main reader returns directly to your search page.

---

### 4. 🔗 Interactive Cross References & Study Navigation
* **Anchor Word Highlighting**: Interactive anchor words dynamically highlighted within King James Version (KJV) verses.
* **Interactive Cross Reference Sheet**:
  * Tapping highlighted anchor words or the `xref` tag opens a bottom sheet with verse previews for all linked cross-references.
  * **Clickable Individual Verse Numbers**: Tapping individual verse numbers in ranges or comma lists (e.g., tapping `8` in `James 3:4, 8, 10` or `17` in `James 3:17-18`) jumps directly to that specific verse in the reader.
* **Floating Return Navigation Bar**:
  * When jumping to a cross-reference, a floating bottom bar (`Return to xref (Job 11:1)`) allows one-tap return to your source passage and re-opens the cross-reference preview sheet.
  * Includes a quick `✕` clear button to dismiss the return bar.

---

### 5. 🔀 Parallel Multi-Translation Comparison
* **First Install Defaults**:
  * Primary: `KJV` (King James Version).
  * Secondary Comparison: `H/G` (Hebrew/Greek), `ABG` (Apostolic Greek), `LXX` (Septuagint English), and `T4T` (Translation for Translators) in **always expanded mode** by default.
* **Inline Verse Comparison**:
  * Tap any verse in the main reader to toggle inline comparison for all active secondary translations.
  * Tap translation tags (`KJV`, `ABE`, `LXX`, etc.) to switch primary translations instantly.
* **Long-Press Drag & Drop Reordering**:
  * Press and hold any translation short name (`[LXX]`, `[ABE]`, `[NAS]`) inside the inline comparison block to drag and reorder comparison versions with haptic touch feedback.
  * Custom compare orders persist across all app chapters.
* **Verse Compare Modal**:
  * Detailed modal comparing selected verses across all installed translations.
* **Missing Verse Handling**:
  * If a verse is missing or omitted in a translation (e.g., Thomson Septuagint LXX variations or partial Bibles), it explicitly displays *"Text is not available"* in theme-matched italics.

---

### 6. 🎨 Custom Reader Themes
Four unified themes applying consistent styling across status bar, header, reader view, and modal dialogs with a 1-tap **Dark $\rightarrow$ Teal $\rightarrow$ Warm $\rightarrow$ Crimson** cycle order (Dark set as default first-install theme):
* 🌙 **Dark** *(Default First-Install)*
* 🌲 **Teal**
* 📜 **Warm** (Parchment)
* 🍷 **Crimson**

---

## 🛠️ Technical Stack & Architecture

* **Language**: Kotlin 100%
* **Architecture**: Android Native (AppCompat, Material Design 3, RecyclerView, CardView, BottomSheetDialog, RelativeLayout)
* **Local Database**: Embedded SQLite (`verses`, `cross_references`, `book_names`, multi-db comparison instances) with in-memory connection handle caching (`openDbMap`) for zero connection churn.
* **Rendering Engine**: Custom `HtmlEngine` injecting dynamic CSS, JavaScript bridges, and HTML DOM elements into a high-performance `WebView`.
* **Data Assets**: Bundled `translations.csv` (1,000+ ebible.org registry), `catalog_filters.properties` (column filter configuration), and `core_book_names.json` (core localized book name maps).
* **Concurrency**: Executors single-thread background tasks for thread-safe DB reads and VPL/USFM Bible parsing.

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
* **eBible.org Download Disclaimer**: Additional Bible translations downloaded via the online catalog remain the property and copyright of **eBible.org** and their respective translators or publishing organizations. Downloaded translations are provided strictly for non-commercial personal study.
