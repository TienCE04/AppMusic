package com.example.notificationapp

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.notificationapp.adapter.MusicAdapter
import com.example.notificationapp.data.model.Song
import com.example.notificationapp.databinding.ActivityMainBinding
import com.example.notificationapp.service.MusicService
import java.util.ArrayList

class MainActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var binding: ActivityMainBinding
    private var listSongs: MutableList<Song> = mutableListOf()
    private lateinit var adapterMusic: MusicAdapter
    private val viewModel: MusicViewModel by viewModels()


    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initAdapter()

        viewModel.loadSongs(this)

        viewModel.songs.observe(this) { list ->
            listSongs.clear()
            listSongs.addAll(list)
            adapterMusic.notifyDataSetChanged()
        }
    }

    private fun initAdapter() {
        adapterMusic = MusicAdapter(listSongs, viewModel) { position ->
            startPlaying(this, position)
        }
        val recycleView = binding.rcvListSong
        recycleView.layoutManager = LinearLayoutManager(this)
        recycleView.adapter = adapterMusic
    }

    override fun onClick(v: View?) {
        when (v?.id) {

        }
    }

    fun startPlaying(context: Context, position: Int) {

        val intent = Intent(this, MusicService::class.java)
        intent.action = MusicService.ACTION_START_SERVICE
        intent.putExtra("song_index", position)
        intent.putParcelableArrayListExtra("songs", listSongs as ArrayList<out Parcelable?>?)
        ContextCompat.startForegroundService(this, intent)
    }
}