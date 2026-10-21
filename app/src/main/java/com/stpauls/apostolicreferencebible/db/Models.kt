package com.stpauls.apostolicreferencebible.db

import android.content.Context
import android.util.Log
import org.json.JSONObject

data class Verse(
    val id: Long = 0,
    val book: String,
    val chapter: Int,
    val verse: Int,
    val text: String
)

data class CrossReference(
    val id: Long = 0,
    val book: String,
    val chapter: Int,
    val verse: Int,
    val anchor: String,
    val references: String
)

data class CrossRefDetail(
    val reference: String,
    val previewText: String?
)

data class BookInfo(
    val code: String,
    val name: String,
    val totalChapters: Int
)

data class SearchResult(
    val bookCode: String,
    val bookName: String,
    val chapter: Int,
    val verse: Int,
    val text: String
)

data class TranslationInfo(
    val code: String,
    val name: String,
    val dbName: String,
    val isPrimary: Boolean,
    val isEnabled: Boolean
)

data class TranslationVerse(
    val translationCode: String,
    val translationName: String,
    val text: String
)

val INSTALLED_TRANSLATIONS = listOf(
    TranslationInfo("KJV", "King James Version", "kjv.db", isPrimary = true, isEnabled = true),
    TranslationInfo("H/G", "Hebrew OT / Greek TR NT", "hgb.db", isPrimary = false, isEnabled = true),
    TranslationInfo("ABG", "Apostolic Polyglot (Greek)", "abg.db", isPrimary = false, isEnabled = true),
    TranslationInfo("ABE", "Apostolic Polyglot (English)", "abe.db", isPrimary = false, isEnabled = true),
    TranslationInfo("LXX", "Thomson Septuagint (English)", "lxx.db", isPrimary = false, isEnabled = true),
    TranslationInfo("T4T", "Translation for Translators", "t4t.db", isPrimary = false, isEnabled = true),
    TranslationInfo("NAS", "NASB (English)", "nas.db", isPrimary = false, isEnabled = true)
)

val TRANSLATION_MAP = INSTALLED_TRANSLATIONS.associate { it.code to it.name }

val BIBLE_BOOKS = listOf(
    BookInfo("GEN", "Genesis", 50),
    BookInfo("EXO", "Exodus", 40),
    BookInfo("LEV", "Leviticus", 27),
    BookInfo("NUM", "Numbers", 36),
    BookInfo("DEU", "Deuteronomy", 34),
    BookInfo("JOS", "Joshua", 24),
    BookInfo("JDG", "Judges", 21),
    BookInfo("RUT", "Ruth", 4),
    BookInfo("1SA", "1 Samuel", 31),
    BookInfo("2SA", "2 Samuel", 24),
    BookInfo("1KI", "1 Kings", 22),
    BookInfo("2KI", "2 Kings", 25),
    BookInfo("1CH", "1 Chronicles", 29),
    BookInfo("2CH", "2 Chronicles", 36),
    BookInfo("EZR", "Ezra", 10),
    BookInfo("NEH", "Nehemiah", 13),
    BookInfo("EST", "Esther", 10),
    BookInfo("JOB", "Job", 42),
    BookInfo("PSA", "Psalms", 150),
    BookInfo("PRO", "Proverbs", 31),
    BookInfo("ECC", "Ecclesiastes", 12),
    BookInfo("SNG", "Song of Solomon", 8),
    BookInfo("ISA", "Isaiah", 66),
    BookInfo("JER", "Jeremiah", 52),
    BookInfo("LAM", "Lamentations", 5),
    BookInfo("EZK", "Ezekiel", 48),
    BookInfo("DAN", "Daniel", 12),
    BookInfo("HOS", "Hosea", 14),
    BookInfo("JOL", "Joel", 3),
    BookInfo("AMO", "Amos", 9),
    BookInfo("OBA", "Obadiah", 1),
    BookInfo("JON", "Jonah", 4),
    BookInfo("MIC", "Micah", 7),
    BookInfo("NAH", "Nahum", 3),
    BookInfo("HAB", "Habakkuk", 3),
    BookInfo("ZEP", "Zephaniah", 3),
    BookInfo("HAG", "Haggai", 2),
    BookInfo("ZEC", "Zechariah", 14),
    BookInfo("MAL", "Malachi", 4),
    BookInfo("MAT", "Matthew", 28),
    BookInfo("MRK", "Mark", 16),
    BookInfo("LUK", "Luke", 24),
    BookInfo("JHN", "John", 21),
    BookInfo("ACT", "Acts", 28),
    BookInfo("ROM", "Romans", 16),
    BookInfo("1CO", "1 Corinthians", 16),
    BookInfo("2CO", "2 Corinthians", 13),
    BookInfo("GAL", "Galatians", 6),
    BookInfo("EPH", "Ephesians", 6),
    BookInfo("PHP", "Philippians", 4),
    BookInfo("COL", "Colossians", 4),
    BookInfo("1TH", "1 Thessalonians", 5),
    BookInfo("2TH", "2 Thessalonians", 3),
    BookInfo("1TI", "1 Timothy", 6),
    BookInfo("2TI", "2 Timothy", 4),
    BookInfo("TIT", "Titus", 3),
    BookInfo("PHM", "Philemon", 1),
    BookInfo("HEB", "Hebrews", 13),
    BookInfo("JAS", "James", 5),
    BookInfo("1PE", "1 Peter", 5),
    BookInfo("2PE", "2 Peter", 3),
    BookInfo("1JN", "1 John", 5),
    BookInfo("2JN", "2 John", 1),
    BookInfo("3JN", "3 John", 1),
    BookInfo("JUD", "Jude", 1),
    BookInfo("REV", "Revelation", 22)
)

val BOOK_NAME_MAP = BIBLE_BOOKS.associate { it.code to it.name }

private var coreBookNamesJsonCache: Map<String, Map<String, String>>? = null

fun loadCoreBookNamesFromJson(context: Context): Map<String, Map<String, String>> {
    coreBookNamesJsonCache?.let { return it }

    val masterMap = mutableMapOf<String, Map<String, String>>()
    try {
        context.assets.open("core_book_names.json").use { inputStream ->
            val jsonText = inputStream.bufferedReader().use { it.readText() }
            val rootObj = JSONObject(jsonText)
            val keys = rootObj.keys()
            while (keys.hasNext()) {
                val langCode = keys.next()
                val langObj = rootObj.getJSONObject(langCode)
                val bookMap = mutableMapOf<String, String>()
                val bookKeys = langObj.keys()
                while (bookKeys.hasNext()) {
                    val bCode = bookKeys.next()
                    bookMap[bCode] = langObj.getString(bCode)
                }
                masterMap[langCode.uppercase()] = bookMap
            }
        }
    } catch (e: Exception) {
        Log.e("Models", "Error reading core_book_names.json", e)
    }

    coreBookNamesJsonCache = masterMap
    return masterMap
}

fun getLocalizedBookName(
    bookCode: String,
    primaryTranslationCode: String,
    dbHelper: BibleDatabaseHelper? = null,
    context: Context? = null
): String {
    // 1. Check custom database book_names table if dbHelper is available
    if (dbHelper != null) {
        val customName = dbHelper.getCustomBookName(primaryTranslationCode, bookCode)
        if (!customName.isNullOrBlank()) {
            return customName
        }
    }

    // 2. Check core JSON book names if context is available
    val code = primaryTranslationCode.uppercase()
    val ctx = context ?: dbHelper?.context
    if (ctx != null) {
        val jsonCatalog = loadCoreBookNamesFromJson(ctx)
        for ((langPrefix, bookMap) in jsonCatalog) {
            if (code.startsWith(langPrefix) || (langPrefix == "HGB" && (code.contains("H/G") || code.contains("HGB")))) {
                val localized = bookMap[bookCode]
                if (!localized.isNullOrBlank()) {
                    return localized
                }
            }
        }
    }

    // 3. Default fallback to English book names
    return BOOK_NAME_MAP[bookCode] ?: bookCode
}
