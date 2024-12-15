package ru.aston.aston_audio_player

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.Build
import android.os.IBinder
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat


class MusicService : Service() {

    private var mediaPlayer: MediaPlayer? = null
    private var currentSongIndex = 0
    private val songList = listOf(R.raw.song1, R.raw.song2, R.raw.song3)

    override fun onCreate() {
        super.onCreate()
        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()
        }
    }

    override fun onStartCommand(
        intent: Intent?, flags: Int, startId: Int
    ): Int {
        when (intent?.action) {
            "PLAY" -> playOrPause()
            "NEXT" -> playNext()
            "PREVIOUS" -> playPrevious()
        }
        return START_STICKY
    }

    private fun playOrPause() {
        if (
            mediaPlayer == null) {
            playCurrentSong()
        } else if (mediaPlayer!!.isPlaying) {
            mediaPlayer?.pause()
        } else {
            mediaPlayer?.start()
        }
        sendPlaybackState()
        updateNotification()
    }

    private fun playCurrentSong() {
        mediaPlayer?.stop()
        mediaPlayer?.release()

        val songResource = songList[currentSongIndex]
        mediaPlayer = MediaPlayer.create(this, songResource)
        mediaPlayer?.start()

        mediaPlayer?.setOnCompletionListener {
            playNext()
            sendPlaybackState()
        }
        updateNotification()
        sendPlaybackState()
    }

    private fun playNext() {
        currentSongIndex = (currentSongIndex + 1) % songList.size
        playCurrentSong()
        sendPlaybackState()
    }

    private fun playPrevious() {
        currentSongIndex = if (
            currentSongIndex - 1 < 0) songList.size - 1 else currentSongIndex - 1
        playCurrentSong()
        sendPlaybackState()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            "MUSIC_CHANNEL",
            "Music Playback",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Channel for music playback controls"
        }
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        super.onDestroy()
    }

    private fun sendPlaybackState() {
        val intent = Intent("MUSIC_PLAYER_STATE").apply {
            putExtra("IS_PLAYING", mediaPlayer?.isPlaying ?: false)
            putExtra("CURRENT_SONG_INDEX", currentSongIndex)
        }
        sendBroadcast(intent)
    }


    @SuppressLint("UnspecifiedImmutableFlag")
    private fun updateNotification() {
        val notification = buildNotification()
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(1, notification)
    }

    private fun buildNotification(): Notification {
        val playIntent = Intent(
            this, MusicService::class.java
        ).apply { action = "PLAY" }
        val nextIntent = Intent(
            this, MusicService::class.java
        ).apply { action = "NEXT" }
        val previousIntent = Intent(
            this, MusicService::class.java
        ).apply { action = "PREVIOUS" }

        val playPendingIntent = PendingIntent.getService(
            this,
            0,
            playIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val nextPendingIntent = PendingIntent.getService(
            this,
            0,
            nextIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val previousPendingIntent = PendingIntent.getService(
            this,
            0,
            previousIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val currentSongName = "Song ${currentSongIndex + 1}"
        val playPauseIcon =
            if (
                mediaPlayer?.isPlaying == true) R.drawable.baseline_pause_24
            else
                R.drawable.baseline_play_arrow_24

        return NotificationCompat.Builder(this, "MUSIC_CHANNEL")
            .setSmallIcon(R.drawable.baseline_music_note_24)
            .setContentTitle("Music Player")
            .setContentText("Playing $currentSongName")
            .addAction(
                R.drawable.baseline_skip_previous_24,
                "Previous",
                previousPendingIntent
            )
            .addAction(
                playPauseIcon,
                "Play/Pause",
                playPendingIntent
            )
            .addAction(
                R.drawable.baseline_skip_next_24,
                "Next",
                nextPendingIntent
            )
            .setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setShowActionsInCompactView(0, 1, 2)
            )
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOnlyAlertOnce(true)
            .build()
    }
}


//class MusicService : Service() {
//
//    private var mediaPlayer: MediaPlayer? = null
//    private var isPlaying = false
//    private var currentSongIndex = 0
//    private val songList = listOf(R.raw.song1, R.raw.song2, R.raw.song3)
//    private var isFirstStart = true
//
//    private lateinit var viewModel: MusicPlayerViewModel
//
//
//    @RequiresApi(Build.VERSION_CODES.O)
//    override fun onCreate() {
//        super.onCreate()
//        Log.d("MusicService", "onCreate called")
//        createNotificationChannel()
//    }
//
//    @RequiresApi(Build.VERSION_CODES.O)
//    private fun createNotificationChannel() {
//        val channel = NotificationChannel(
//            "MUSIC_CHANNEL",
//            "Music Playback",
//            NotificationManager.IMPORTANCE_LOW
//        ).apply {
//            description = "Channel for music playback controls"
//        }
//        val notificationManager = getSystemService(NotificationManager::class.java)
//        notificationManager.createNotificationChannel(channel)
//        Log.d("MusicService", "Notification channel created")
//    }
//
//    override fun onBind(intent: Intent?): IBinder? = null
//
//    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
//        if (isFirstStart) {
//            createNotification()
//            isFirstStart = false
//        }
//
//        when (intent?.action) {
//            "PLAY" -> playOrPause()
//            "NEXT" -> playNext()
//            "PREVIOUS" -> playPrevious()
//        }
//
//        return START_STICKY
//    }
//
//
//    private fun playOrPause() {
//        if (mediaPlayer == null) {
//            playCurrentSong()
//            isPlaying = true
//        } else if (mediaPlayer!!.isPlaying) {
//            mediaPlayer?.pause()
//            isPlaying = false
//        } else {
//            mediaPlayer?.start()
//            isPlaying = true
//        }
//        Log.d("MusicService", "playOrPause called. isPlaying: $isPlaying")
//        updateNotification()
//        sendPlaybackState()
//    }
//
//
//    private fun playCurrentSong() {
//
//        mediaPlayer?.stop()
//        mediaPlayer?.release()
//
//        mediaPlayer = MediaPlayer.create(this, songList[currentSongIndex])
//        mediaPlayer?.start()
//        isPlaying = true
//
//        mediaPlayer?.setOnCompletionListener {
//            playNext()
//        }
//        updateNotification()
//        sendPlaybackState()
//    }
//
//
//    private fun playNext() {
//        currentSongIndex = (currentSongIndex + 1) % songList.size
//        playCurrentSong()
//        updateNotification()
//    }
//
//    private fun playPrevious() {
//        currentSongIndex = if (currentSongIndex - 1 < 0) songList.size - 1 else currentSongIndex - 1
//        playCurrentSong()
//        updateNotification()
//    }
//
//
//    override fun onDestroy() {
//        mediaPlayer?.stop()
//        mediaPlayer?.release()
//        mediaPlayer = null
//        super.onDestroy()
//    }
//
//    private fun sendPlaybackState() {
//        Log.d("MusicService", "Sending state: $isPlaying")
//        val intent = Intent("MUSIC_PLAYER_STATE").apply {
//            putExtra("IS_PLAYING", isPlaying)
//        }
//        sendBroadcast(intent)
//        Log.d("MusicService", "Broadcast sent")
//    }
//
//
//    @SuppressLint("UnspecifiedImmutableFlag")
//    private fun createNotification() {
//        val playIntent = Intent(this, MusicService::class.java).apply { action = "PLAY" }
//        val nextIntent = Intent(this, MusicService::class.java).apply { action = "NEXT" }
//        val previousIntent = Intent(this, MusicService::class.java).apply { action = "PREVIOUS" }
//
//        val playPendingIntent =
//            PendingIntent.getService(this, 0, playIntent, PendingIntent.FLAG_IMMUTABLE)
//        val nextPendingIntent =
//            PendingIntent.getService(this, 0, nextIntent, PendingIntent.FLAG_IMMUTABLE)
//        val previousPendingIntent =
//            PendingIntent.getService(this, 0, previousIntent, PendingIntent.FLAG_IMMUTABLE)
//
//        val songName = songList.getOrNull(currentSongIndex) ?: "Unknown"
//
//        val notification = NotificationCompat.Builder(this, "MUSIC_CHANNEL")
//            .setSmallIcon(R.drawable.baseline_music_note_24)
//            .setContentTitle("Music Player")
//            .setContentText("Playing $songName")
//            .addAction(
//                R.drawable.baseline_skip_previous_24,
//                "Previous",
//                previousPendingIntent
//            )
//            .addAction(
//                R.drawable.baseline_play_arrow_24,
//                "Play/Pause",
//                playPendingIntent
//            )
//            .addAction(
//                R.drawable.baseline_skip_next_24,
//                "Next",
//                nextPendingIntent
//            )
//            .setStyle(
//                androidx.media.app.NotificationCompat.MediaStyle()
//                    .setShowActionsInCompactView(0, 1, 2)
//            )
//            .setPriority(NotificationCompat.PRIORITY_LOW)
//            .build()
//        startForeground(1, notification)
//        Log.d("MusicService", "Foreground service started with notification")
//    }
//
//    @SuppressLint("UnspecifiedImmutableFlag")
//    private fun updateNotification() {
//
//        Log.d("MusicService", "Updating notification. isPlaying: $isPlaying")
//
//        val playIntent = Intent(this, MusicService::class.java).apply { action = "PLAY" }
//        val nextIntent = Intent(this, MusicService::class.java).apply { action = "NEXT" }
//        val previousIntent = Intent(this, MusicService::class.java).apply { action = "PREVIOUS" }
//
//        val playPendingIntent =
//            PendingIntent.getService(this, 0, playIntent, PendingIntent.FLAG_IMMUTABLE)
//        val nextPendingIntent =
//            PendingIntent.getService(this, 0, nextIntent, PendingIntent.FLAG_IMMUTABLE)
//        val previousPendingIntent =
//            PendingIntent.getService(this, 0, previousIntent, PendingIntent.FLAG_IMMUTABLE)
//
//        val songName = songList.getOrNull(currentSongIndex) ?: "Unknown"
//
//        val playPauseIcon =
//            if (isPlaying) R.drawable.baseline_pause_24 else R.drawable.baseline_play_arrow_24
//
//        Log.d("MusicService", "Setting playPauseIcon: $playPauseIcon")
//
//        val notification = NotificationCompat.Builder(this, "MUSIC_CHANNEL")
//            .setSmallIcon(R.drawable.baseline_music_note_24)
//            .setContentTitle("Music Player")
//            .setContentText("Playing $songName")
//            .addAction(
//                R.drawable.baseline_skip_previous_24,
//                "Previous",
//                previousPendingIntent
//            )
//            .addAction(
//                playPauseIcon,
//                "Play/Pause",
//                playPendingIntent
//            )
//            .addAction(
//                R.drawable.baseline_skip_next_24,
//                "Next",
//                nextPendingIntent
//            )
//            .setStyle(
//                androidx.media.app.NotificationCompat.MediaStyle()
//                    .setShowActionsInCompactView(0, 1, 2)
//            )
//            .setPriority(NotificationCompat.PRIORITY_LOW)
//            .setOnlyAlertOnce(true)
//            .build()
//
//        val notificationManager = getSystemService(NotificationManager::class.java)
//
//        notificationManager.cancel(1)
//        notificationManager.notify(1, notification)
//        Log.d("MusicService", "Notification updated")
//    }
//
//}

