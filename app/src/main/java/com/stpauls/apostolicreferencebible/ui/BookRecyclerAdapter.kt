package com.stpauls.apostolicreferencebible.ui

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.stpauls.apostolicreferencebible.MainActivity
import com.stpauls.apostolicreferencebible.R
import com.stpauls.apostolicreferencebible.db.BIBLE_BOOKS
import com.stpauls.apostolicreferencebible.db.getLocalizedBookName

class BookRecyclerAdapter(
    private val context: Context,
    initialExpandedPosition: Int = 0,
    private val themeColors: MainActivity.ModalThemeColors,
    private val primaryTranslationCode: String = "KJV",
    private val onChapterSelected: (bookIndex: Int, chapterNum: Int) -> Unit
) : RecyclerView.Adapter<BookRecyclerAdapter.BookViewHolder>() {

    private val inflater: LayoutInflater = LayoutInflater.from(context)
    var expandedPosition: Int = initialExpandedPosition
        private set

    class BookViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvBookName: TextView = itemView.findViewById(R.id.tvBookName)
        val tvChapterCount: TextView = itemView.findViewById(R.id.tvChapterCount)
        val vBookDivider: View = itemView.findViewById(R.id.vBookDivider)
        val chapterContainerParent: View = itemView.findViewById(R.id.chapterContainerParent)
        val chapterContainer: LinearLayout = itemView.findViewById(R.id.chapterContainer)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val view = inflater.inflate(R.layout.item_book_recycler, parent, false)
        return BookViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        val book = BIBLE_BOOKS[position]
        holder.tvBookName.text = getLocalizedBookName(book.code, primaryTranslationCode)
        holder.tvBookName.setTextColor(Color.parseColor(themeColors.titleColor))

        holder.tvChapterCount.text = "${book.totalChapters} Chapters"
        holder.tvChapterCount.setTextColor(Color.parseColor(themeColors.subtitleColor))

        holder.vBookDivider.setBackgroundColor(Color.parseColor(themeColors.dividerColor))
        holder.chapterContainerParent.setBackgroundColor(Color.parseColor(themeColors.expandedGridBg))

        val isExpanded = (position == expandedPosition)
        holder.chapterContainerParent.visibility = if (isExpanded) View.VISIBLE else View.GONE

        // Header click toggles expansion
        holder.itemView.setOnClickListener {
            val currentPos = holder.adapterPosition
            if (currentPos == RecyclerView.NO_POSITION) return@setOnClickListener

            val prevExpanded = expandedPosition
            if (isExpanded) {
                expandedPosition = -1
                notifyItemChanged(currentPos)
            } else {
                expandedPosition = currentPos
                if (prevExpanded != -1) {
                    notifyItemChanged(prevExpanded)
                }
                notifyItemChanged(expandedPosition)
            }
        }

        // Bind chapter grid if expanded
        if (isExpanded) {
            holder.chapterContainer.removeAllViews()
            val totalChapters = book.totalChapters
            val columns = 6 // 6 chips per row
            var currentRow: LinearLayout? = null

            for (ch in 1..totalChapters) {
                if ((ch - 1) % columns == 0) {
                    currentRow = LinearLayout(context).apply {
                        orientation = LinearLayout.HORIZONTAL
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        )
                    }
                    holder.chapterContainer.addView(currentRow)
                }

                val chipView = inflater.inflate(R.layout.item_chapter_chip, currentRow, false) as TextView
                chipView.text = ch.toString()
                chipView.setTextColor(Color.parseColor(themeColors.chipTextColor))

                // Dynamic themed background chip
                val chipDrawable = GradientDrawable().apply {
                    shape = GradientDrawable.RECTANGLE
                    cornerRadius = 16f
                    setColor(Color.parseColor(themeColors.chipBg))
                    setStroke(2, Color.parseColor(themeColors.chipBorderColor))
                }
                chipView.background = chipDrawable

                chipView.setOnClickListener {
                    val pos = holder.adapterPosition
                    if (pos != RecyclerView.NO_POSITION) {
                        onChapterSelected(pos, ch)
                    }
                }

                currentRow?.addView(chipView)
            }
        }
    }

    override fun getItemCount(): Int = BIBLE_BOOKS.size
}
