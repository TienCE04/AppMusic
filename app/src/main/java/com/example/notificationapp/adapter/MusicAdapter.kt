package com.example.notificationapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.notificationapp.MusicViewModel
import com.example.notificationapp.R
import com.example.notificationapp.data.model.Song
import com.example.notificationapp.databinding.ItemSongBinding

class MusicAdapter(
    val list: List<Song>,
    val viewModel: MusicViewModel,
    val onClick: (Int) -> Unit
) :
    RecyclerView.Adapter<MusicViewHolder>() {


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MusicViewHolder {
        val itemView = ItemSongBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MusicViewHolder(itemView)
    }

    override fun onBindViewHolder(
        holder: MusicViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        if (payloads.isNotEmpty()) {
            val item = list[position]
            when (payloads[0]) {
                "iconChange" -> {
                    if (viewModel.isCurrentSong == item.url && viewModel.isPlaying) {
                        holder.isPlay.setImageResource(R.drawable.icon_pause)
                    } else {
                        holder.isPlay.setImageResource(R.drawable.icon_play)
                    }
                }
            }
            return
        }

        super.onBindViewHolder(holder, position, payloads)
    }

    override fun onBindViewHolder(holder: MusicViewHolder, position: Int) {
        val item = list[position]
        holder.nameSong.text = item.title
        holder.nameArtist.text = item.artist

        holder.isPlay.setOnClickListener {
            val previousPosition = viewModel.isPosition
            val previousSong = viewModel.isCurrentSong

            if (previousSong == item.url) {
                viewModel.isPlaying = !viewModel.isPlaying
            } else {
                viewModel.isCurrentSong = item.url
                viewModel.isPlaying = true
                viewModel.isPosition = position
            }

            if (previousPosition != -1) notifyItemChanged(previousPosition, "iconChange")
            notifyItemChanged(position, "iconChange")

            onClick(position)
        }

        if (viewModel.isCurrentSong == item.url && viewModel.isPlaying) {
            holder.isPlay.setImageResource(R.drawable.icon_pause)
        } else {
            holder.isPlay.setImageResource(R.drawable.icon_play)
        }
    }


    override fun getItemCount(): Int {
        return list.size
    }
}