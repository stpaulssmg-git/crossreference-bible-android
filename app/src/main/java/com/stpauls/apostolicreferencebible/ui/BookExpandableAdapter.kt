package com.stpauls.apostolicreferencebible.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.LinearLayout
import android.widget.TextView
import com.stpauls.apostolicreferencebible.R
import com.stpauls.apostolicreferencebible.db.BIBLE_BOOKS
import com.stpauls.apostolicreferencebible.db.BookInfo

class BookExpandableAdapter(
    private val context: Context,
    private val onChapterSelected: (bookIndex: Int, chapterNum: Int) -> Unit
) : BaseExpandableListAdapter() {

    private val inflater: LayoutInflater = LayoutInflater.from(context)

    override fun getGroupCount(): Int = BIBLE_BOOKS.size

    override fun getChildrenCount(groupPosition: Int): Int = 1 // One child layout containing chapter grid

    override fun getGroup(groupPosition: Int): BookInfo = BIBLE_BOOKS[groupPosition]

    override fun getChild(groupPosition: Int, childPosition: Int): BookInfo = BIBLE_BOOKS[groupPosition]

    override fun getGroupId(groupPosition: Int): Long = groupPosition.toLong()

    override fun getChildId(groupPosition: Int, childPosition: Int): Long = childPosition.toLong()

    override fun hasStableIds(): Boolean = true

    override fun getGroupView(
        groupPosition: Int,
        isExpanded: Boolean,
        convertView: View?,
        parent: ViewGroup?
    ): View {
        val view = convertView ?: inflater.inflate(R.layout.item_book_group, parent, false)
        val tvBookName = view.findViewById<TextView>(R.id.tvBookName)
        val tvChapterCount = view.findViewById<TextView>(R.id.tvChapterCount)

        val book = getGroup(groupPosition)
        tvBookName.text = book.name
        tvChapterCount.text = "${book.totalChapters} Chapters"

        return view
    }

    override fun getChildView(
        groupPosition: Int,
        childPosition: Int,
        isLastChild: Boolean,
        convertView: View?,
        parent: ViewGroup?
    ): View {
        val view = convertView ?: inflater.inflate(R.layout.item_chapter_grid, parent, false)
        val chapterContainer = view.findViewById<LinearLayout>(R.id.chapterContainer)
        chapterContainer.removeAllViews()

        val book = getGroup(groupPosition)
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
                chapterContainer.addView(currentRow)
            }

            val chipView = inflater.inflate(R.layout.item_chapter_chip, currentRow, false) as TextView
            chipView.text = ch.toString()
            chipView.setOnClickListener {
                onChapterSelected(groupPosition, ch)
            }

            currentRow?.addView(chipView)
        }

        return view
    }

    override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean = true
}
