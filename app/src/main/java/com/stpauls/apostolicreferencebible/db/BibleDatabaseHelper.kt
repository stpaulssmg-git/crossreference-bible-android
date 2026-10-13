package com.stpauls.apostolicreferencebible.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import java.io.FileOutputStream

class BibleDatabaseHelper(private val context: Context) :
    SQLiteOpenHelper(context, "dummy.db", null, 1) {

    companion object {
        private const val TAG = "BibleDatabaseHelper"

        private val BOOK_MAP = mapOf(
            "GEN" to "GEN", "GENESIS" to "GEN",
            "EXOD" to "EXO", "EXO" to "EXO", "EXODUS" to "EXO",
            "LEV" to "LEV", "LEVITICUS" to "LEV",
            "NUM" to "NUM", "NUMBERS" to "NUM",
            "DEUT" to "DEU", "DEU" to "DEU", "DEUTERONOMY" to "DEU",
            "JOSH" to "JOS", "JOS" to "JOS", "JOSHUA" to "JOS",
            "JUDG" to "JDG", "JDG" to "JDG", "JUDGES" to "JDG",
            "RUTH" to "RUT", "RUT" to "RUT",
            "1 SAM" to "1SA", "1SAM" to "1SA", "1SA" to "1SA",
            "2 SAM" to "2SA", "2SAM" to "2SA", "2SA" to "2SA",
            "1 KGS" to "1KI", "1KING" to "1KI", "1KI" to "1KI",
            "2 KGS" to "2KI", "2KING" to "2KI", "2KI" to "2KI",
            "1 CHR" to "1CH", "1CH" to "1CH",
            "2 CHR" to "2CH", "2CH" to "2CH",
            "EZRA" to "EZR", "EZR" to "EZR",
            "NEH" to "NEH", "NEHEMIAH" to "NEH",
            "ESTH" to "EST", "EST" to "EST", "ESTHER" to "EST",
            "JOB" to "JOB",
            "PS" to "PSA", "PSA" to "PSA", "PSALM" to "PSA", "PSALMS" to "PSA",
            "PROV" to "PRO", "PRO" to "PRO", "PROVERBS" to "PRO",
            "ECCL" to "ECC", "ECC" to "ECC", "ECCLESIASTES" to "ECC",
            "SONG" to "SNG", "SNG" to "SNG", "SOL" to "SNG",
            "ISA" to "ISA", "ISAIAH" to "ISA",
            "JER" to "JER", "JEREMIAH" to "JER",
            "LAM" to "LAM", "LAMENTATIONS" to "LAM",
            "EZEK" to "EZK", "EZK" to "EZK", "EZE" to "EZK", "EZEKIEL" to "EZK",
            "DAN" to "DAN", "DANIEL" to "DAN",
            "HOS" to "HOS", "HOSEA" to "HOS", "JOEL" to "JOL", "JOL" to "JOL", "JOE" to "JOL",
            "AMOS" to "AMO", "AMO" to "AMO", "OBAD" to "OBA", "OBA" to "OBA",
            "JONAH" to "JON", "JON" to "JON", "MIC" to "MIC", "MICAH" to "MIC",
            "NAH" to "NAH", "NAM" to "NAH", "NAHUM" to "NAH", "HAB" to "HAB", "ZEPH" to "ZEP", "ZEP" to "ZEP",
            "HAG" to "HAG", "ZECH" to "ZEC", "ZEC" to "ZEC", "MAL" to "MAL",
            "MATT" to "MAT", "MAT" to "MAT", "MATTHEW" to "MAT",
            "MARK" to "MRK", "MRK" to "MRK", "MAR" to "MRK",
            "LUKE" to "LUK", "LUK" to "LUK",
            "JOHN" to "JHN", "JHN" to "JHN", "JOH" to "JHN",
            "ACTS" to "ACT", "ACT" to "ACT",
            "ROM" to "ROM", "ROMANS" to "ROM",
            "1 COR" to "1CO", "1CO" to "1CO", "2 COR" to "2CO", "2CO" to "2CO",
            "GAL" to "GAL", "GALATIANS" to "GAL", "EPH" to "EPH", "EPHESIANS" to "EPH",
            "PHIL" to "PHP", "PHP" to "PHP", "PHI" to "PHP", "PHILIPPIANS" to "PHP",
            "COL" to "COL", "COLOSSIANS" to "COL",
            "1 THESS" to "1TH", "1TH" to "1TH", "2 THESS" to "2TH", "2TH" to "2TH",
            "1 TIM" to "1TI", "1TI" to "1TI", "2 TIM" to "2TI", "2TI" to "2TI",
            "TITUS" to "TIT", "TIT" to "TIT", "PHILEM" to "PHM", "PHM" to "PHM", "PHLM" to "PHM",
            "HEB" to "HEB", "HEBREWS" to "HEB", "JAS" to "JAS", "JAM" to "JAS", "JAMES" to "JAS",
            "1 PET" to "1PE", "1PE" to "1PE", "2 PET" to "2PE", "2PE" to "2PE",
            "1 JOHN" to "1JN", "1JN" to "1JN", "1JO" to "1JN",
            "2 JOHN" to "2JN", "2JN" to "2JN", "2JO" to "2JN",
            "3 JOHN" to "3JN", "3JN" to "3JN", "3JO" to "3JN",
            "JUDE" to "JUD", "JUD" to "JUD",
            "REV" to "REV", "REVELATION" to "REV"
        )
    }

    override fun onCreate(db: SQLiteDatabase) {}
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {}

    fun getDbFileName(translationCode: String): String {
        val info = INSTALLED_TRANSLATIONS.find { it.code.equals(translationCode, ignoreCase = true) }
        return info?.dbName ?: "${translationCode.lowercase().replace("/", "")}.db"
    }

    fun ensureDatabaseCopied(dbFileName: String) {
        val dbPath = context.getDatabasePath(dbFileName)
        if (!dbPath.exists()) {
            Log.d(TAG, "Copying $dbFileName from assets...")
            val startTime = System.currentTimeMillis()
            dbPath.parentFile?.mkdirs()

            try {
                context.assets.open("database/$dbFileName").use { input ->
                    FileOutputStream(dbPath).use { output ->
                        val buffer = ByteArray(8192)
                        var length: Int
                        while (input.read(buffer).also { length = it } > 0) {
                            output.write(buffer, 0, length)
                        }
                        output.flush()
                    }
                }
                Log.d(TAG, "Successfully copied $dbFileName in ${System.currentTimeMillis() - startTime} ms")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to copy $dbFileName from assets", e)
            }
        }
    }

    private fun getReadableDb(dbFileName: String): SQLiteDatabase {
        ensureDatabaseCopied(dbFileName)
        val dbPath = context.getDatabasePath(dbFileName)
        return SQLiteDatabase.openDatabase(dbPath.path, null, SQLiteDatabase.OPEN_READONLY)
    }

    fun normalizeBookCode(bookRaw: String): String {
        val upper = bookRaw.trim().uppercase()
        return BOOK_MAP[upper] ?: upper
    }

    fun formatLocalizedReference(referenceStr: String, primaryTranslationCode: String): String {
        val trimmed = referenceStr.trim()
        val colonIdx = trimmed.indexOf(':')
        if (colonIdx <= 0) return referenceStr

        val beforeColon = trimmed.substring(0, colonIdx).trim()
        val spaceIdx = beforeColon.lastIndexOf(' ')
        if (spaceIdx <= 0) return referenceStr

        val bookRaw = beforeColon.substring(0, spaceIdx).trim()
        val chapterAndVerse = trimmed.substring(spaceIdx + 1).trim()

        val normalizedBook = normalizeBookCode(bookRaw)
        val localizedBookName = getLocalizedBookName(normalizedBook, primaryTranslationCode)

        return "$localizedBookName $chapterAndVerse"
    }

    data class DirectReferenceJump(
        val bookIndex: Int,
        val bookCode: String,
        val bookName: String,
        val chapter: Int,
        val verse: Int,
        val totalChapters: Int
    )

    data class AdvancedSearchResult(
        val results: List<SearchResult>,
        val otCount: Int,
        val ntCount: Int,
        val totalCount: Int,
        val bookCounts: Map<String, Int>,
        val pageResults: List<SearchResult>,
        val currentPage: Int,
        val totalPages: Int
    )

    fun parseDirectReferences(query: String, primaryTranslationCode: String): List<DirectReferenceJump> {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return emptyList()

        val bookRaw: String
        val chapter: Int
        val verse: Int

        val colonIdx = trimmed.indexOf(':')
        if (colonIdx > 0) {
            val beforeColon = trimmed.substring(0, colonIdx).trim()
            val spaceIdx = beforeColon.lastIndexOf(' ')
            if (spaceIdx <= 0) return emptyList()

            bookRaw = beforeColon.substring(0, spaceIdx).trim().uppercase()
            chapter = beforeColon.substring(spaceIdx + 1).trim().toIntOrNull() ?: return emptyList()

            val afterColon = trimmed.substring(colonIdx + 1).trim()
            val firstVersePart = afterColon.split(",", "-").firstOrNull()?.trim()
            verse = firstVersePart?.filter { it.isDigit() }?.toIntOrNull() ?: 1
        } else {
            val regex = Regex("^([1-3]?\\s*[\\p{L}]+)\\s*(\\d+)[\\s:]*(\\d+)?$")
            val match = regex.find(trimmed) ?: return emptyList()

            bookRaw = match.groupValues[1].trim().uppercase()
            val chapterStr = match.groupValues[2].trim()
            val verseStr = match.groupValues.getOrNull(3)?.trim()

            chapter = chapterStr.toIntOrNull() ?: return emptyList()
            verse = if (!verseStr.isNullOrBlank()) verseStr.toIntOrNull() ?: 1 else 1
        }

        val matchedCodes = mutableSetOf<String>()

        // 1. Check exact normalized code match in BOOK_MAP
        val exactCode = BOOK_MAP[bookRaw]
        if (exactCode != null) {
            matchedCodes.add(normalizeBookCode(exactCode))
        }

        // 2. Collect ALL prefix / startsWith matches from localized book maps & English maps
        val activeMap = when (primaryTranslationCode.uppercase()) {
            "KAN" -> KAN_BOOK_NAME_MAP
            "TEL" -> TEL_BOOK_NAME_MAP
            "MAL" -> MAL_BOOK_NAME_MAP
            "TAM" -> TAM_BOOK_NAME_MAP
            "HIN" -> HIN_BOOK_NAME_MAP
            "SPA" -> SPA_BOOK_NAME_MAP
            "H/G", "HGB" -> HGB_BOOK_NAME_MAP
            else -> BOOK_NAME_MAP
        }

        // Add matching codes from active localized map
        activeMap.entries.filter { it.value.uppercase().startsWith(bookRaw) }.forEach {
            matchedCodes.add(normalizeBookCode(it.key))
        }

        // Add matching codes from English BOOK_NAME_MAP
        BOOK_NAME_MAP.entries.filter { it.value.uppercase().startsWith(bookRaw) }.forEach {
            matchedCodes.add(normalizeBookCode(it.key))
        }

        // Add matching codes from BOOK_MAP keys
        BOOK_MAP.entries.filter { it.key.startsWith(bookRaw) }.forEach {
            matchedCodes.add(normalizeBookCode(it.value))
        }

        if (matchedCodes.isEmpty()) return emptyList()

        val jumpsList = mutableListOf<DirectReferenceJump>()

        for (code in matchedCodes) {
            val bookIdx = BIBLE_BOOKS.indexOfFirst { it.code.equals(code, ignoreCase = true) }
            if (bookIdx >= 0) {
                val bookInfo = BIBLE_BOOKS[bookIdx]
                // Verify valid chapter number for this specific book
                if (chapter in 1..bookInfo.totalChapters) {
                    val localizedName = getLocalizedBookName(bookInfo.code, primaryTranslationCode)
                    jumpsList.add(
                        DirectReferenceJump(
                            bookIndex = bookIdx,
                            bookCode = bookInfo.code,
                            bookName = localizedName,
                            chapter = chapter,
                            verse = verse,
                            totalChapters = bookInfo.totalChapters
                        )
                    )
                }
            }
        }

        // Sort jumps by standard biblical order (bookIndex)
        return jumpsList.sortedBy { it.bookIndex }
    }

    fun searchVersesAdvanced(
        translationCode: String,
        query: String,
        filterBookCode: String? = null,
        page: Int = 1,
        pageSize: Int = 10
    ): AdvancedSearchResult {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) {
            return AdvancedSearchResult(emptyList(), 0, 0, 0, emptyMap(), emptyList(), 1, 0)
        }

        val dbFileName = getDbFileName(translationCode)
        val db = getReadableDb(dbFileName)

        val isWildcard = trimmed.contains("..")
        val cleanTerm = trimmed.replace("..", "").trim()
        if (cleanTerm.isEmpty()) {
            return AdvancedSearchResult(emptyList(), 0, 0, 0, emptyMap(), emptyList(), 1, 0)
        }

        val sqlPattern = "%$cleanTerm%"
        val wordRegex = if (isWildcard) {
            Regex("\\b${Regex.escape(cleanTerm)}\\w*", RegexOption.IGNORE_CASE)
        } else {
            Regex("\\b${Regex.escape(cleanTerm)}\\b", RegexOption.IGNORE_CASE)
        }

        val otSet = BIBLE_BOOKS.subList(0, 39).map { it.code }.toSet()
        val bookCountsMap = mutableMapOf<String, Int>()
        val allMatchingVerses = mutableListOf<SearchResult>()

        val cursor = db.query(
            "verses",
            arrayOf("book", "chapter", "verse", "text"),
            "text LIKE ?",
            arrayOf(sqlPattern),
            null, null, "id ASC"
        )

        var otTotal = 0
        var ntTotal = 0

        if (cursor.moveToFirst()) {
            do {
                val bCode = cursor.getString(cursor.getColumnIndexOrThrow("book"))
                val c = cursor.getInt(cursor.getColumnIndexOrThrow("chapter"))
                val v = cursor.getInt(cursor.getColumnIndexOrThrow("verse"))
                val text = cursor.getString(cursor.getColumnIndexOrThrow("text"))

                // Verify whole word / prefix regex match
                if (wordRegex.containsMatchIn(text)) {
                    bookCountsMap[bCode] = (bookCountsMap[bCode] ?: 0) + 1
                    if (otSet.contains(bCode)) {
                        otTotal++
                    } else {
                        ntTotal++
                    }

                    if (filterBookCode == null || filterBookCode.equals(bCode, ignoreCase = true)) {
                        val bName = BOOK_NAME_MAP[bCode] ?: bCode
                        allMatchingVerses.add(SearchResult(bCode, bName, c, v, text))
                    }
                }
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()

        val totalMatches = allMatchingVerses.size
        val totalPages = if (totalMatches > 0) Math.ceil(totalMatches.toDouble() / pageSize).toInt() else 0
        val validatedPage = page.coerceIn(1, Math.max(1, totalPages))

        val startIndex = (validatedPage - 1) * pageSize
        val endIndex = Math.min(startIndex + pageSize, totalMatches)

        val pageResults = if (startIndex < totalMatches) {
            allMatchingVerses.subList(startIndex, endIndex)
        } else {
            emptyList()
        }

        val otSum = if (filterBookCode == null) otTotal else bookCountsMap.filterKeys { otSet.contains(it) }.values.sum()
        val ntSum = if (filterBookCode == null) ntTotal else bookCountsMap.filterKeys { !otSet.contains(it) }.values.sum()

        return AdvancedSearchResult(
            results = allMatchingVerses,
            otCount = otSum,
            ntCount = ntSum,
            totalCount = totalMatches,
            bookCounts = bookCountsMap,
            pageResults = pageResults,
            currentPage = validatedPage,
            totalPages = totalPages
        )
    }

    fun getMaxVerseNumberForChapter(book: String, chapter: Int): Int {
        val normalizedBook = normalizeBookCode(book)
        var maxVerse = 0

        // Query kjv.db first as reference benchmark
        try {
            val db = getReadableDb("kjv.db")
            val cursor = db.rawQuery(
                "SELECT MAX(verse) FROM verses WHERE book = ? AND chapter = ?",
                arrayOf(normalizedBook, chapter.toString())
            )
            if (cursor.moveToFirst()) {
                maxVerse = cursor.getInt(0)
            }
            cursor.close()
            db.close()
        } catch (e: Exception) {
            Log.e(TAG, "Error getting max verse from kjv.db", e)
        }

        if (maxVerse <= 0) {
            // Fallback: check all installed translations to find max verse
            for (trans in INSTALLED_TRANSLATIONS) {
                try {
                    val db = getReadableDb(trans.dbName)
                    val cursor = db.rawQuery(
                        "SELECT MAX(verse) FROM verses WHERE book = ? AND chapter = ?",
                        arrayOf(normalizedBook, chapter.toString())
                    )
                    if (cursor.moveToFirst()) {
                        val v = cursor.getInt(0)
                        if (v > maxVerse) maxVerse = v
                    }
                    cursor.close()
                    db.close()
                } catch (_: Exception) {}
            }
        }

        return if (maxVerse > 0) maxVerse else 1
    }

    fun getVerses(translationCode: String, book: String, chapter: Int): List<Verse> {
        val dbFileName = getDbFileName(translationCode)
        val normalizedBook = normalizeBookCode(book)
        val fetchedMap = mutableMapOf<Int, Verse>()
        val db = getReadableDb(dbFileName)

        val cursor = db.query(
            "verses",
            arrayOf("id", "book", "chapter", "verse", "text"),
            "book = ? AND chapter = ?",
            arrayOf(normalizedBook, chapter.toString()),
            null, null, "verse ASC"
        )

        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getLong(cursor.getColumnIndexOrThrow("id"))
                val b = cursor.getString(cursor.getColumnIndexOrThrow("book"))
                val c = cursor.getInt(cursor.getColumnIndexOrThrow("chapter"))
                val v = cursor.getInt(cursor.getColumnIndexOrThrow("verse"))
                val text = cursor.getString(cursor.getColumnIndexOrThrow("text"))
                fetchedMap[v] = Verse(id, b, c, v, text)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()

        val maxVerse = getMaxVerseNumberForChapter(normalizedBook, chapter)
        val fullList = mutableListOf<Verse>()

        for (vNum in 1..maxVerse) {
            val vObj = fetchedMap[vNum] ?: Verse(0, normalizedBook, chapter, vNum, "")
            fullList.add(vObj)
        }

        return fullList
    }

    fun getSecondaryVersesForChapter(
        secondaryTranslations: Set<String>,
        book: String,
        chapter: Int,
        primaryVerseNumbers: List<Int> = emptyList()
    ): Map<Int, List<TranslationVerse>> {
        val resultMap = mutableMapOf<Int, MutableList<TranslationVerse>>()
        val normalizedBook = normalizeBookCode(book)

        for (transCode in secondaryTranslations) {
            val dbFileName = getDbFileName(transCode)
            val transName = TRANSLATION_MAP[transCode.uppercase()] ?: transCode
            val fetchedVersesMap = mutableMapOf<Int, String>()

            try {
                val db = getReadableDb(dbFileName)
                val cursor = db.query(
                    "verses",
                    arrayOf("verse", "text"),
                    "book = ? AND chapter = ?",
                    arrayOf(normalizedBook, chapter.toString()),
                    null, null, "verse ASC"
                )

                if (cursor.moveToFirst()) {
                    do {
                        val vNum = cursor.getInt(cursor.getColumnIndexOrThrow("verse"))
                        val text = cursor.getString(cursor.getColumnIndexOrThrow("text"))
                        fetchedVersesMap[vNum] = text
                    } while (cursor.moveToNext())
                }
                cursor.close()
                db.close()
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching secondary verses for $transCode", e)
            }

            // Determine all verse numbers to map
            val targetVerseNumbers = if (primaryVerseNumbers.isNotEmpty()) {
                primaryVerseNumbers
            } else if (fetchedVersesMap.isNotEmpty()) {
                fetchedVersesMap.keys.sorted()
            } else {
                emptyList()
            }

            for (vNum in targetVerseNumbers) {
                val text = fetchedVersesMap[vNum] ?: ""
                val list = resultMap.getOrPut(vNum) { mutableListOf() }
                list.add(TranslationVerse(transCode.uppercase(), transName, text))
            }
        }

        return resultMap
    }

    fun getCrossReferences(book: String, chapter: Int): List<CrossReference> {
        val normalizedBook = normalizeBookCode(book)
        val list = mutableListOf<CrossReference>()
        val db = getReadableDb("crossref.db")

        val cursor = db.query(
            "cross_references",
            arrayOf("id", "book", "chapter", "verse", "anchor", "references_data"),
            "book = ? AND chapter = ?",
            arrayOf(normalizedBook, chapter.toString()),
            null, null, null
        )

        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getLong(cursor.getColumnIndexOrThrow("id"))
                val b = cursor.getString(cursor.getColumnIndexOrThrow("book"))
                val c = cursor.getInt(cursor.getColumnIndexOrThrow("chapter"))
                val v = cursor.getInt(cursor.getColumnIndexOrThrow("verse"))
                val anchor = cursor.getString(cursor.getColumnIndexOrThrow("anchor"))
                val refs = cursor.getString(cursor.getColumnIndexOrThrow("references_data"))
                list.add(CrossReference(id, b, c, v, anchor, refs))
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return list
    }

    fun searchVerses(translationCode: String, query: String, limit: Int = 100): List<SearchResult> {
        val dbFileName = getDbFileName(translationCode)
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return emptyList()

        val list = mutableListOf<SearchResult>()
        val db = getReadableDb(dbFileName)

        val cursor = db.query(
            "verses",
            arrayOf("book", "chapter", "verse", "text"),
            "text LIKE ?",
            arrayOf("%$trimmed%"),
            null, null, "id ASC", limit.toString()
        )

        if (cursor.moveToFirst()) {
            do {
                val bCode = cursor.getString(cursor.getColumnIndexOrThrow("book"))
                val c = cursor.getInt(cursor.getColumnIndexOrThrow("chapter"))
                val v = cursor.getInt(cursor.getColumnIndexOrThrow("verse"))
                val text = cursor.getString(cursor.getColumnIndexOrThrow("text"))
                val bName = BOOK_NAME_MAP[bCode] ?: bCode

                list.add(SearchResult(bCode, bName, c, v, text))
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return list
    }

    fun getVerseTextByRef(translationCode: String, referenceStr: String): String? {
        val dbFileName = getDbFileName(translationCode)
        val trimmed = referenceStr.trim()
        val colonIdx = trimmed.indexOf(':')
        if (colonIdx <= 0) return null

        val beforeColon = trimmed.substring(0, colonIdx).trim()
        val spaceIdx = beforeColon.lastIndexOf(' ')
        if (spaceIdx <= 0) return null

        val bookRaw = beforeColon.substring(0, spaceIdx).trim()
        val chapterStr = beforeColon.substring(spaceIdx + 1).trim()
        val chapter = chapterStr.toIntOrNull() ?: return null

        val normalizedBook = normalizeBookCode(bookRaw)
        val afterColon = trimmed.substring(colonIdx + 1).trim()

        val db = getReadableDb(dbFileName)
        val verseList = mutableListOf<Pair<Int, String>>()

        if (afterColon.contains("-") && !afterColon.contains(",")) {
            // Range format: e.g. "22-24"
            val parts = afterColon.split("-")
            if (parts.size >= 2) {
                val startVerse = parts[0].filter { it.isDigit() }.toIntOrNull() ?: return null
                val endVerse = parts[1].filter { it.isDigit() }.toIntOrNull() ?: return null

                val cursor = db.query(
                    "verses",
                    arrayOf("verse", "text"),
                    "book = ? AND chapter = ? AND verse >= ? AND verse <= ?",
                    arrayOf(normalizedBook, chapter.toString(), startVerse.toString(), endVerse.toString()),
                    null, null, "verse ASC"
                )

                if (cursor.moveToFirst()) {
                    do {
                        val vNum = cursor.getInt(cursor.getColumnIndexOrThrow("verse"))
                        val text = cursor.getString(cursor.getColumnIndexOrThrow("text"))
                        verseList.add(Pair(vNum, text))
                    } while (cursor.moveToNext())
                }
                cursor.close()
            }
        } else if (afterColon.contains(",")) {
            // Comma list (or combined list & range): e.g. "1,3,6"
            val subParts = afterColon.split(",")
            val targetVerses = mutableListOf<Int>()

            for (part in subParts) {
                val pTrim = part.trim()
                if (pTrim.contains("-")) {
                    val rangeParts = pTrim.split("-")
                    val startV = rangeParts.getOrNull(0)?.filter { it.isDigit() }?.toIntOrNull()
                    val endV = rangeParts.getOrNull(1)?.filter { it.isDigit() }?.toIntOrNull()
                    if (startV != null && endV != null) {
                        for (v in startV..endV) {
                            targetVerses.add(v)
                        }
                    }
                } else {
                    val singleV = pTrim.filter { it.isDigit() }.toIntOrNull()
                    if (singleV != null) {
                        targetVerses.add(singleV)
                    }
                }
            }

            if (targetVerses.isNotEmpty()) {
                val placeholders = targetVerses.joinToString(",") { "?" }
                val args = mutableListOf(normalizedBook, chapter.toString())
                args.addAll(targetVerses.map { it.toString() })

                val cursor = db.query(
                    "verses",
                    arrayOf("verse", "text"),
                    "book = ? AND chapter = ? AND verse IN ($placeholders)",
                    args.toTypedArray(),
                    null, null, "verse ASC"
                )

                if (cursor.moveToFirst()) {
                    do {
                        val vNum = cursor.getInt(cursor.getColumnIndexOrThrow("verse"))
                        val text = cursor.getString(cursor.getColumnIndexOrThrow("text"))
                        verseList.add(Pair(vNum, text))
                    } while (cursor.moveToNext())
                }
                cursor.close()
            }
        } else {
            // Single verse
            val verseNum = afterColon.filter { it.isDigit() }.toIntOrNull() ?: return null
            val cursor = db.query(
                "verses",
                arrayOf("verse", "text"),
                "book = ? AND chapter = ? AND verse = ?",
                arrayOf(normalizedBook, chapter.toString(), verseNum.toString()),
                null, null, null
            )

            if (cursor.moveToFirst()) {
                val vNum = cursor.getInt(cursor.getColumnIndexOrThrow("verse"))
                val text = cursor.getString(cursor.getColumnIndexOrThrow("text"))
                verseList.add(Pair(vNum, text))
            }
            cursor.close()
        }

        db.close()
        if (verseList.isEmpty()) return null

        val isSingleVerse = verseList.size == 1
        val sb = StringBuilder()

        for ((idx, item) in verseList.withIndex()) {
            if (idx > 0) sb.append("<br>")
            if (!isSingleVerse) {
                sb.append("<font color='#B71C1C'><sup><small><b>")
                    .append(item.first)
                    .append("</b></small></sup></font> ")
            }
            sb.append(item.second)
        }

        return sb.toString()
    }

    fun getCompareVerses(
        bookCode: String,
        chapter: Int,
        verseNum: Int,
        primaryTranslation: String,
        enabledTranslations: Set<String>
    ): List<TranslationVerse> {
        val result = mutableListOf<TranslationVerse>()
        val normalizedBook = normalizeBookCode(bookCode)

        val secondaryTranslations = enabledTranslations.filter { !it.equals(primaryTranslation, ignoreCase = true) }

        for (transCode in secondaryTranslations) {
            val dbFileName = getDbFileName(transCode)
            val transName = TRANSLATION_MAP[transCode.uppercase()] ?: transCode
            try {
                val db = getReadableDb(dbFileName)
                val cursor = db.query(
                    "verses",
                    arrayOf("text"),
                    "book = ? AND chapter = ? AND verse = ?",
                    arrayOf(normalizedBook, chapter.toString(), verseNum.toString()),
                    null, null, null
                )

                if (cursor.moveToFirst()) {
                    val text = cursor.getString(cursor.getColumnIndexOrThrow("text"))
                    result.add(TranslationVerse(transCode.uppercase(), transName, text))
                } else {
                    result.add(TranslationVerse(transCode.uppercase(), transName, ""))
                }
                cursor.close()
                db.close()
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching compare verse for $transCode", e)
                result.add(TranslationVerse(transCode.uppercase(), transName, ""))
            }
        }

        return result
    }
}
