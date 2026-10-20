package com.stpauls.apostolicreferencebible.ui

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
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
        val btnDownload: Button = itemView.findViewById(R.id.btnDownloadEbible)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CatalogViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_ebible_download, parent, false)
        return CatalogViewHolder(view)
    }

    override fun onBindViewHolder(holder: CatalogViewHolder, position: Int) {
        val item = filteredItems[position]

        holder.cardView.setCardBackgroundColor(Color.parseColor(themeColors.expandedGridBg))

        holder.tvTitle.text = item.title
        holder.tvTitle.setTextColor(Color.parseColor(themeColors.titleColor))

        holder.tvDetails.text = "${item.language} • ${item.country}"
        holder.tvDetails.setTextColor(Color.parseColor(themeColors.subtitleColor))

        holder.btnDownload.backgroundTintList = ColorStateList.valueOf(Color.parseColor(themeColors.refTitleColor))

        val isDownloaded = downloadedIds.contains(item.id.lowercase())
        if (isDownloaded) {
            holder.btnDownload.text = "Installed ✓"
            holder.btnDownload.isEnabled = false
            holder.btnDownload.alpha = 0.6f
        } else {
            holder.btnDownload.text = "⬇ Download"
            holder.btnDownload.isEnabled = true
            holder.btnDownload.alpha = 1.0f
            holder.btnDownload.setOnClickListener {
                onDownloadClick(item)
            }
        }
    }

    override fun getItemCount(): Int = filteredItems.size
}
