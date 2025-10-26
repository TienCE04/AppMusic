package com.example.notificationapp.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.example.notificationapp.R
import com.example.notificationapp.data.model.Song
import com.example.notificationapp.MainActivity

class MusicService : Service() {

    companion object {
        const val CHANNEL_ID = "music_channel"
        const val ACTION_START_SERVICE = "ACTION_START_SERVICE"
        const val ACTION_PLAY_OR_PAUSE = "ACTION_PLAY_OR_PAUSE"
        const val ACTION_NEXT = "NEXT"
        const val ACTION_PREVIOUS = "PREVIOUS"
        const val NOTIFICATION_ID = 1
    }

    private var isPlaying = false
    private var remoteViews: RemoteViews? = null
    private var songList: List<Song> = listOf()
    private var currentIndex = 0
    private var mediaPlayer: MediaPlayer? = null
    private var currentUrl: String? = null
    private var isForegroundStarted = false

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        // Start foreground ngay lập tức với notification tạm thời nếu chưa start
        if (!isForegroundStarted) {
            val tempSong = Song(title = "Loading...", artist = "", url = "")
            showCustomNotification(tempSong)
            isForegroundStarted = true
        }

        when (intent?.action) {
            ACTION_START_SERVICE -> {
                val index = intent.getIntExtra("song_index", 0)
                val songs: ArrayList<Song>? =
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        intent.getParcelableArrayListExtra("songs", Song::class.java)
                    } else {
                        @Suppress("DEPRECATION")
                        intent.getParcelableArrayListExtra<Song>("songs")
                    }

                if (!songs.isNullOrEmpty()) {
                    songList = songs
                    currentIndex = index
                    playSong(songList[currentIndex])
                }
            }

            ACTION_PLAY_OR_PAUSE -> togglePlayPause()
            ACTION_NEXT -> playNext()
            ACTION_PREVIOUS -> playPrevious()
        }

        return START_STICKY
    }

    private fun playSong(song: Song) {
        if (song.url == currentUrl) {
            togglePlayPause()
            return
        }

        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer()

        try {
            if (song.url.startsWith("http://") || song.url.startsWith("https://")) {
                mediaPlayer?.setDataSource(song.url)
            } else {
                val assetManager = applicationContext.assets
                val fileName = song.url.removePrefix("file:///android_asset").removePrefix("/")
                val afd = assetManager.openFd(fileName)
                mediaPlayer?.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                afd.close()
            }

            mediaPlayer?.apply {
                setOnPreparedListener {
                    start()
                    this@MusicService.isPlaying = true
                    updateNotification()
                }
                setOnCompletionListener { playNext() }
                setOnErrorListener { _, what, extra ->
                    Log.e("MusicService", "Playback error: what=$what extra=$extra")
                    stopSelf()
                    true
                }
                prepareAsync()
            }

            currentUrl = song.url
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("MusicService", "Error playing song: ${e.message}")
        }
    }

    private fun togglePlayPause() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.pause()
                isPlaying = false
            } else {
                it.start()
                isPlaying = true
            }
            updateNotification()
        }
    }

    @SuppressLint("RemoteViewLayout")
    private fun showCustomNotification(song: Song) {
        if (song == null) return

        remoteViews = RemoteViews(packageName, R.layout.item_notification)
        remoteViews?.setTextViewText(R.id.tv_name_song, song.title)
        remoteViews?.setTextViewText(R.id.tv_description, song.artist)
        remoteViews?.setImageViewResource(
            R.id.img_pause,
            if (isPlaying) R.drawable.icon_pause else R.drawable.icon_play
        )

        setClickActions(remoteViews!!)

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.icon_music)
            .setCustomContentView(remoteViews)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .build()

        // startForeground chỉ gọi 1 lần khi service bắt đầu
        if (!isForegroundStarted) {
            startForeground(NOTIFICATION_ID, notification)
            isForegroundStarted = true
        } else {
            // Cập nhật notification
            val manager = getSystemService(NOTIFICATION_SERVICE) as android.app.NotificationManager
            manager.notify(NOTIFICATION_ID, notification)
        }
    }

    private fun updateNotification() {
        songList.getOrNull(currentIndex)?.let { song ->
            remoteViews?.setTextViewText(R.id.tv_name_song, song.title)
            remoteViews?.setTextViewText(R.id.tv_description, song.artist)
            remoteViews?.setImageViewResource(
                R.id.img_pause,
                if (isPlaying) R.drawable.icon_pause else R.drawable.icon_play
            )

            val notification = NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.icon_music)
                .setCustomContentView(remoteViews)
                .setOngoing(true)
                .setColor(getColor(R.color.black))
                .setOnlyAlertOnce(true)
                .build()

            val manager = getSystemService(NOTIFICATION_SERVICE) as android.app.NotificationManager
            manager.notify(NOTIFICATION_ID, notification)
        }
    }

    private fun setClickActions(rv: RemoteViews) {
        // Mỗi nút dùng requestCode khác nhau để tránh PendingIntent bị trùng
        val playIntent = PendingIntent.getService(
            this, 0, Intent(this, MusicService::class.java).apply { action = ACTION_PLAY_OR_PAUSE },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val nextIntent = PendingIntent.getService(
            this, 1, Intent(this, MusicService::class.java).apply { action = ACTION_NEXT },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val prevIntent = PendingIntent.getService(
            this, 2, Intent(this, MusicService::class.java).apply { action = ACTION_PREVIOUS },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        rv.setOnClickPendingIntent(R.id.img_pause, playIntent)
        rv.setOnClickPendingIntent(R.id.img_next, nextIntent)
        rv.setOnClickPendingIntent(R.id.img_previous, prevIntent)
    }

    private fun playNext() {
        if (songList.isNotEmpty()) {
            currentIndex = (currentIndex + 1) % songList.size
            playSong(songList[currentIndex])
        }
    }

    private fun playPrevious() {
        if (songList.isNotEmpty()) {
            currentIndex = if (currentIndex - 1 < 0) songList.size - 1 else currentIndex - 1
            playSong(songList[currentIndex])
        }
    }

    override fun onDestroy() {
        mediaPlayer?.release()
        mediaPlayer = null
        super.onDestroy()
    }
}
