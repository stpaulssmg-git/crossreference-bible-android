package com.stpauls.apostolicreferencebible.engine

import android.content.Context
import com.stpauls.apostolicreferencebible.db.CrossReference
import com.stpauls.apostolicreferencebible.db.TranslationVerse
import com.stpauls.apostolicreferencebible.db.Verse
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.Locale

class HtmlEngine(private val context: Context) {

    private val templateHtml: String by lazy {
        loadTemplate()
    }

    private fun loadTemplate(): String {
        return context.assets.open("template.html").use { inputStream ->
            BufferedReader(InputStreamReader(inputStream)).use { reader ->
                reader.readText()
            }
        }
    }

    private fun isRtlText(text: String): Boolean {
        for (char in text) {
            val directionality = Character.getDirectionality(char)
            if (directionality == Character.DIRECTIONALITY_RIGHT_TO_LEFT ||
                directionality == Character.DIRECTIONALITY_RIGHT_TO_LEFT_ARABIC) {
                return true
            }
        }
        return false
    }

    fun generateChapterHtml(
        verses: List<Verse>,
        crossRefs: List<CrossReference>,
        secondaryVersesMap: Map<Int, List<TranslationVerse>> = emptyMap(),
        themeName: String = "warm",
        fontSize: Float = 14f,
        primaryTranslationCode: String = "KJV"
    ): String {
        val crossRefsByVerse = crossRefs.groupBy { it.verse }
        val sb = StringBuilder()

        val enableAnchorHighlighting = primaryTranslationCode.equals("KJV", ignoreCase = true)

        for (verse in verses) {
            val verseXrefs = crossRefsByVerse[verse.verse] ?: emptyList()
            val (processedText, _) = if (enableAnchorHighlighting) {
                enrichVerseText(verse, verseXrefs)
            } else {
                Pair(escapeHtml(verse.text), 0)
            }
            val verseTextDisplay = if (verse.text.isBlank()) {
                "<i class=\"missing-text\">Text is not available</i>"
            } else {
                processedText
            }

            val secVerses = secondaryVersesMap[verse.verse] ?: emptyList()

            val verseId = "v_${verse.verse}"
            val pId = "v_p_${verse.verse}"
            val isPrimaryRtl = isRtlText(verse.text)
            val pDir = if (isPrimaryRtl) "dir=\"rtl\" style=\"text-align: right;\"" else "dir=\"ltr\" style=\"text-align: left;\""

            sb.append("<p id=\"$pId\" class=\"verse-p\" $pDir ")
            if (secVerses.isNotEmpty()) {
                sb.append("onclick=\"toggleCompare('$verseId', event)\"")
            }
            sb.append(">")

            sb.append("<span class=\"verse-num\">")
                .append(verse.verse)
                .append("</span>&nbsp;")

            sb.append(verseTextDisplay)

            // Append xref button if cross references exist for this verse
            if (verseXrefs.isNotEmpty()) {
                sb.append("&nbsp;<span class=\"xref-btn\" onclick=\"event.stopPropagation(); Android.onVerseCrossRefClicked('${verse.book}', ${verse.chapter}, ${verse.verse})\">xref</span>")
            }

            // Append inline collapsible secondary verse comparison block if secondary verses exist
            if (secVerses.isNotEmpty()) {
                sb.append("<div id=\"$verseId\" class=\"secondary-verse-block\" style=\"display: none;\">")

                for (sec in secVerses) {
                    val isSecRtl = isRtlText(sec.text)
                    val rowClass = if (isSecRtl) "secondary-row secondary-rtl" else "secondary-row"
                    val dirAttr = if (isSecRtl) "dir=\"rtl\"" else "dir=\"ltr\""

                    val secTextDisplay = if (sec.text.isBlank()) {
                        "<i class=\"missing-text\">Text is not available</i>"
                    } else {
                        escapeHtml(sec.text)
                    }

                    sb.append("<div class=\"$rowClass\" $dirAttr>")
                        .append("<span class=\"trans-tag\" onclick=\"event.stopPropagation(); Android.onSwitchPrimaryTranslation('${sec.translationCode}')\">")
                        .append(sec.translationCode)
                        .append("</span>&nbsp;")
                        .append("<span class=\"secondary-text\">").append(secTextDisplay).append("</span>")
                        .append("</div>")
                }

                sb.append("</div>")
            }

            sb.append("</p>\n")
        }

        val formattedFontSize = String.format(Locale.US, "%.1f", fontSize)

        return templateHtml
            .replace("class=\"theme-warm\"", "class=\"theme-$themeName\" style=\"font-size: ${formattedFontSize}px;\"")
            .replace("<!-- BIBLE_CONTENT_PLACEHOLDER -->", sb.toString())
    }

    private fun enrichVerseText(verse: Verse, crossRefs: List<CrossReference>): Pair<String, Int> {
        if (crossRefs.isEmpty()) {
            return Pair(escapeHtml(verse.text), 0)
        }

        // Sort anchors by length descending so longer phrases match first
        val sortedRefs = crossRefs.sortedByDescending { it.anchor.length }
        var currentText = verse.text
        var matchedCount = 0

        // We use placeholders to prevent replacing inside an already replaced HTML tag
        val placeholders = mutableListOf<String>()

        for (xref in sortedRefs) {
            val anchor = xref.anchor
            if (anchor.isBlank()) continue

            val index = currentText.indexOf(anchor, ignoreCase = true)
            if (index >= 0) {
                val placeholder = "___XREF_PLACEHOLDER_${placeholders.size}___"
                val matchedWord = currentText.substring(index, index + anchor.length)
                val escapedWord = escapeHtml(matchedWord)
                val escapedAnchor = escapeHtml(anchor)
                val escapedRefs = escapeHtml(xref.references)
                val sourceRef = "${verse.book} ${verse.chapter}:${verse.verse}"

                val htmlSpan = "<span class=\"cross-ref\" " +
                        "data-anchor=\"$escapedAnchor\" " +
                        "data-source=\"$sourceRef\" " +
                        "data-refs=\"$escapedRefs\" " +
                        "onclick=\"Android.onCrossReferenceClicked(this.getAttribute('data-anchor'), this.getAttribute('data-source'), this.getAttribute('data-refs'))\">" +
                        "$escapedWord</span>"

                placeholders.add(htmlSpan)
                // Replace only the first match occurrence
                currentText = currentText.substring(0, index) + placeholder + currentText.substring(index + anchor.length)
                matchedCount++
            }
        }

        // Escape remaining text that is not inside placeholders
        var escapedResult = escapeHtml(currentText)

        // Restore placeholders
        placeholders.forEachIndexed { idx, span ->
            val placeholder = "___XREF_PLACEHOLDER_${idx}___"
            escapedResult = escapedResult.replace(placeholder, span)
        }

        return Pair(escapedResult, matchedCount)
    }

    private fun escapeHtml(text: String): String {
        return text.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#39;")
    }
}
