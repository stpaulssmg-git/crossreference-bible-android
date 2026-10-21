package com.stpauls.apostolicreferencebible.ui

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.stpauls.apostolicreferencebible.MainActivity
import com.stpauls.apostolicreferencebible.R

class WordSuggestionAdapter(
    private val themeColors: MainActivity.ModalThemeColors,
    private val onSuggestionClicked: (word: String) -> Unit
) : RecyclerView.Adapter<WordSuggestionAdapter.ViewHolder>() {

    private val suggestions = mutableListOf<String>()

    fun updateSuggestions(newSuggestions: List<String>) {
        suggestions.clear()
        suggestions.addAll(newSuggestions)
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivIcon: ImageView = itemView.findViewById(R.id.ivSuggestionIcon)
        val tvWord: TextView = itemView.findViewById(R.id.tvSuggestionWord)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_word_suggestion, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val word = suggestions[position]
        holder.tvWord.text = word
        holder.tvWord.setTextColor(Color.parseColor(themeColors.titleColor))
        holder.ivIcon.setColorFilter(Color.parseColor(themeColors.subtitleColor))

        holder.itemView.setOnClickListener {
            onSuggestionClicked(word)
        }
    }

    override fun getItemCount(): Int = suggestions.size
}
