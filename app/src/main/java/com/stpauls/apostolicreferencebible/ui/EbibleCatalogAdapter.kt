package com.stpauls.apostolicreferencebible.ui

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.stpauls.apostolicreferencebible.MainActivity
import com.stpauls.apostolicreferencebible.R
import com.stpauls.apostolicreferencebible.db.EbibleCatalogItem

class EbibleCatalogAdapter(
    private val themeColors: MainActivity.ModalThemeColors,
    private val downloadedIds: Set<String>,
    private val onDownloadClick: (item: EbibleCatalogItem) -> Unit,
    private val onDeleteClick: ((item: EbibleCatalogItem) -> Unit)? = null
) : RecyclerView.Adapter<EbibleCatalogAdapter.CatalogViewHolder>() {

    private val allItems = mutableListOf<EbibleCatalogItem>()
    private val filteredItems = mutableListOf<EbibleCatalogItem>()
    private val downloadingIds = mutableSetOf<String>()
    private val expandedItemIds = mutableSetOf<String>()

    fun setCatalog(items: List<EbibleCatalogItem>) {
        allItems.clear()
        allItems.addAll(items)
        filteredItems.clear()
        filteredItems.addAll(items)
        notifyDataSetChanged()
    }

    fun markDownloading(itemId: String) {
        downloadingIds.add(itemId.lowercase())
        notifyDataSetChanged()
    }

    fun markDownloadFailed(itemId: String) {
        downloadingIds.remove(itemId.lowercase())
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
        val layoutHeader: View = itemView.findViewById(R.id.layoutEbibleHeader)
        val tvTitle: TextView = itemView.findViewById(R.id.tvEbibleTitle)
        val tvDetails: TextView = itemView.findViewById(R.id.tvEbibleDetails)
        val pbProgress: ProgressBar = itemView.findViewById(R.id.pbDownloadProgress)
        val btnDownloadIcon: ImageButton = itemView.findViewById(R.id.btnDownloadIcon)
        val btnDeleteEbible: ImageButton = itemView.findViewById(R.id.btnDeleteEbible)

        // Expandable details container and metadata views
        val layoutDetails: View = itemView.findViewById(R.id.layoutEbibleDetails)
        val vDivider: View = itemView.findViewById(R.id.vEbibleDivider)
        val tvDescription: TextView = itemView.findViewById(R.id.tvEbibleDescription)
        val tvBooksScope: TextView = itemView.findViewById(R.id.tvEbibleBooksScope)
        val tvUpdateDate: TextView = itemView.findViewById(R.id.tvEbibleUpdateDate)
        val tvCopyright: TextView = itemView.findViewById(R.id.tvEbibleCopyright)
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

        // Expand/Collapse Details
        val isExpanded = expandedItemIds.contains(item.id.lowercase())
        holder.layoutDetails.visibility = if (isExpanded) View.VISIBLE else View.GONE

        if (isExpanded) {
            holder.vDivider.setBackgroundColor(Color.parseColor(themeColors.dividerColor))

            if (item.description.isNotBlank()) {
                holder.tvDescription.text = "📝 Description: ${item.description}"
                holder.tvDescription.setTextColor(Color.parseColor(themeColors.titleColor))
                holder.tvDescription.visibility = View.VISIBLE
            } else {
                holder.tvDescription.visibility = View.GONE
            }

            val otCnt = item.otBooks.toIntOrNull() ?: 0
            val ntCnt = item.ntBooks.toIntOrNull() ?: 0
            val scopeText = when {
                otCnt > 0 && ntCnt > 0 -> "📚 Available Books: OT ($otCnt Books) • NT ($ntCnt Books)"
                otCnt > 0 -> "📚 Available Books: OT ($otCnt Books)"
                ntCnt > 0 -> "📚 Available Books: NT ($ntCnt Books)"
                else -> ""
            }
            if (scopeText.isNotEmpty()) {
                holder.tvBooksScope.text = scopeText
                holder.tvBooksScope.setTextColor(Color.parseColor(themeColors.refTitleColor))
                holder.tvBooksScope.visibility = View.VISIBLE
            } else {
                holder.tvBooksScope.visibility = View.GONE
            }

            if (item.updateDate.isNotBlank()) {
                holder.tvUpdateDate.text = "📅 Last Updated: ${item.updateDate}"
                holder.tvUpdateDate.setTextColor(Color.parseColor(themeColors.subtitleColor))
                holder.tvUpdateDate.visibility = View.VISIBLE
            } else {
                holder.tvUpdateDate.visibility = View.GONE
            }

            if (item.copyright.isNotBlank()) {
                holder.tvCopyright.text = "⚖️ ${item.copyright}"
                holder.tvCopyright.setTextColor(Color.parseColor(themeColors.previewTextColor))
                holder.tvCopyright.visibility = View.VISIBLE
            } else {
                holder.tvCopyright.visibility = View.GONE
            }
        }

        holder.layoutHeader.setOnClickListener {
            val key = item.id.lowercase()
            if (expandedItemIds.contains(key)) {
                expandedItemIds.remove(key)
            } else {
                expandedItemIds.add(key)
            }
            notifyItemChanged(position)
        }

        val isDownloaded = downloadedIds.contains(item.id.lowercase())
        val isDownloading = downloadingIds.contains(item.id.lowercase())

        if (isDownloading) {
            holder.pbProgress.visibility = View.VISIBLE
            holder.btnDownloadIcon.visibility = View.GONE
            holder.btnDeleteEbible.visibility = View.GONE
        } else if (isDownloaded) {
            holder.pbProgress.visibility = View.GONE
            holder.btnDownloadIcon.visibility = View.GONE

            holder.btnDeleteEbible.visibility = View.VISIBLE
            holder.btnDeleteEbible.setOnClickListener {
                onDeleteClick?.invoke(item)
            }
        } else {
            holder.pbProgress.visibility = View.GONE
            holder.btnDownloadIcon.visibility = View.VISIBLE
            holder.btnDownloadIcon.setImageResource(android.R.drawable.stat_sys_download)
            holder.btnDownloadIcon.isEnabled = true
            holder.btnDownloadIcon.alpha = 1.0f
            holder.btnDownloadIcon.setColorFilter(Color.parseColor(themeColors.refTitleColor))
            holder.btnDownloadIcon.setOnClickListener {
                markDownloading(item.id)
                onDownloadClick(item)
            }

            holder.btnDeleteEbible.visibility = View.GONE
            holder.btnDeleteEbible.setOnClickListener(null)
        }
    }

    override fun getItemCount(): Int = filteredItems.size
}
