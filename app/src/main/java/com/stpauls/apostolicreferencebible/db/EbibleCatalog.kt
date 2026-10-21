package com.stpauls.apostolicreferencebible.db

import android.content.Context
import android.util.Log
import java.io.File

data class EbibleCatalogItem(
    val id: String,
    val code: String,
    val language: String,
    val country: String,
    val title: String,
    val vplUrl: String
)

private var cachedCatalogList: List<EbibleCatalogItem>? = null

fun loadOfflineCatalog(context: Context): List<EbibleCatalogItem> {
    cachedCatalogList?.let { return it }

    try {
        val cachedFile = File(context.filesDir, "translations_cache.csv")
        if (cachedFile.exists() && cachedFile.length() > 100) {
            val csvText = cachedFile.readText()
            val items = parseTranslationsCsv(csvText)
            if (items.isNotEmpty()) {
                cachedCatalogList = items
                return items
            }
        }
    } catch (e: Exception) {
        Log.e("EbibleCatalog", "Error reading cached translations.csv", e)
    }

    try {
        context.assets.open("translations.csv").use { inputStream ->
            val csvText = inputStream.bufferedReader().use { it.readText() }
            val items = parseTranslationsCsv(csvText)
            if (items.isNotEmpty()) {
                cachedCatalogList = items
                return items
            }
        }
    } catch (e: Exception) {
        Log.e("EbibleCatalog", "Error reading asset translations.csv", e)
    }

    return emptyList()
}

fun updateCachedCatalog(context: Context, newCsvText: String): List<EbibleCatalogItem> {
    val items = parseTranslationsCsv(newCsvText)
    if (items.isNotEmpty()) {
        cachedCatalogList = items
        try {
            val cachedFile = File(context.filesDir, "translations_cache.csv")
            cachedFile.writeText(newCsvText)
        } catch (e: Exception) {
            Log.e("EbibleCatalog", "Error saving cached translations.csv", e)
        }
    }
    return items
}

fun searchEbibleCatalog(query: String, catalog: List<EbibleCatalogItem>): List<EbibleCatalogItem> {
    val q = query.trim().lowercase()
    if (q.isEmpty()) return catalog

    return catalog.filter { item ->
        item.language.lowercase().contains(q) ||
        item.title.lowercase().contains(q) ||
        item.country.lowercase().contains(q) ||
        item.code.lowercase().contains(q) ||
        item.id.lowercase().contains(q)
    }
}

fun parseTranslationsCsv(csvText: String): List<EbibleCatalogItem> {
    val items = mutableListOf<EbibleCatalogItem>()
    val rows = parseFullCsv(csvText)
    if (rows.size <= 1) return items

    val headers = rows[0]
    val langCodeIdx = headers.indexOf("languageCode")
    val transIdIdx = headers.indexOf("translationId")
    val langNameIdx = headers.indexOf("languageNameInEnglish")
    val localLangNameIdx = headers.indexOf("languageName")
    val dialectIdx = headers.indexOf("dialect")
    val titleIdx = headers.indexOf("title")
    val downloadableIdx = headers.indexOf("downloadable")

    if (transIdIdx == -1) return items

    for (i in 1 until rows.size) {
        val cols = rows[i]
        if (cols.size <= transIdIdx) continue

        val transId = cols[transIdIdx].trim()
        if (transId.isEmpty()) continue

        val isDownloadable = if (downloadableIdx != -1 && cols.size > downloadableIdx) {
            val d = cols[downloadableIdx].trim().lowercase()
            d != "false" && d != "no" && d != "0"
        } else true

        if (!isDownloadable) continue

        val langCode = if (langCodeIdx != -1 && cols.size > langCodeIdx && cols[langCodeIdx].trim().isNotEmpty()) {
            cols[langCodeIdx].trim().uppercase()
        } else transId.take(3).uppercase()

        var langName = if (langNameIdx != -1 && cols.size > langNameIdx) cols[langNameIdx].trim() else ""
        if (langName.isEmpty() && localLangNameIdx != -1 && cols.size > localLangNameIdx) {
            langName = cols[localLangNameIdx].trim()
        }
        if (langName.isEmpty()) langName = "Unknown"

        val dialect = if (dialectIdx != -1 && cols.size > dialectIdx) cols[dialectIdx].trim() else ""

        val title = if (titleIdx != -1 && cols.size > titleIdx && cols[titleIdx].trim().isNotEmpty()) {
            cols[titleIdx].trim()
        } else langName

        val countryDesc = if (dialect.isNotEmpty()) "Dialect: $dialect • Variant ID: $transId" else "Variant ID: $transId"

        items.add(
            EbibleCatalogItem(
                id = transId,
                code = langCode,
                language = langName,
                country = countryDesc,
                title = title,
                vplUrl = "https://ebible.org/Scriptures/${transId}_vpl.zip"
            )
        )
    }

    val parsedList = items.distinctBy { it.id.lowercase() }
    return parsedList.sortedWith(compareBy({ it.language.lowercase() }, { it.title.lowercase() }))
}

private fun parseFullCsv(text: String): List<List<String>> {
    val rows = mutableListOf<List<String>>()
    val curRow = mutableListOf<String>()
    val curCell = StringBuilder()
    var inQuotes = false
    var i = 0
    val len = text.length

    while (i < len) {
        val ch = text[i]
        if (ch == '"') {
            if (inQuotes && i + 1 < len && text[i + 1] == '"') {
                curCell.append('"')
                i++
            } else {
                inQuotes = !inQuotes
            }
        } else if (ch == ',' && !inQuotes) {
            curRow.add(curCell.toString())
            curCell.clear()
        } else if ((ch == '\r' || ch == '\n') && !inQuotes) {
            if (ch == '\r' && i + 1 < len && text[i + 1] == '\n') {
                i++
            }
            curRow.add(curCell.toString())
            curCell.clear()
            if (curRow.any { it.isNotBlank() }) {
                rows.add(ArrayList(curRow))
            }
            curRow.clear()
        } else {
            curCell.append(ch)
        }
        i++
    }
    if (curCell.isNotEmpty() || curRow.isNotEmpty()) {
        curRow.add(curCell.toString())
        if (curRow.any { it.isNotBlank() }) {
            rows.add(curRow)
        }
    }
    return rows
}
