package com.stpauls.apostolicreferencebible

import android.annotation.SuppressLint
import android.app.Dialog
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.text.Editable
import android.text.Html
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextPaint
import android.text.TextWatcher
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Log
import android.util.TypedValue
import android.view.GestureDetector
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.stpauls.apostolicreferencebible.db.BIBLE_BOOKS
import com.stpauls.apostolicreferencebible.db.BibleDatabaseHelper
import com.stpauls.apostolicreferencebible.db.INSTALLED_TRANSLATIONS
import com.stpauls.apostolicreferencebible.db.TRANSLATION_MAP
import com.stpauls.apostolicreferencebible.db.getLocalizedBookName
import com.stpauls.apostolicreferencebible.engine.HtmlEngine
import com.stpauls.apostolicreferencebible.ui.BookRecyclerAdapter
import com.stpauls.apostolicreferencebible.ui.SearchResultAdapter
import java.util.concurrent.Executors
import kotlin.math.abs

class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MainActivity"
        private const val PREFS_NAME = "bible_app_prefs"
        private const val KEY_THEME = "selected_theme"
        private const val KEY_FONT_SIZE = "selected_font_size"
        private const val KEY_FONT_FAMILY = "selected_font_family"
        private const val KEY_PRIMARY_TRANS = "primary_translation"
        private const val KEY_ENABLED_TRANS = "enabled_translations"
        private const val KEY_COMPARE_ORDER = "compare_translation_order"
    }

    private lateinit var webView: WebView
    private lateinit var tvHeaderTitle: TextView
    private lateinit var btnHeaderTitleContainer: LinearLayout
    private lateinit var tvActiveTranslationBadge: TextView
    private lateinit var btnThemePicker: TextView
    private lateinit var btnOpenSearch: ImageButton
    private lateinit var headerBar: RelativeLayout
    private lateinit var headerDivider: View
    private lateinit var layoutSplashOverlay: RelativeLayout

    // On-the-Fly Chapter Swipe HUD Overlay Elements
    private lateinit var cardChapterSwipeHUD: CardView
    private lateinit var tvChapterSwipeHUDText: TextView
    private var hudRunnable: Runnable? = null
    private val hudHandler = Handler(Looper.getMainLooper())

    // Floating Cross-Reference Return Navigation Bar Elements
    private lateinit var cardCrossRefReturnBar: CardView
    private lateinit var btnReturnToCrossRef: TextView
    private lateinit var btnClearReturnBar: ImageButton

    data class CrossRefReturnState(
        val anchor: String,
        val sourceRef: String,
        val referencesStr: String,
        val sourceBookIndex: Int,
        val sourceChapter: Int,
        val sourceVerse: Int
    )

    private var activeCrossRefReturnState: CrossRefReturnState? = null

    private lateinit var dbHelper: BibleDatabaseHelper
    private lateinit var htmlEngine: HtmlEngine
    private val executor = Executors.newSingleThreadExecutor()

    private var currentBookIndex = 0
    private var currentChapter = 1
    private var currentTheme = "crimson" // Options: "warm", "crimson", "teal", "dark"
    private var currentFontSize = 14f // Lowest initial zoom level by default
    private var currentFontFamily = "sans" // Options: "sans", "serif", "mono", "rounded"

    private var currentPrimaryTranslation = "KJV"
    private var enabledTranslations = mutableSetOf("KJV", "ABE")

    private val themeList = listOf("crimson", "warm", "teal", "dark")

    // Navigation Backstack History for Cross-Reference study jumps
    data class NavState(
        val bookIndex: Int,
        val chapter: Int,
        val verse: Int,
        val isSearchJump: Boolean = false
    )
    private val navHistory = mutableListOf<NavState>()

    // Retain search dialog instance to preserve query and scroll results when returning from main reader
    private var cachedSearchDialog: Dialog? = null

    data class ModalThemeColors(
        val sheetBg: String,
        val titleColor: String,
        val subtitleColor: String,
        val dividerColor: String,
        val refTitleColor: String,
        val previewTextColor: String,
        val dragHandleColor: String,
        val expandedGridBg: String,
        val chipBg: String,
        val chipTextColor: String,
        val chipBorderColor: String
    )

    fun getModalThemeColors(themeName: String): ModalThemeColors {
        return when (themeName) {
            "crimson" -> ModalThemeColors(
                sheetBg = "#FFF8F6",
                titleColor = "#3E1014",
                subtitleColor = "#8C5A5D",
                dividerColor = "#E8D0C8",
                refTitleColor = "#8B0000",
                previewTextColor = "#3E1014",
                dragHandleColor = "#E8D0C8",
                expandedGridBg = "#FDF0ED",
                chipBg = "#F8ECE8",
                chipTextColor = "#B71C1C",
                chipBorderColor = "#E0C0B8"
            )
            "teal" -> ModalThemeColors(
                sheetBg = "#F2F8F8",
                titleColor = "#00363A",
                subtitleColor = "#547A7C",
                dividerColor = "#B2DFDB",
                refTitleColor = "#005F60",
                previewTextColor = "#112A2C",
                dragHandleColor = "#B2DFDB",
                expandedGridBg = "#EBF5F5",
                chipBg = "#E0F2F1",
                chipTextColor = "#00796B",
                chipBorderColor = "#B0BEC5"
            )
            "dark" -> ModalThemeColors(
                sheetBg = "#1E1E1E",
                titleColor = "#FFFFFF",
                subtitleColor = "#AAAAAA",
                dividerColor = "#333333",
                refTitleColor = "#EF5350",
                previewTextColor = "#E0E0E0",
                dragHandleColor = "#555555",
                expandedGridBg = "#2A2A2A",
                chipBg = "#333333",
                chipTextColor = "#64B5F6",
                chipBorderColor = "#444444"
            )
            else -> ModalThemeColors( // "warm"
                sheetBg = "#FBF9F5",
                titleColor = "#1565C0",
                subtitleColor = "#757575",
                dividerColor = "#E0D8C8",
                refTitleColor = "#B71C1C",
                previewTextColor = "#2C2C2C",
                dragHandleColor = "#CCCCCC",
                expandedGridBg = "#FAF6EE",
                chipBg = "#F5F0E6",
                chipTextColor = "#1565C0",
                chipBorderColor = "#D0C8B8"
            )
        }
    }

    private fun isRtlText(text: String): Boolean {
        if (text.isBlank()) return false
        for (char in text) {
            val code = char.code
            if ((code in 0x0590..0x05FF) || (code in 0xFB1D..0xFB4F) ||
                (code in 0x0600..0x06FF) || (code in 0x0750..0x077F) || (code in 0x08A0..0x08FF) ||
                (code in 0xFB50..0xFDFF) || (code in 0xFE70..0xFEFF)) {
                return true
            }
            val directionality = Character.getDirectionality(char)
            if (directionality == Character.DIRECTIONALITY_RIGHT_TO_LEFT ||
                directionality == Character.DIRECTIONALITY_RIGHT_TO_LEFT_ARABIC) {
                return true
            }
        }
        return false
    }

    @SuppressLint("MissingPermission")
    private fun vibrateHaptic() {
        val vibrator = getSystemService(VIBRATOR_SERVICE) as? Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createOneShot(40, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(40)
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Register Back Press Callback for Cross-Reference Study Navigation Stack
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (navHistory.isNotEmpty()) {
                    val previousState = navHistory.removeAt(navHistory.lastIndex)
                    currentBookIndex = previousState.bookIndex
                    currentChapter = previousState.chapter
                    loadCurrentChapter(targetVerseNum = previousState.verse)

                    if (previousState.isSearchJump && cachedSearchDialog != null) {
                        cachedSearchDialog?.show()
                    }
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                    isEnabled = true
                }
            }
        })

        // Load saved preferences with KJV as primary and ABE as compare by default on first install
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        currentTheme = prefs.getString(KEY_THEME, "crimson") ?: "crimson"
        currentFontSize = prefs.getFloat(KEY_FONT_SIZE, 14f)
        currentFontFamily = prefs.getString(KEY_FONT_FAMILY, "sans") ?: "sans"
        currentPrimaryTranslation = prefs.getString(KEY_PRIMARY_TRANS, "KJV") ?: "KJV"
        enabledTranslations = prefs.getStringSet(KEY_ENABLED_TRANS, setOf("KJV", "ABE"))?.toMutableSet() ?: mutableSetOf("KJV", "ABE")

        // Initialize helpers
        dbHelper = BibleDatabaseHelper(this)
        htmlEngine = HtmlEngine(this)

        // Initialize Views
        webView = findViewById(R.id.webView)
        tvHeaderTitle = findViewById(R.id.tvHeaderTitle)
        btnHeaderTitleContainer = findViewById(R.id.btnHeaderTitleContainer)
        tvActiveTranslationBadge = findViewById(R.id.tvActiveTranslationBadge)
        btnThemePicker = findViewById(R.id.btnThemePicker)
        btnOpenSearch = findViewById(R.id.btnOpenSearch)
        headerBar = findViewById(R.id.headerBar)
        headerDivider = findViewById(R.id.headerDivider)
        layoutSplashOverlay = findViewById(R.id.layoutSplashOverlay)

        // Chapter Swipe HUD Overlay Views
        cardChapterSwipeHUD = findViewById(R.id.cardChapterSwipeHUD)
        tvChapterSwipeHUDText = findViewById(R.id.tvChapterSwipeHUDText)

        // Floating Cross Reference Return Bar
        cardCrossRefReturnBar = findViewById(R.id.cardCrossRefReturnBar)
        btnReturnToCrossRef = findViewById(R.id.btnReturnToCrossRef)
        btnClearReturnBar = findViewById(R.id.btnClearReturnBar)

        btnClearReturnBar.setOnClickListener {
            cardCrossRefReturnBar.visibility = View.GONE
            activeCrossRefReturnState = null
        }

        btnReturnToCrossRef.setOnClickListener {
            val returnState = activeCrossRefReturnState ?: return@setOnClickListener
            cardCrossRefReturnBar.visibility = View.GONE

            currentBookIndex = returnState.sourceBookIndex
            currentChapter = returnState.sourceChapter
            loadCurrentChapter(targetVerseNum = returnState.sourceVerse)

            showBottomSheet(returnState.anchor, returnState.sourceRef, returnState.referencesStr)
        }

        webView.settings.apply {
            javaScriptEnabled = true
            setSupportZoom(false)
            builtInZoomControls = false
            displayZoomControls = false
            useWideViewPort = false
            loadWithOverviewMode = false
        }
        webView.addJavascriptInterface(CrossReferenceInterface(), "Android")

        // Single click opens About modal; Long press opens Quick Primary Translation Selector
        tvActiveTranslationBadge.setOnClickListener { showAboutModal() }
        tvActiveTranslationBadge.setOnLongClickListener {
            vibrateHaptic()
            showQuickPrimarySelectorModal()
            true
        }

        // Open Book & Chapter selector modal on title click
        btnHeaderTitleContainer.setOnClickListener { showBookPickerModal() }

        // Open Search modal on search button click
        btnOpenSearch.setOnClickListener { showSearchModal() }

        // Cycle through themes on single click
        btnThemePicker.setOnClickListener { cycleNextTheme() }

        // Apply native header colors for saved theme
        applyNativeThemeUI(currentTheme)

        // Load initial chapter (Genesis 1)
        loadCurrentChapter()
    }

    private fun showQuickPrimarySelectorModal() {
        val bottomSheetDialog = BottomSheetDialog(this)
        val dialogView = LayoutInflater.from(this).inflate(
            R.layout.dialog_quick_primary,
            findViewById(android.R.id.content),
            false
        )

        val colors = getModalThemeColors(currentTheme)
        dialogView.findViewById<View>(R.id.quickPrimaryContainer)?.setBackgroundColor(Color.parseColor(colors.sheetBg))
        dialogView.findViewById<View>(R.id.vDragHandle)?.setBackgroundColor(Color.parseColor(colors.dragHandleColor))
        dialogView.findViewById<TextView>(R.id.tvQuickPrimaryTitle)?.setTextColor(Color.parseColor(colors.titleColor))
        dialogView.findViewById<View>(R.id.vQuickPrimaryDivider)?.setBackgroundColor(Color.parseColor(colors.dividerColor))

        val containerList = dialogView.findViewById<LinearLayout>(R.id.containerQuickPrimaryList)
        val inflater = LayoutInflater.from(this)
        containerList?.removeAllViews()

        for (trans in INSTALLED_TRANSLATIONS) {
            val itemView = inflater.inflate(R.layout.item_quick_primary, containerList, false)
            val rbQuickPrimary = itemView.findViewById<RadioButton>(R.id.rbQuickPrimary)
            val tvQuickPrimaryLabel = itemView.findViewById<TextView>(R.id.tvQuickPrimaryLabel)

            val isCurrentPrimary = trans.code.equals(currentPrimaryTranslation, ignoreCase = true)
            rbQuickPrimary.isChecked = isCurrentPrimary
            tvQuickPrimaryLabel.setTextColor(Color.parseColor(colors.titleColor))
            tvQuickPrimaryLabel.text = "${trans.code} - ${trans.name}"

            itemView.setOnClickListener {
                if (!isCurrentPrimary) {
                    currentPrimaryTranslation = trans.code
                    enabledTranslations.add(trans.code)
                    saveTranslationPreferences()
                    loadCurrentChapter()
                    cachedSearchDialog = null
                    bottomSheetDialog.dismiss()
                    Toast.makeText(this@MainActivity, "Primary Translation: ${trans.name}", Toast.LENGTH_SHORT).show()
                } else {
                    bottomSheetDialog.dismiss()
                }
            }

            containerList?.addView(itemView)
        }

        bottomSheetDialog.setContentView(dialogView)
        bottomSheetDialog.show()
    }

    private fun showOnTheFlyChapterHUD() {
        val bookInfo = BIBLE_BOOKS[currentBookIndex]
        val localizedBookName = getLocalizedBookName(bookInfo.code, currentPrimaryTranslation)
        tvChapterSwipeHUDText.text = "$localizedBookName $currentChapter"

        hudRunnable?.let { hudHandler.removeCallbacks(it) }
        cardChapterSwipeHUD.animate().cancel()
        cardChapterSwipeHUD.alpha = 1.0f
        cardChapterSwipeHUD.visibility = View.VISIBLE

        hudRunnable = Runnable {
            cardChapterSwipeHUD.animate().alpha(0f).setDuration(350).withEndAction {
                cardChapterSwipeHUD.visibility = View.GONE
            }
        }
        hudHandler.postDelayed(hudRunnable!!, 1100)
    }

    private fun cycleNextTheme() {
        val currentIndex = themeList.indexOf(currentTheme)
        val nextIndex = if (currentIndex >= 0) (currentIndex + 1) % themeList.size else 0
        val nextTheme = themeList[nextIndex]

        changeTheme(nextTheme)
    }

    private fun applyNativeThemeUI(themeName: String) {
        val (headerBg, headerText, dividerBg, statusBg) = when (themeName) {
            "crimson" -> Tuple4("#F8ECE8", "#3E1014", "#E8D0C8", "#F8ECE8")
            "teal" -> Tuple4("#E0F2F1", "#00363A", "#B2DFDB", "#E0F2F1")
            "dark" -> Tuple4("#121212", "#FFFFFF", "#333333", "#121212")
            else -> Tuple4("#F5F0E6", "#1C1B1F", "#E0D8C8", "#F5F0E6") // "warm"
        }

        headerBar.setBackgroundColor(Color.parseColor(headerBg))
        tvHeaderTitle.setTextColor(Color.parseColor(headerText))
        btnOpenSearch.setColorFilter(Color.parseColor(headerText))

        tvActiveTranslationBadge.setTextColor(Color.parseColor(headerText))
        @Suppress("DEPRECATION")
        tvActiveTranslationBadge.background?.setColorFilter(Color.parseColor(headerText), PorterDuff.Mode.SRC_IN)

        layoutSplashOverlay.setBackgroundColor(Color.parseColor(headerBg))
        findViewById<TextView>(R.id.tvSplashTitle)?.setTextColor(Color.parseColor(headerText))

        btnThemePicker.text = themeName.first().uppercase()
        btnThemePicker.setTextColor(Color.parseColor(headerText))
        @Suppress("DEPRECATION")
        btnThemePicker.background?.setColorFilter(Color.parseColor(headerText), PorterDuff.Mode.SRC_IN)

        headerDivider.setBackgroundColor(Color.parseColor(dividerBg))

        val (hudBg, hudText) = when (themeName) {
            "crimson" -> Pair("#8B0000", "#FFFFFF")
            "teal" -> Pair("#005F60", "#FFFFFF")
            "dark" -> Pair("#2A2A2A", "#EF5350")
            else -> Pair("#1565C0", "#FFFFFF")
        }
        cardChapterSwipeHUD.setCardBackgroundColor(Color.parseColor(hudBg))
        tvChapterSwipeHUDText.setTextColor(Color.parseColor(hudText))

        val returnBarBg = when (themeName) {
            "crimson" -> "#8B0000"
            "teal" -> "#005F60"
            "dark" -> "#1E3A5F"
            else -> "#1565C0"
        }
        cardCrossRefReturnBar.setCardBackgroundColor(Color.parseColor(returnBarBg))

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = Color.parseColor(statusBg)
        }
    }

    private data class Tuple4(val headerBg: String, val headerText: String, val dividerBg: String, val statusBg: String)

    private fun changeTheme(newTheme: String) {
        currentTheme = newTheme
        cachedSearchDialog = null // Invalidate search dialog cache on theme change

        // Save to SharedPreferences
        getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
            .edit()
            .putString(KEY_THEME, newTheme)
            .apply()

        // Apply Native UI theme
        applyNativeThemeUI(newTheme)

        // Dynamically update theme in WebView
        webView.evaluateJavascript("setTheme('$newTheme')", null)
    }

    private fun loadCurrentChapter(targetVerseNum: Int? = null) {
        val bookInfo = BIBLE_BOOKS[currentBookIndex]
        val localizedBookName = getLocalizedBookName(bookInfo.code, currentPrimaryTranslation)
        tvHeaderTitle.text = "$localizedBookName $currentChapter"
        tvActiveTranslationBadge.text = currentPrimaryTranslation

        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        currentFontSize = prefs.getFloat(KEY_FONT_SIZE, currentFontSize)

        if (targetVerseNum != null) {
            webView.webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    webView.postDelayed({
                        webView.evaluateJavascript("scrollToVerse($targetVerseNum)", null)
                    }, 250)
                    // Reset webViewClient after scrolling once so subsequent loads do NOT re-trigger highlight
                    webView.webViewClient = WebViewClient()
                }
            }
        } else {
            webView.webViewClient = WebViewClient()
        }

        executor.execute {
            try {
                // Query primary verses and cross references for requested chapter
                val verses = dbHelper.getVerses(currentPrimaryTranslation, bookInfo.code, currentChapter)
                val crossRefs = dbHelper.getCrossReferences(bookInfo.code, currentChapter)

                // Query enabled secondary comparison verses for requested chapter
                val activeSecondary = enabledTranslations.filter { !it.equals(currentPrimaryTranslation, ignoreCase = true) }.toSet()
                val primaryVerseNumbers = verses.map { it.verse }
                val compareOrderCsv = prefs.getString(KEY_COMPARE_ORDER, "") ?: ""
                val compareOrderList = compareOrderCsv.split(",").map { it.trim() }.filter { it.isNotEmpty() }

                val secondaryVersesMap = if (activeSecondary.isNotEmpty()) {
                    dbHelper.getSecondaryVersesForChapter(
                        activeSecondary,
                        bookInfo.code,
                        currentChapter,
                        primaryVerseNumbers,
                        compareOrderList
                    )
                } else emptyMap()

                // Generate HTML with anchor word highlighting ONLY for KJV, inline secondary verses, active theme, active font family, and saved font size
                val htmlContent = htmlEngine.generateChapterHtml(
                    verses = verses,
                    crossRefs = crossRefs,
                    secondaryVersesMap = secondaryVersesMap,
                    themeName = currentTheme,
                    fontFamily = currentFontFamily,
                    fontSize = currentFontSize,
                    primaryTranslationCode = currentPrimaryTranslation
                )

                runOnUiThread {
                    webView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)

                    // Smoothly fade out splash screen overlay once main reader content is loaded
                    if (layoutSplashOverlay.visibility == View.VISIBLE) {
                        layoutSplashOverlay.animate().alpha(0f).setDuration(400).withEndAction {
                            layoutSplashOverlay.visibility = View.GONE
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading chapter", e)
            }
        }
    }

    private fun showAboutModal() {
        val bottomSheetDialog = BottomSheetDialog(this)
        val dialogView = LayoutInflater.from(this).inflate(
            R.layout.dialog_about,
            findViewById(android.R.id.content),
            false
        )

        val colors = getModalThemeColors(currentTheme)

        dialogView.setBackgroundColor(Color.parseColor(colors.sheetBg))
        dialogView.findViewById<View>(R.id.vDragHandle)?.setBackgroundColor(Color.parseColor(colors.dragHandleColor))
        dialogView.findViewById<TextView>(R.id.tvAboutTitle)?.setTextColor(Color.parseColor(colors.titleColor))
        dialogView.findViewById<TextView>(R.id.tvAboutVersion)?.setTextColor(Color.parseColor(colors.subtitleColor))
        dialogView.findViewById<View>(R.id.vAboutDivider)?.setBackgroundColor(Color.parseColor(colors.dividerColor))
        dialogView.findViewById<View>(R.id.vAboutDivider2)?.setBackgroundColor(Color.parseColor(colors.dividerColor))
        dialogView.findViewById<View>(R.id.vAboutDividerFont)?.setBackgroundColor(Color.parseColor(colors.dividerColor))
        dialogView.findViewById<View>(R.id.vAboutDividerFeature)?.setBackgroundColor(Color.parseColor(colors.dividerColor))
        dialogView.findViewById<View>(R.id.vAboutDivider3)?.setBackgroundColor(Color.parseColor(colors.dividerColor))
        dialogView.findViewById<View>(R.id.vAboutDivider4)?.setBackgroundColor(Color.parseColor(colors.dividerColor))

        dialogView.findViewById<TextView>(R.id.tvManageTranslationsHeader)?.setTextColor(Color.parseColor(colors.titleColor))
        dialogView.findViewById<TextView>(R.id.tvFontSectionHeader)?.setTextColor(Color.parseColor(colors.titleColor))
        dialogView.findViewById<TextView>(R.id.tvAboutAppHeader)?.setTextColor(Color.parseColor(colors.titleColor))
        dialogView.findViewById<TextView>(R.id.tvKeyFeaturesHeader)?.setTextColor(Color.parseColor(colors.titleColor))
        dialogView.findViewById<TextView>(R.id.tvCopyrightHeader)?.setTextColor(Color.parseColor(colors.titleColor))

        dialogView.findViewById<TextView>(R.id.tvAboutDescription)?.setTextColor(Color.parseColor(colors.subtitleColor))
        dialogView.findViewById<TextView>(R.id.tvKeyFeaturesDetails)?.setTextColor(Color.parseColor(colors.subtitleColor))

        dialogView.findViewById<TextView>(R.id.tvAboutFooterDev)?.setTextColor(Color.parseColor(colors.titleColor))
        dialogView.findViewById<TextView>(R.id.tvAboutFooterCopyright)?.setTextColor(Color.parseColor(colors.subtitleColor))

        // Bind Font Selector Radio Group in About Modal Sheet
        val rbSans = dialogView.findViewById<RadioButton>(R.id.rbAboutFontSans)
        val rbSerif = dialogView.findViewById<RadioButton>(R.id.rbAboutFontSerif)
        val rbMono = dialogView.findViewById<RadioButton>(R.id.rbAboutFontMono)
        val rbRounded = dialogView.findViewById<RadioButton>(R.id.rbAboutFontRounded)

        rbSans?.setTextColor(Color.parseColor(colors.titleColor))
        rbSerif?.setTextColor(Color.parseColor(colors.titleColor))
        rbMono?.setTextColor(Color.parseColor(colors.titleColor))
        rbRounded?.setTextColor(Color.parseColor(colors.titleColor))

        when (currentFontFamily) {
            "serif" -> rbSerif?.isChecked = true
            "mono" -> rbMono?.isChecked = true
            "rounded" -> rbRounded?.isChecked = true
            else -> rbSans?.isChecked = true
        }

        val rgAboutFontGroup = dialogView.findViewById<RadioGroup>(R.id.rgAboutFontGroup)
        rgAboutFontGroup?.setOnCheckedChangeListener { _, checkedId ->
            val newFont = when (checkedId) {
                R.id.rbAboutFontSerif -> "serif"
                R.id.rbAboutFontMono -> "mono"
                R.id.rbAboutFontRounded -> "rounded"
                else -> "sans"
            }

            if (newFont != currentFontFamily) {
                currentFontFamily = newFont
                getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                    .edit()
                    .putString(KEY_FONT_FAMILY, newFont)
                    .apply()

                webView.evaluateJavascript("setFontFamily('$newFont')", null)
            }
        }

        val tvCopyrightDetails = dialogView.findViewById<TextView>(R.id.tvCopyrightDetails)
        tvCopyrightDetails?.setTextColor(Color.parseColor(colors.subtitleColor))
        tvCopyrightDetails?.setLinkTextColor(Color.parseColor(colors.chipTextColor))
        tvCopyrightDetails?.movementMethod = LinkMovementMethod.getInstance()

        val copyrightHtml = """
            <b>New American Standard Bible® (NASB)</b><br>
            Scripture taken from the New American Standard Bible®, Copyright &copy; 1960, 1962, 1963, 1968, 1971, 1972, 1973, 1975, 1977, 1995, 2020 by The Lockman Foundation. Used in accordance with the non-commercial digital media policy. <a href="https://www.lockman.org">www.Lockman.org</a><br><br>
            <b>Apostolic Bible Polyglot (ABe / ABg)</b><br>
            &copy; Charles Van der Pool (<a href="https://www.apostolicbible.com">www.apostolicbible.com</a>). Inclusion in this app does not imply endorsement by the author.<br><br>
            <b>Translation for Translators (T4T)</b><br>
            &copy; Ellis W. Deibler, Jr. / unfoldingWord. Licensed under <a href="https://creativecommons.org/licenses/by-sa/4.0/">CC BY-SA 4.0</a>.<br><br>
            <b>Indian Revised Versions (KAN / TLG / TAM / HIN)</b><br>
            Kannada, Telugu, Tamil, and Hindi IRV Bibles &copy; Bridge Connectivity Solutions Pvt. Ltd. / unfoldingWord. Licensed under <a href="https://creativecommons.org/licenses/by-sa/4.0/">CC BY-SA 4.0</a>.<br><br>
            <b>Public Domain Translations (KJV / H/G / MAL / SPA / LXX)</b><br>
            King James Version (KJV), Hebrew Masoretic &amp; Greek TR (H/G), Malayalam VPL, Spanish Reina Valera 1909 (SPA), and Charles Thomson LXX Septuagint (LXX) are in the Public Domain.
        """.trimIndent()

        tvCopyrightDetails?.text = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(copyrightHtml, Html.FROM_HTML_MODE_LEGACY)
        } else {
            @Suppress("DEPRECATION")
            Html.fromHtml(copyrightHtml)
        }

        dialogView.findViewById<TextView>(R.id.tvColTranslation)?.setTextColor(Color.parseColor(colors.subtitleColor))
        dialogView.findViewById<TextView>(R.id.tvColPrimary)?.setTextColor(Color.parseColor(colors.subtitleColor))
        dialogView.findViewById<TextView>(R.id.tvColCompare)?.setTextColor(Color.parseColor(colors.subtitleColor))
        dialogView.findViewById<View>(R.id.vTableHeaderDivider)?.setBackgroundColor(Color.parseColor(colors.dividerColor))

        val btnToggleManageTranslations = dialogView.findViewById<LinearLayout>(R.id.btnToggleManageTranslations)
        val ivManageTranslationsDropdown = dialogView.findViewById<ImageView>(R.id.ivManageTranslationsDropdown)
        val layoutManageTranslationsContent = dialogView.findViewById<LinearLayout>(R.id.layoutManageTranslationsContent)

        ivManageTranslationsDropdown?.setColorFilter(Color.parseColor(colors.titleColor))

        // Expanded state by default whenever opened
        layoutManageTranslationsContent.visibility = View.VISIBLE
        ivManageTranslationsDropdown.rotation = 180f

        btnToggleManageTranslations?.setOnClickListener {
            if (layoutManageTranslationsContent.visibility == View.VISIBLE) {
                layoutManageTranslationsContent.visibility = View.GONE
                ivManageTranslationsDropdown.rotation = 0f
            } else {
                layoutManageTranslationsContent.visibility = View.VISIBLE
                ivManageTranslationsDropdown.rotation = 180f
            }
        }

        val containerTranslations = dialogView.findViewById<LinearLayout>(R.id.containerTranslations)
        val inflater = LayoutInflater.from(this)

        val radioButtonsMap = mutableMapOf<String, RadioButton>()
        val checkBoxesMap = mutableMapOf<String, CheckBox>()

        containerTranslations?.removeAllViews()

        for (trans in INSTALLED_TRANSLATIONS) {
            val rowView = inflater.inflate(R.layout.item_translation_setting, containerTranslations, false)
            val tvTransLabel = rowView.findViewById<TextView>(R.id.tvTransLabel)
            val rbTransPrimary = rowView.findViewById<RadioButton>(R.id.rbTransPrimary)
            val cbTransEnable = rowView.findViewById<CheckBox>(R.id.cbTransEnable)

            tvTransLabel.setTextColor(Color.parseColor(colors.titleColor))
            tvTransLabel.text = "${trans.code} - ${trans.name}"

            radioButtonsMap[trans.code] = rbTransPrimary
            checkBoxesMap[trans.code] = cbTransEnable

            rbTransPrimary.setOnClickListener {
                if (!currentPrimaryTranslation.equals(trans.code, ignoreCase = true)) {
                    currentPrimaryTranslation = trans.code
                    enabledTranslations.add(trans.code)
                    saveTranslationPreferences()
                    syncTranslationCheckUI(radioButtonsMap, checkBoxesMap)
                    loadCurrentChapter()
                    cachedSearchDialog = null
                }
            }

            cbTransEnable.setOnCheckedChangeListener { _, isChecked ->
                if (!trans.code.equals(currentPrimaryTranslation, ignoreCase = true)) {
                    if (isChecked) enabledTranslations.add(trans.code) else enabledTranslations.remove(trans.code)
                    saveTranslationPreferences()
                    loadCurrentChapter()
                }
            }

            containerTranslations?.addView(rowView)
        }

        syncTranslationCheckUI(radioButtonsMap, checkBoxesMap)

        bottomSheetDialog.setContentView(dialogView)
        bottomSheetDialog.show()
    }

    private fun syncTranslationCheckUI(
        radioButtonsMap: Map<String, RadioButton>,
        checkBoxesMap: Map<String, CheckBox>
    ) {
        radioButtonsMap.forEach { (code, rb) ->
            rb.isChecked = code.equals(currentPrimaryTranslation, ignoreCase = true)
        }
        checkBoxesMap.forEach { (code, cb) ->
            val isPrimary = code.equals(currentPrimaryTranslation, ignoreCase = true)
            cb.isChecked = isPrimary || enabledTranslations.contains(code)
            cb.isEnabled = !isPrimary
        }
    }

    private fun saveTranslationPreferences() {
        getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
            .edit()
            .putString(KEY_PRIMARY_TRANS, currentPrimaryTranslation)
            .putStringSet(KEY_ENABLED_TRANS, enabledTranslations)
            .apply()
    }

    private fun showVerseCompareModal(bookCode: String, chapter: Int, verseNum: Int) {
        val bottomSheetDialog = BottomSheetDialog(this)
        val dialogView = LayoutInflater.from(this).inflate(
            R.layout.dialog_verse_compare,
            findViewById(android.R.id.content),
            false
        )

        val colors = getModalThemeColors(currentTheme)

        val sheetContainer = dialogView.findViewById<LinearLayout>(R.id.compareSheetContainer)
        val vDragHandle = dialogView.findViewById<View>(R.id.vDragHandle)
        val tvCompareTitle = dialogView.findViewById<TextView>(R.id.tvCompareTitle)
        val tvCompareSubtitle = dialogView.findViewById<TextView>(R.id.tvCompareSubtitle)
        val vCompareDivider = dialogView.findViewById<View>(R.id.vCompareDivider)
        val layoutNoSecondary = dialogView.findViewById<LinearLayout>(R.id.layoutNoSecondary)
        val tvNoSecondaryMsg = dialogView.findViewById<TextView>(R.id.tvNoSecondaryMsg)
        val btnManageTranslationsPrompt = dialogView.findViewById<Button>(R.id.btnManageTranslationsPrompt)
        val containerCompareVerses = dialogView.findViewById<LinearLayout>(R.id.containerCompareVerses)

        sheetContainer?.setBackgroundColor(Color.parseColor(colors.sheetBg))
        vDragHandle?.setBackgroundColor(Color.parseColor(colors.dragHandleColor))
        tvCompareTitle?.setTextColor(Color.parseColor(colors.titleColor))
        tvCompareSubtitle?.setTextColor(Color.parseColor(colors.subtitleColor))
        vCompareDivider?.setBackgroundColor(Color.parseColor(colors.dividerColor))
        tvNoSecondaryMsg?.setTextColor(Color.parseColor(colors.previewTextColor))

        val localizedBookName = getLocalizedBookName(bookCode, currentPrimaryTranslation)
        tvCompareSubtitle?.text = "$localizedBookName $chapter:$verseNum"

        btnManageTranslationsPrompt.setOnClickListener {
            bottomSheetDialog.dismiss()
            showAboutModal()
        }

        executor.execute {
            // Query compare verses EXCLUDING primary translation
            val compareVerses = dbHelper.getCompareVerses(bookCode, chapter, verseNum, currentPrimaryTranslation, enabledTranslations)

            runOnUiThread {
                if (compareVerses.isEmpty()) {
                    layoutNoSecondary.visibility = View.VISIBLE
                    containerCompareVerses.visibility = View.GONE
                } else {
                    layoutNoSecondary.visibility = View.GONE
                    containerCompareVerses.visibility = View.VISIBLE

                    val inflater = LayoutInflater.from(this@MainActivity)
                    containerCompareVerses.removeAllViews()

                    for (item in compareVerses) {
                        val itemView = inflater.inflate(R.layout.item_compare_verse, containerCompareVerses, false)
                        val tvTransName = itemView.findViewById<TextView>(R.id.tvTransName)
                        val tvTransText = itemView.findViewById<TextView>(R.id.tvTransText)
                        val btnSetPrimary = itemView.findViewById<TextView>(R.id.btnSetPrimary)
                        val vCompareItemDivider = itemView.findViewById<View>(R.id.vCompareItemDivider)

                        tvTransName.setTextColor(Color.parseColor(colors.titleColor))
                        tvTransName.text = item.translationName

                        if (item.text.isBlank()) {
                            val missingHtml = "<i class=\"missing-text\">Text is not available</i>"
                            tvTransText.text = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                Html.fromHtml(missingHtml, Html.FROM_HTML_MODE_LEGACY)
                            } else {
                                @Suppress("DEPRECATION")
                                Html.fromHtml(missingHtml)
                            }
                            tvTransText.setTextColor(Color.parseColor(colors.subtitleColor))
                        } else {
                            tvTransText.text = item.text
                            tvTransText.setTextColor(Color.parseColor(colors.previewTextColor))
                        }

                        if (isRtlText(item.text)) {
                            tvTransText.gravity = Gravity.END
                            tvTransText.textDirection = View.TEXT_DIRECTION_RTL
                        } else {
                            tvTransText.gravity = Gravity.START
                            tvTransText.textDirection = View.TEXT_DIRECTION_LTR
                        }

                        btnSetPrimary.setTextColor(Color.parseColor(colors.refTitleColor))
                        btnSetPrimary.text = "Switch Primary to ${item.translationCode}"
                        vCompareItemDivider.setBackgroundColor(Color.parseColor(colors.dividerColor))

                        btnSetPrimary.setOnClickListener {
                            currentPrimaryTranslation = item.translationCode
                            enabledTranslations.add(item.translationCode)
                            saveTranslationPreferences()
                            loadCurrentChapter()
                            cachedSearchDialog = null
                            bottomSheetDialog.dismiss()
                            Toast.makeText(this@MainActivity, "Primary Translation: ${item.translationName}", Toast.LENGTH_SHORT).show()
                        }

                        containerCompareVerses.addView(itemView)
                    }
                }

                bottomSheetDialog.setContentView(dialogView)
                bottomSheetDialog.show()
            }
        }
    }

    private fun focusSearchAndOpenKeyboard(etSearchQuery: EditText) {
        etSearchQuery.requestFocus()
        etSearchQuery.postDelayed({
            etSearchQuery.requestFocus()
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            @Suppress("DEPRECATION")
            imm.showSoftInput(etSearchQuery, InputMethodManager.SHOW_IMPLICIT)
        }, 150)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun showSearchModal() {
        if (cachedSearchDialog != null) {
            cachedSearchDialog?.show()
            val etQuery = cachedSearchDialog?.findViewById<EditText>(R.id.etSearchQuery)
            if (etQuery != null) {
                focusSearchAndOpenKeyboard(etQuery)
            }
            return
        }

        val searchDialog = Dialog(this, R.style.FullScreenDialogTheme)
        val dialogView = LayoutInflater.from(this).inflate(
            R.layout.dialog_search,
            null,
            false
        )

        val colors = getModalThemeColors(currentTheme)

        dialogView.findViewById<View>(R.id.searchContainer)?.setBackgroundColor(Color.parseColor(colors.sheetBg))
        dialogView.findViewById<View>(R.id.searchTopBar)?.setBackgroundColor(Color.parseColor(colors.expandedGridBg))
        dialogView.findViewById<TextView>(R.id.tvSearchModalTitle)?.setTextColor(Color.parseColor(colors.titleColor))
        dialogView.findViewById<TextView>(R.id.tvActiveSearchTrans)?.text = currentPrimaryTranslation
        dialogView.findViewById<View>(R.id.vTopBarDivider)?.setBackgroundColor(Color.parseColor(colors.dividerColor))

        val btnCloseSearch = dialogView.findViewById<ImageButton>(R.id.btnCloseSearch)
        btnCloseSearch?.setColorFilter(Color.parseColor(colors.titleColor))
        btnCloseSearch?.setOnClickListener { searchDialog.dismiss() }

        dialogView.findViewById<View>(R.id.searchBoxRow)?.setBackgroundColor(Color.parseColor(colors.chipBg))
        dialogView.findViewById<ImageView>(R.id.ivSearchIcon)?.setColorFilter(Color.parseColor(colors.subtitleColor))
        dialogView.findViewById<ImageButton>(R.id.btnClearQuery)?.setColorFilter(Color.parseColor(colors.subtitleColor))
        dialogView.findViewById<View>(R.id.vSearchDivider)?.setBackgroundColor(Color.parseColor(colors.dividerColor))

        val etSearchQuery = dialogView.findViewById<EditText>(R.id.etSearchQuery)
        val btnClearQuery = dialogView.findViewById<ImageButton>(R.id.btnClearQuery)
        val containerDirectJumps = dialogView.findViewById<LinearLayout>(R.id.containerDirectJumps)

        val layoutSearchStatsBar = dialogView.findViewById<LinearLayout>(R.id.layoutSearchStatsBar)
        val tvStatsPrefix = dialogView.findViewById<TextView>(R.id.tvStatsPrefix)
        val tvStatsOt = dialogView.findViewById<TextView>(R.id.tvStatsOt)
        val btnToggleOtStats = dialogView.findViewById<TextView>(R.id.btnToggleOtStats)
        val tvStatsNt = dialogView.findViewById<TextView>(R.id.tvStatsNt)
        val btnToggleNtStats = dialogView.findViewById<TextView>(R.id.btnToggleNtStats)

        // Apply theme colors to stats bar elements so they stand out clearly in Dark Mode
        tvStatsPrefix.setTextColor(Color.parseColor(colors.chipTextColor))
        tvStatsOt.setTextColor(Color.parseColor(colors.previewTextColor))
        btnToggleOtStats.setTextColor(Color.parseColor(colors.chipTextColor))
        tvStatsNt.setTextColor(Color.parseColor(colors.previewTextColor))
        btnToggleNtStats.setTextColor(Color.parseColor(colors.chipTextColor))

        val layoutPaginationBar = dialogView.findViewById<LinearLayout>(R.id.layoutPaginationBar)
        val btnPrevPage = dialogView.findViewById<TextView>(R.id.btnPrevPage)
        val tvPageIndicator = dialogView.findViewById<TextView>(R.id.tvPageIndicator)
        val btnNextPage = dialogView.findViewById<TextView>(R.id.btnNextPage)

        val tvResultCount = dialogView.findViewById<TextView>(R.id.tvResultCount)
        val rvSearchResults = dialogView.findViewById<RecyclerView>(R.id.rvSearchResults)

        tvResultCount.setTextColor(Color.parseColor(colors.subtitleColor))
        btnPrevPage.setTextColor(Color.parseColor(colors.chipTextColor))
        tvPageIndicator.setTextColor(Color.parseColor(colors.titleColor))
        btnNextPage.setTextColor(Color.parseColor(colors.chipTextColor))

        etSearchQuery.setTextColor(Color.parseColor(colors.titleColor))
        etSearchQuery.setHintTextColor(Color.parseColor(colors.subtitleColor))

        rvSearchResults.layoutManager = LinearLayoutManager(this)

        val adapter = SearchResultAdapter(colors, primaryTranslationCode = currentPrimaryTranslation) { result ->
            val bookIdx = BIBLE_BOOKS.indexOfFirst { it.code == result.bookCode }
            if (bookIdx >= 0) {
                navHistory.add(NavState(currentBookIndex, currentChapter, 1, isSearchJump = true))
                currentBookIndex = bookIdx
                currentChapter = result.chapter
                loadCurrentChapter(targetVerseNum = result.verse)
                searchDialog.dismiss()
            }
        }
        rvSearchResults.adapter = adapter

        btnClearQuery.setOnClickListener {
            etSearchQuery.setText("")
        }

        var activeFilterBookCode: String? = null
        var activeSearchPage = 1
        var lastQueryResult: BibleDatabaseHelper.AdvancedSearchResult? = null

        fun executeSearchQuery(queryText: String, targetPage: Int) {
            val letterDigitCount = queryText.count { it.isLetterOrDigit() }
            val isPunctuationOnly = letterDigitCount < 1

            executor.execute {
                val advResult = dbHelper.searchVersesAdvanced(
                    translationCode = currentPrimaryTranslation,
                    query = queryText,
                    filterBookCode = activeFilterBookCode,
                    page = targetPage,
                    pageSize = 10
                )
                lastQueryResult = advResult
                activeSearchPage = advResult.currentPage

                runOnUiThread {
                    val directJumps = dbHelper.parseDirectReferences(queryText, currentPrimaryTranslation)
                    val searchTerm = queryText.replace(Regex("\\s*\\d+[:\\s]*\\d*$"), "").trim()

                    if (advResult.totalCount > 0 && directJumps.isEmpty() && !isPunctuationOnly) {
                        layoutSearchStatsBar.visibility = View.VISIBLE
                        tvStatsPrefix.text = "$currentPrimaryTranslation: "
                        tvStatsOt.text = "OT - ${advResult.otCount}"
                        tvStatsNt.text = "NT - ${advResult.ntCount}"

                        if (activeFilterBookCode != null) {
                            val locName = getLocalizedBookName(activeFilterBookCode!!, currentPrimaryTranslation)
                            tvResultCount.text = "Showing ${advResult.pageResults.size} verses for $locName (Total ${advResult.totalCount})"
                        } else if (searchTerm.isNotEmpty()) {
                            tvResultCount.text = "Showing ${advResult.pageResults.size} verses for \"$searchTerm\" (Total ${advResult.totalCount})"
                        } else {
                            tvResultCount.text = "Showing ${advResult.pageResults.size} verses (Total ${advResult.totalCount})"
                        }

                        // Pagination Bar logic
                        if (advResult.totalPages > 1) {
                            layoutPaginationBar.visibility = View.VISIBLE
                            tvPageIndicator.text = "Page ${advResult.currentPage} of ${advResult.totalPages}"
                            btnPrevPage.isEnabled = advResult.currentPage > 1
                            btnPrevPage.alpha = if (advResult.currentPage > 1) 1.0f else 0.4f
                            btnNextPage.isEnabled = advResult.currentPage < advResult.totalPages
                            btnNextPage.alpha = if (advResult.currentPage < advResult.totalPages) 1.0f else 0.4f
                        } else {
                            layoutPaginationBar.visibility = View.GONE
                        }
                    } else {
                        layoutSearchStatsBar.visibility = View.GONE
                        layoutPaginationBar.visibility = View.GONE
                        if (directJumps.isEmpty()) {
                            tvResultCount.text = "No verses found for \"$queryText\""
                        }
                    }

                    adapter.updateResults(advResult.pageResults, queryText)
                }
            }
        }

        btnPrevPage.setOnClickListener {
            val queryText = etSearchQuery.text?.toString()?.trim().orEmpty()
            if (queryText.isNotEmpty() && activeSearchPage > 1) {
                executeSearchQuery(queryText, activeSearchPage - 1)
            }
        }

        btnNextPage.setOnClickListener {
            val queryText = etSearchQuery.text?.toString()?.trim().orEmpty()
            if (queryText.isNotEmpty() && lastQueryResult != null && activeSearchPage < lastQueryResult!!.totalPages) {
                executeSearchQuery(queryText, activeSearchPage + 1)
            }
        }

        // Swipe Gesture Detector on RecyclerView to navigate pages
        val swipeGestureDetector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
            override fun onFling(e1: MotionEvent?, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
                if (e1 != null && abs(velocityX) > abs(velocityY) && abs(velocityX) > 200) {
                    val deltaX = e2.x - e1.x
                    val queryText = etSearchQuery.text?.toString()?.trim().orEmpty()
                    if (queryText.isNotEmpty() && lastQueryResult != null && lastQueryResult!!.totalPages > 1) {
                        if (deltaX < -100 && activeSearchPage < lastQueryResult!!.totalPages) {
                            // Swipe Left -> Next Page
                            executeSearchQuery(queryText, activeSearchPage + 1)
                            return true
                        } else if (deltaX > 100 && activeSearchPage > 1) {
                            // Swipe Right -> Prev Page
                            executeSearchQuery(queryText, activeSearchPage - 1)
                            return true
                        }
                    }
                }
                return false
            }
        })

        rvSearchResults.addOnItemTouchListener(object : RecyclerView.SimpleOnItemTouchListener() {
            override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
                swipeGestureDetector.onTouchEvent(e)
                return false
            }
        })

        fun showBookBreakdownModal(isOt: Boolean, bookCountsMap: Map<String, Int>) {
            val breakdownDialog = BottomSheetDialog(this@MainActivity)
            val container = LinearLayout(this@MainActivity).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(32, 32, 32, 32)
                setBackgroundColor(Color.parseColor(colors.sheetBg))
            }
            
            val title = TextView(this@MainActivity).apply {
                text = if (isOt) "Old Testament Books" else "New Testament Books"
                setTextColor(Color.parseColor(colors.titleColor))
                textSize = 18f
                setTypeface(null, Typeface.BOLD)
                setPadding(0, 0, 0, 24)
            }
            container.addView(title)

            val targetBooks = if (isOt) BIBLE_BOOKS.subList(0, 39) else BIBLE_BOOKS.subList(39, 66)
            var countInScope = 0

            var rowLayout = LinearLayout(this@MainActivity).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }
            container.addView(rowLayout)

            for (book in targetBooks) {
                val cnt = bookCountsMap[book.code] ?: 0
                if (cnt > 0) {
                    countInScope++
                    val chipBtn = TextView(this@MainActivity).apply {
                        val locName = getLocalizedBookName(book.code, currentPrimaryTranslation)
                        text = "$locName ($cnt)"
                        setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
                        setTextColor(Color.parseColor(colors.chipTextColor))
                        setPadding(16, 16, 16, 16)
                        layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                            setMargins(8, 8, 8, 8)
                        }
                        setBackgroundColor(Color.parseColor(colors.chipBg))
                        gravity = Gravity.CENTER
                        
                        setOnClickListener {
                            activeFilterBookCode = if (activeFilterBookCode == book.code) null else book.code
                            val queryText = etSearchQuery.text?.toString()?.trim().orEmpty()
                            if (queryText.isNotEmpty()) {
                                executeSearchQuery(queryText, 1)
                            }
                            breakdownDialog.dismiss()
                        }
                    }

                    rowLayout.addView(chipBtn)
                    if (rowLayout.childCount >= 2) { // 2 items per row
                        rowLayout = LinearLayout(this@MainActivity).apply {
                            orientation = LinearLayout.HORIZONTAL
                            layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            )
                        }
                        container.addView(rowLayout)
                    }
                }
            }

            // Pad the last row if it has only 1 item to maintain column width
            if (rowLayout.childCount == 1) {
                val dummy = View(this@MainActivity).apply {
                    layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                        setMargins(8, 8, 8, 8)
                    }
                }
                rowLayout.addView(dummy)
            }

            if (countInScope > 0) {
                val scrollView = NestedScrollView(this@MainActivity).apply {
                    addView(container)
                }
                breakdownDialog.setContentView(scrollView)
                breakdownDialog.show()
            }
        }

        btnToggleOtStats.setOnClickListener {
            if (lastQueryResult != null) {
                showBookBreakdownModal(isOt = true, lastQueryResult!!.bookCounts)
            }
        }

        btnToggleNtStats.setOnClickListener {
            if (lastQueryResult != null) {
                showBookBreakdownModal(isOt = false, lastQueryResult!!.bookCounts)
            }
        }

        // Onscreen keyboard Enter/Search action listener to execute jump or minimize keyboard
        etSearchQuery.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_NULL) {
                // Minimize onscreen keyboard
                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(etSearchQuery.windowToken, 0)
                etSearchQuery.clearFocus()

                val queryText = etSearchQuery.text?.toString()?.trim().orEmpty()
                val directJumps = dbHelper.parseDirectReferences(queryText, currentPrimaryTranslation)
                if (directJumps.isNotEmpty()) {
                    val jump = directJumps.first()
                    navHistory.add(NavState(currentBookIndex, currentChapter, 1, isSearchJump = true))
                    currentBookIndex = jump.bookIndex
                    currentChapter = jump.chapter
                    loadCurrentChapter(targetVerseNum = jump.verse)
                    searchDialog.dismiss()
                    return@setOnEditorActionListener true
                }
            }
            false
        }

        etSearchQuery.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val query = s?.toString()?.trim().orEmpty()
                activeFilterBookCode = null

                if (query.isEmpty()) {
                    btnClearQuery.visibility = View.GONE
                    containerDirectJumps.visibility = View.GONE
                    layoutSearchStatsBar.visibility = View.GONE
                    layoutPaginationBar.visibility = View.GONE
                    tvResultCount.text = "Type a reference (mar 1 3) or word (love..)"
                    adapter.updateResults(emptyList(), "")
                    return
                }

                btnClearQuery.visibility = View.VISIBLE

                // Step 1: Direct Reference Jump Matches (Supports multiple matching books e.g. ma 3 2 -> Matthew 3:2, Mark 3:2, Malachi 3:2)
                val directJumps = dbHelper.parseDirectReferences(query, currentPrimaryTranslation)
                containerDirectJumps.removeAllViews()

                if (directJumps.isNotEmpty()) {
                    containerDirectJumps.visibility = View.VISIBLE
                    val inflater = LayoutInflater.from(this@MainActivity)

                    for (jump in directJumps) {
                        val cardView = inflater.inflate(R.layout.item_direct_jump, containerDirectJumps, false)
                        val tvRef = cardView.findViewById<TextView>(R.id.tvDirectJumpReference)
                        val tvCnt = cardView.findViewById<TextView>(R.id.tvDirectJumpChapterCount)

                        tvRef.text = "${jump.bookName} ${jump.chapter}:${jump.verse}"
                        tvCnt.text = "${jump.bookName} (${jump.totalChapters} Chapters)"

                        cardView.setOnClickListener {
                            navHistory.add(NavState(currentBookIndex, currentChapter, 1, isSearchJump = true))
                            currentBookIndex = jump.bookIndex
                            currentChapter = jump.chapter
                            loadCurrentChapter(targetVerseNum = jump.verse)
                            searchDialog.dismiss()
                        }

                        containerDirectJumps.addView(cardView)
                    }
                } else {
                    containerDirectJumps.visibility = View.GONE
                }

                executeSearchQuery(query, 1)
            }
        })

        searchDialog.setContentView(dialogView)
        searchDialog.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT
        )
        searchDialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)

        searchDialog.setOnShowListener {
            focusSearchAndOpenKeyboard(etSearchQuery)
        }

        cachedSearchDialog = searchDialog
        searchDialog.show()
    }

    private fun showBookPickerModal() {
        val bottomSheetDialog = BottomSheetDialog(this)
        val dialogView = LayoutInflater.from(this).inflate(
            R.layout.dialog_book_picker,
            findViewById(android.R.id.content),
            false
        )

        val colors = getModalThemeColors(currentTheme)
        dialogView.findViewById<View>(R.id.pickerContainer)?.setBackgroundColor(Color.parseColor(colors.sheetBg))
        dialogView.findViewById<View>(R.id.vDragHandle)?.setBackgroundColor(Color.parseColor(colors.dragHandleColor))
        dialogView.findViewById<TextView>(R.id.tvPickerTitle)?.setTextColor(Color.parseColor(colors.titleColor))
        dialogView.findViewById<View>(R.id.vPickerDivider)?.setBackgroundColor(Color.parseColor(colors.dividerColor))

        val rvBooks = dialogView.findViewById<RecyclerView>(R.id.rvBooks)
        rvBooks.layoutManager = LinearLayoutManager(this)

        val adapter = BookRecyclerAdapter(
            context = this,
            initialExpandedPosition = currentBookIndex,
            themeColors = colors,
            primaryTranslationCode = currentPrimaryTranslation
        ) { selectedBookIdx, selectedChapterNum ->
            currentBookIndex = selectedBookIdx
            currentChapter = selectedChapterNum
            loadCurrentChapter()
            bottomSheetDialog.dismiss()
        }

        rvBooks.adapter = adapter

        bottomSheetDialog.setContentView(dialogView)

        // Expand bottom sheet height for comfortable scrolling
        bottomSheetDialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        bottomSheetDialog.behavior.peekHeight = (resources.displayMetrics.heightPixels * 0.85).toInt()

        bottomSheetDialog.show()

        // Scroll to currently active book
        if (currentBookIndex >= 0 && currentBookIndex < adapter.itemCount) {
            rvBooks.scrollToPosition(currentBookIndex)
        }
    }

    private fun navigateNextChapter() {
        val bookInfo = BIBLE_BOOKS[currentBookIndex]
        if (currentChapter < bookInfo.totalChapters) {
            currentChapter++
            loadCurrentChapter()
            showOnTheFlyChapterHUD()
        } else if (currentBookIndex < BIBLE_BOOKS.size - 1) {
            currentBookIndex++
            currentChapter = 1
            loadCurrentChapter()
            showOnTheFlyChapterHUD()
        } else {
            Toast.makeText(this, "Reached the end of Revelation", Toast.LENGTH_SHORT).show()
        }
    }

    private fun navigatePreviousChapter() {
        if (currentChapter > 1) {
            currentChapter--
            loadCurrentChapter()
            showOnTheFlyChapterHUD()
        } else if (currentBookIndex > 0) {
            currentBookIndex--
            currentChapter = BIBLE_BOOKS[currentBookIndex].totalChapters
            loadCurrentChapter()
            showOnTheFlyChapterHUD()
        } else {
            Toast.makeText(this, "Reached the beginning of Genesis", Toast.LENGTH_SHORT).show()
        }
    }

    // JavaScript Bridge connecting WebView clicks & gestures to Android Native code
    inner class CrossReferenceInterface {
        @SuppressLint("MissingPermission")
        @JavascriptInterface
        fun vibrateHaptic() {
            runOnUiThread {
                val vibrator = getSystemService(VIBRATOR_SERVICE) as? Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(VibrationEffect.createOneShot(40, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(40)
                }
            }
        }

        @JavascriptInterface
        fun saveCompareOrder(orderCsv: String) {
            runOnUiThread {
                val newOrderList = orderCsv.split(",").map { it.trim().uppercase() }.filter { it.isNotEmpty() }
                if (newOrderList.isNotEmpty()) {
                    getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                        .edit()
                        .putString(KEY_COMPARE_ORDER, orderCsv)
                        .apply()
                    loadCurrentChapter()
                    Toast.makeText(this@MainActivity, "Compare Order Updated", Toast.LENGTH_SHORT).show()
                }
            }
        }

        @JavascriptInterface
        fun onCrossReferenceClicked(anchor: String, sourceRef: String, referencesStr: String) {
            runOnUiThread {
                showBottomSheet(anchor, sourceRef, referencesStr)
            }
        }

        @JavascriptInterface
        fun onSwitchPrimaryTranslation(translationCode: String) {
            runOnUiThread {
                if (!currentPrimaryTranslation.equals(translationCode, ignoreCase = true)) {
                    currentPrimaryTranslation = translationCode
                    enabledTranslations.add(translationCode)
                    saveTranslationPreferences()
                    loadCurrentChapter()
                    cachedSearchDialog = null
                    val transName = TRANSLATION_MAP[translationCode.uppercase()] ?: translationCode
                    Toast.makeText(this@MainActivity, "Primary Translation: $transName", Toast.LENGTH_SHORT).show()
                }
            }
        }

        @JavascriptInterface
        fun onVerseNumberClicked(bookCode: String, chapter: Int, verseNum: Int) {
            runOnUiThread {
                showVerseCompareModal(bookCode, chapter, verseNum)
            }
        }

        @JavascriptInterface
        fun onVerseCrossRefClicked(bookCode: String, chapter: Int, verseNum: Int) {
            runOnUiThread {
                // Fetch cross references for this verse
                executor.execute {
                    val crossRefs = dbHelper.getCrossReferences(bookCode, chapter).filter { it.verse == verseNum }
                    if (crossRefs.isNotEmpty()) {
                        val refList = crossRefs.flatMap { it.references.split("|") }.map { it.trim() }.filter { it.isNotEmpty() }
                        val referencesStr = refList.joinToString("|")
                        runOnUiThread {
                            val localizedBook = getLocalizedBookName(bookCode, currentPrimaryTranslation)
                            showBottomSheet("Verse $verseNum", "$localizedBook $chapter:$verseNum", referencesStr)
                        }
                    }
                }
            }
        }

        @JavascriptInterface
        fun onNextChapter() {
            runOnUiThread {
                navigateNextChapter()
            }
        }

        @JavascriptInterface
        fun onPreviousChapter() {
            runOnUiThread {
                navigatePreviousChapter()
            }
        }

        @JavascriptInterface
        fun saveFontSize(fontSize: Float) {
            currentFontSize = fontSize
            getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                .edit()
                .putFloat(KEY_FONT_SIZE, fontSize)
                .apply()
        }
    }

    private fun buildInteractiveReferenceSpannable(
        rawRef: String,
        localizedRef: String,
        linkColor: Int,
        isRtl: Boolean,
        onVerseClick: (verseNum: Int) -> Unit
    ): SpannableStringBuilder {
        val formattedText = if (isRtl) "\u2067$localizedRef\u2069" else localizedRef
        val ssb = SpannableStringBuilder(formattedText)
        val colonIdx = formattedText.indexOf(':')
        val firstVerseNum = rawRef.substringAfter(':', "").split(",", "-").firstOrNull()?.filter { it.isDigit() }?.toIntOrNull() ?: 1

        if (colonIdx > 0) {
            val prefixSpan = object : ClickableSpan() {
                override fun onClick(widget: View) {
                    onVerseClick(firstVerseNum)
                }
                override fun updateDrawState(ds: TextPaint) {
                    ds.color = linkColor
                    ds.isUnderlineText = false
                }
            }
            ssb.setSpan(prefixSpan, 0, colonIdx + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

            val digitRegex = Regex("\\b\\d+\\b")
            val matches = digitRegex.findAll(formattedText, startIndex = colonIdx + 1)

            for (match in matches) {
                val vNum = match.value.toIntOrNull() ?: continue
                val start = match.range.first
                val end = match.range.last + 1

                val vSpan = object : ClickableSpan() {
                    override fun onClick(widget: View) {
                        onVerseClick(vNum)
                    }
                    override fun updateDrawState(ds: TextPaint) {
                        ds.color = linkColor
                        ds.isUnderlineText = true
                    }
                }
                ssb.setSpan(vSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        } else {
            val wholeSpan = object : ClickableSpan() {
                override fun onClick(widget: View) {
                    onVerseClick(firstVerseNum)
                }
                override fun updateDrawState(ds: TextPaint) {
                    ds.color = linkColor
                    ds.isUnderlineText = false
                }
            }
            ssb.setSpan(wholeSpan, 0, formattedText.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        return ssb
    }

    // Displays clicked cross references in a Bottom Sheet with verse text previews
    @SuppressLint("ClickableViewAccessibility", "InflateParams")
    private fun showBottomSheet(anchor: String, sourceRef: String, referencesStr: String) {
        val bottomSheetDialog = BottomSheetDialog(this)
        val view = LayoutInflater.from(this).inflate(
            R.layout.bottom_sheet_cross_ref,
            null,
            false
        )

        val colors = getModalThemeColors(currentTheme)

        val sheetContainer = view.findViewById<LinearLayout>(R.id.sheetContainer)
        val vDragHandle = view.findViewById<View>(R.id.vDragHandle)
        val tvTitle = view.findViewById<TextView>(R.id.tvTitle)
        val tvSourceVerse = view.findViewById<TextView>(R.id.tvSourceVerse)
        val vDivider = view.findViewById<View>(R.id.vDivider)
        val scrollXrefs = view.findViewById<NestedScrollView>(R.id.scrollXrefs)
        val containerXrefs = view.findViewById<LinearLayout>(R.id.containerXrefs)

        sheetContainer?.setBackgroundColor(Color.parseColor(colors.sheetBg))
        vDragHandle?.setBackgroundColor(Color.parseColor(colors.dragHandleColor))
        tvTitle?.setTextColor(Color.parseColor(colors.titleColor))
        tvTitle?.text = "Cross References for \"$anchor\""
        tvSourceVerse?.setTextColor(Color.parseColor(colors.subtitleColor))

        val formattedSourceRef = dbHelper.formatLocalizedReference(sourceRef, currentPrimaryTranslation)
        val isSourceRtl = isRtlText(formattedSourceRef)
        tvSourceVerse?.text = if (isSourceRtl) "\u2067$formattedSourceRef\u2069" else formattedSourceRef
        if (isSourceRtl) {
            tvSourceVerse?.gravity = Gravity.END
            tvSourceVerse?.textDirection = View.TEXT_DIRECTION_RTL
        } else {
            tvSourceVerse?.gravity = Gravity.START
            tvSourceVerse?.textDirection = View.TEXT_DIRECTION_LTR
        }

        vDivider?.setBackgroundColor(Color.parseColor(colors.dividerColor))

        // Extract source verse number to push onto navigation stack when user jumps to a cross reference
        val sourceVerseNum = sourceRef.substringAfterLast(':').filter { it.isDigit() }.toIntOrNull() ?: 1

        // Pinch-to-zoom scaling for Cross Reference preview items
        var dynamicXrefFontSize = currentFontSize

        fun updateXrefFontSizes(baseSize: Float) {
            val titleSize = (baseSize - 0.5f).coerceAtLeast(12f)
            val previewSize = (baseSize - 1.5f).coerceAtLeast(11f)

            if (containerXrefs != null) {
                for (i in 0 until containerXrefs.childCount) {
                    val itemView = containerXrefs.getChildAt(i) ?: continue
                    val tvRef = itemView.findViewById<TextView>(R.id.tvReference)
                    val tvPrev = itemView.findViewById<TextView>(R.id.tvPreviewText)

                    tvRef?.setTextSize(TypedValue.COMPLEX_UNIT_SP, titleSize)
                    tvPrev?.setTextSize(TypedValue.COMPLEX_UNIT_SP, previewSize)
                }
            }
        }

        val scaleDetector = ScaleGestureDetector(this, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            override fun onScale(detector: ScaleGestureDetector): Boolean {
                val factor = detector.scaleFactor
                val newSize = (dynamicXrefFontSize * factor).coerceIn(11f, 36f)
                if (Math.abs(newSize - dynamicXrefFontSize) > 0.1f) {
                    dynamicXrefFontSize = newSize
                    currentFontSize = newSize
                    getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                        .edit()
                        .putFloat(KEY_FONT_SIZE, newSize)
                        .apply()

                    updateXrefFontSizes(dynamicXrefFontSize)
                }
                return true
            }
        })

        scrollXrefs?.setOnTouchListener { _, event ->
            scaleDetector.onTouchEvent(event)
            false
        }

        // Split pipe-separated references (e.g. "Prov 8:22-24|Prov 16:4|John 1:1-3")
        val refList = referencesStr.split("|").map { it.trim() }.filter { it.isNotEmpty() }

        // Scale font sizes proportional to reader font size
        val refTitleFontSize = (currentFontSize - 0.5f).coerceAtLeast(12f)
        val previewFontSize = (currentFontSize - 1.5f).coerceAtLeast(11f)

        executor.execute {
            // Fetch previews from SQLite for each reference item
            val itemDetails = refList.map { ref ->
                val previewText = dbHelper.getVerseTextByRef(currentPrimaryTranslation, ref)
                val localizedRef = dbHelper.formatLocalizedReference(ref, currentPrimaryTranslation)
                Triple(ref, localizedRef, previewText)
            }

            runOnUiThread {
                val inflater = LayoutInflater.from(this@MainActivity)
                val refColor = Color.parseColor(colors.refTitleColor)

                for ((rawRef, localizedRef, preview) in itemDetails) {
                    val itemView = inflater.inflate(R.layout.item_cross_ref, containerXrefs, false)
                    val tvReference = itemView.findViewById<TextView>(R.id.tvReference)
                    val tvPreviewText = itemView.findViewById<TextView>(R.id.tvPreviewText)
                    val itemDivider = itemView.findViewById<View>(R.id.itemDivider)

                    tvReference.movementMethod = LinkMovementMethod.getInstance()
                    tvReference.setTextSize(TypedValue.COMPLEX_UNIT_SP, refTitleFontSize)
                    itemDivider?.setBackgroundColor(Color.parseColor(colors.dividerColor))

                    val isRefRtl = isRtlText(localizedRef) || isRtlText(rawRef)
                    if (isRefRtl) {
                        tvReference.gravity = Gravity.END
                        tvReference.textDirection = View.TEXT_DIRECTION_RTL
                    } else {
                        tvReference.gravity = Gravity.START
                        tvReference.textDirection = View.TEXT_DIRECTION_LTR
                    }

                    // Build interactive title where clicking individual verse numbers jumps to that specific verse
                    tvReference.text = buildInteractiveReferenceSpannable(
                        rawRef = rawRef,
                        localizedRef = localizedRef,
                        linkColor = refColor,
                        isRtl = isRefRtl
                    ) { targetVerseNum ->
                        // Save source state for Return Bar
                        activeCrossRefReturnState = CrossRefReturnState(
                            anchor = anchor,
                            sourceRef = sourceRef,
                            referencesStr = referencesStr,
                            sourceBookIndex = currentBookIndex,
                            sourceChapter = currentChapter,
                            sourceVerse = sourceVerseNum
                        )

                        val localizedSource = dbHelper.formatLocalizedReference(sourceRef, currentPrimaryTranslation)
                        btnReturnToCrossRef.text = "Return to xref ($localizedSource)"
                        cardCrossRefReturnBar.visibility = View.VISIBLE

                        val jumps = dbHelper.parseDirectReferences(rawRef, currentPrimaryTranslation)
                        if (jumps.isNotEmpty()) {
                            val targetJump = jumps.first()
                            currentBookIndex = targetJump.bookIndex
                            currentChapter = targetJump.chapter
                            loadCurrentChapter(targetVerseNum = targetVerseNum)
                            bottomSheetDialog.dismiss()
                        }
                    }

                    if (!preview.isNullOrBlank()) {
                        tvPreviewText.setTextColor(Color.parseColor(colors.previewTextColor))
                        tvPreviewText.setTextSize(TypedValue.COMPLEX_UNIT_SP, previewFontSize)

                        val isPrevRtl = isRtlText(preview)
                        val styledPreview = preview.replace("#B71C1C", colors.refTitleColor)
                        val finalPreviewText = if (isPrevRtl) "\u2067$styledPreview\u2069" else styledPreview

                        tvPreviewText.text = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            Html.fromHtml(finalPreviewText, Html.FROM_HTML_MODE_LEGACY)
                        } else {
                            @Suppress("DEPRECATION")
                            Html.fromHtml(finalPreviewText)
                        }

                        if (isPrevRtl) {
                            tvPreviewText.gravity = Gravity.END
                            tvPreviewText.textDirection = View.TEXT_DIRECTION_RTL
                        } else {
                            tvPreviewText.gravity = Gravity.START
                            tvPreviewText.textDirection = View.TEXT_DIRECTION_LTR
                        }

                        tvPreviewText.visibility = View.VISIBLE
                    } else {
                        tvPreviewText.visibility = View.GONE
                    }

                    containerXrefs.addView(itemView)
                }

                bottomSheetDialog.setContentView(view)
                bottomSheetDialog.show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        executor.shutdown()
    }
}
