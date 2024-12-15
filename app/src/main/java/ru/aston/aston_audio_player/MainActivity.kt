package ru.aston.aston_audio_player

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity() {

    private lateinit var btnPlayPause: ImageButton
    private lateinit var receiver: BroadcastReceiver
    private val viewModel: MusicPlayerViewModel by viewModels()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnPlayPause = findViewById(R.id.btnPlayPause)

        viewModel.isPlaying.observe(this) { isPlaying ->
            if (isPlaying != null) {
                updatePlayPauseIcon(isPlaying)
            }
        }

        viewModel.currentSongIndex.observe(this) { songIndex ->
            updateCurrentSongTitle(songIndex)
        }

        btnPlayPause.setOnClickListener {
            sendServiceCommand("PLAY")
        }

        findViewById<ImageButton>(R.id.btnNext).setOnClickListener {
            sendServiceCommand("NEXT")
        }

        findViewById<ImageButton>(R.id.btnPrevious).setOnClickListener {
            sendServiceCommand("PREVIOUS")
        }

        receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val isPlaying = intent?.getBooleanExtra(
                    "IS_PLAYING", false
                ) ?: false
                val currentSongIndex = intent?.getIntExtra(
                    "CURRENT_SONG_INDEX", 0
                ) ?: 0

                viewModel.updatePlayingState(isPlaying)
                viewModel.updateCurrentSongIndex(currentSongIndex)
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(
                receiver,
                IntentFilter("MUSIC_PLAYER_STATE"), Context.RECEIVER_EXPORTED
            )
        } else {
            registerReceiver(receiver, IntentFilter("MUSIC_PLAYER_STATE"))
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (
                checkSelfPermission(
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(
                    arrayOf(
                        android.Manifest.permission.POST_NOTIFICATIONS
                    ), 100
                )
            }
        }
    }

    private fun updatePlayPauseIcon(isPlaying: Boolean) {
        val icon =
            if (
                isPlaying) R.drawable.baseline_pause_24
            else
                R.drawable.baseline_play_arrow_24
        btnPlayPause.setImageResource(icon)
    }

    private fun updateCurrentSongTitle(songIndex: Int) {
        val songTitle = "Playing Song ${songIndex + 1}"
        findViewById<TextView>(R.id.currentSongTitle).text = songTitle
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (
            requestCode == 100) {
            if (
                (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            ) {
                Log.d("MainActivity", "Notification permission granted")
            } else {
                Log.d("MainActivity", "Notification permission denied")
            }
        }
    }

    private fun sendServiceCommand(action: String) {
        val intent = Intent(this, MusicService::class.java)
        intent.action = action
        startService(intent)
    }
}


