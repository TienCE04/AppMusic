package com.example.notificationapp

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.notificationapp.data.model.Song
import com.example.notificationapp.data.repository.MusicRepository

class MusicViewModel : ViewModel() {
    private val repository = MusicRepository()
    val songs = MutableLiveData<List<Song>>()

    var isPlaying = false
    var isCurrentSong = ""
    var isPosition = -1

    fun loadSongs(context: Context) {
        songs.value = repository.getSongs(context)
    }

}