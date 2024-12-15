package ru.aston.aston_audio_player


import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MusicPlayerViewModel : ViewModel() {

    private val _isPlaying = MutableLiveData<Boolean?>(false)
    val isPlaying: MutableLiveData<Boolean?> get() = _isPlaying

    private val _currentSongIndex = MutableLiveData<Int>(0)
    val currentSongIndex: LiveData<Int> get() = _currentSongIndex

    private val songList = listOf("Song 1", "Song 2", "Song 3")

    fun updatePlayingState(isPlaying: Boolean) {
        _isPlaying.value = isPlaying
    }

    fun updateCurrentSongIndex(index: Int) {
        _currentSongIndex.value = index
    }

    fun getCurrentSongName(): String {
        val index = _currentSongIndex.value ?: 0
        return songList[index]
    }
}
