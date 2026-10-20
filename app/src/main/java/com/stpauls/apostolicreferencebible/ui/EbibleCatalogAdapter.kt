package com.stpauls.apostolicreferencebible.ui

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.stpauls.apostolicreferencebible.MainActivity
import com.stpauls.apostolicreferencebible.R
import com.stpauls.apostolicreferencebible.db.EbibleCatalogItem

class EbibleCatalogAdapter(
    private val themeColors: MainActivity.ModalThemeColors,
    private val downloadedIds: Set<String>,
    private val onDownloadClick: (item: EbibleCatalogItem) -> Unit
) : RecyclerView.Adapter<EbibleCatalogAdapter.CatalogViewHolder>() {

    private val allItems = mutableListOf<EbibleCatalogItem>()
    private val filteredItems = mutableListOf<EbibleCatalogItem>()

    fun setCatalog(items: List<EbibleCatalogItem>) {
        allItems.clear()
        allItems.addAll(items)
        filteredItems.clear()
        filteredItems.addAll(items)
        notifyDataSetChanged()
    }

    fun filter(query: String) {
        val trimmed = query.trim().lowercase()
        filteredItems.clear()
        if (trimmed.isEmpty()) {
            filteredItems.addAll(allItems)
        } else {
            filteredItems.addAll(allItems.filter {
                it.language.lowercase().contains(trimmed) ||
                it.country.lowercase().contains(trimmed) ||
                it.title.lowercase().contains(trimmed) ||
                it.code.lowercase().contains(trimmed)
            })
        }
        notifyDataSetChanged()
    }

    class CatalogViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardView: CardView = itemView.findViewById(R.id.cardEbibleItem)
        val tvTitle: TextView = itemView.findViewById(R.id.tvEbibleTitle)
        val tvDetails: TextView = itemView.findViewById(R.id.tvEbibleDetails)
        val btnDownloadIcon: ImageButton = itemView.findViewById(R.id.btnDownloadIcon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CatalogViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_ebible_download, parent, false)
        return CatalogViewHolder(view)
    }

    override fun onBindViewHolder(holder: CatalogViewHolder, position: Int) {
        val item = filteredItems[position]

        holder.cardView.setCardBackgroundColor(Color.parseColor(themeColors.expandedGridBg))

        holder.tvTitle.text = "${item.language} - ${item.title}"
        holder.tvTitle.setTextColor(Color.parseColor(themeColors.titleColor))

        holder.tvDetails.text = item.country
        holder.tvDetails.setTextColor(Color.parseColor(themeColors.subtitleColor))

        val isDownloaded = downloadedIds.contains(item.id.lowercase()) || downloadedIds.contains(item.code.lowercase())
        if (isDownloaded) {
            holder.btnDownloadIcon.setImageResource(android.R.drawable.checkbox_on_background)
            holder.btnDownloadIcon.isEnabled = false
            holder.btnDownloadIcon.alpha = 0.4f
            holder.btnDownloadIcon.clearColorFilter()
        } else {
            holder.btnDownloadIcon.setImageResource(android.R.drawable.stat_sys_download)
            holder.btnDownloadIcon.isEnabled = true
            holder.btnDownloadIcon.alpha = 1.0f
            holder.btnDownloadIcon.setColorFilter(Color.parseColor(themeColors.refTitleColor))
            holder.btnDownloadIcon.setOnClickListener {
                onDownloadClick(item)
            }
        }
    }

    override fun getItemCount(): Int = filteredItems.size
}
