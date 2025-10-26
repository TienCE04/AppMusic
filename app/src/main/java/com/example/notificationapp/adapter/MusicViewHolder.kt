package com.example.notificationapp.adapter

import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.notificationapp.databinding.ItemSongBinding

class MusicViewHolder(view: ItemSongBinding) : RecyclerView.ViewHolder(view.root) {
    val nameSong: TextView = view.tvNameSong
    val nameArtist: TextView = view.tvDescription
    val isPlay: ImageView = view.iconPlay
}