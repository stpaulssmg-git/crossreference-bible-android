package com.stpauls.apostolicreferencebible.ui

import android.graphics.Color
import android.os.Build
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.stpauls.apostolicreferencebible.MainActivity
import com.stpauls.apostolicreferencebible.R
import com.stpauls.apostolicreferencebible.db.SearchResult
import com.stpauls.apostolicreferencebible.db.getLocalizedBookName

class SearchResultAdapter(
    private val themeColors: MainActivity.ModalThemeColors,
    private val primaryTranslationCode: String = "KJV",
    private val onResultSelected: (result: SearchResult) -> Unit
) : RecyclerView.Adapter<SearchResultAdapter.SearchViewHolder>() {

    private val items = mutableListOf<SearchResult>()
    private var searchQuery: String = ""

    fun updateResults(newResults: List<SearchResult>, query: String) {
        items.clear()
        items.addAll(newResults)
        searchQuery = query.trim()
        notifyDataSetChanged()
    }

    class SearchViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvSearchReference: TextView = itemView.findViewById(R.id.tvSearchReference)
        val tvSearchSnippet: TextView = itemView.findViewById(R.id.tvSearchSnippet)
        val vItemSearchDivider: View = itemView.findViewById(R.id.vItemSearchDivider)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_search_result, parent, false)
        return SearchViewHolder(view)
    }

    override fun onBindViewHolder(holder: SearchViewHolder, position: Int) {
        val item = items[position]

        val localizedBook = getLocalizedBookName(item.bookCode, primaryTranslationCode)
        holder.tvSearchReference.text = "$localizedBook ${item.chapter}:${item.verse}"
        holder.tvSearchReference.setTextColor(Color.parseColor(themeColors.refTitleColor))

        holder.vItemSearchDivider.setBackgroundColor(Color.parseColor(themeColors.dividerColor))

        // Highlight matched query term in verse snippet with theme's accent color
        val snippetHtml = highlightQuery(item.text, searchQuery)
        holder.tvSearchSnippet.setTextColor(Color.parseColor(themeColors.previewTextColor))
        holder.tvSearchSnippet.text = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(snippetHtml, Html.FROM_HTML_MODE_LEGACY)
        } else {
            @Suppress("DEPRECATION")
            Html.fromHtml(snippetHtml)
        }

        holder.itemView.setOnClickListener {
            onResultSelected(item)
        }
    }

    override fun getItemCount(): Int = items.size

    private fun highlightQuery(text: String, query: String): String {
        if (query.isEmpty()) return text
        val regex = Regex("(?i)(" + Regex.escape(query) + ")")
        val hexColor = themeColors.refTitleColor
        return regex.replace(text) { matchResult ->
            "<font color='$hexColor'><b>${matchResult.value}</b></font>"
        }
    }
}
