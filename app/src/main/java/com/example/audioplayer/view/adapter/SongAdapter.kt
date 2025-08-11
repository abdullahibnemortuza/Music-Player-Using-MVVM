package com.example.audioplayer.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.audioplayer.databinding.ItemSongBinding
import com.example.audioplayer.model.Song
import kotlin.math.roundToInt

class SongAdapter(
    private var items: List<Song>,
    private val onClick: (position: Int) -> Unit
) : RecyclerView.Adapter<SongAdapter.VH>() {

    inner class VH(val binding: ItemSongBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                onClick(adapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemSongBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val s = items[position]
        holder.binding.tvSongTitle.text = s.title
        holder.binding.tvArtist.text = s.artist ?: "Unknown"
        holder.binding.tvDuration.text = msToTime(s.duration)
    }

    override fun getItemCount(): Int = items.size

    fun submitList(list: List<Song>) {
        items = list
        notifyDataSetChanged()
    }

    private fun msToTime(ms: Long): String {
        val totalSeconds = (ms / 1000.0).roundToInt()
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format("%d:%02d", minutes, seconds)
    }
}