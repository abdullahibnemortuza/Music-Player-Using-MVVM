package com.example.audioplayer.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.audioplayer.model.Song
import com.example.audioplayer.repository.SongRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PlayerViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = SongRepository(application.applicationContext)
    private val _songs = MutableLiveData<List<Song>>(emptyList())
    val songs: LiveData<List<Song>> = _songs

    private val _currentIndex = MutableLiveData<Int>(-1)
    val currentIndex: LiveData<Int> = _currentIndex

    private val _isPlaying = MutableLiveData<Boolean>(false)
    val isPlaying: LiveData<Boolean> = _isPlaying

    init {
        loadSongs()
    }

    private fun loadSongs() {
        CoroutineScope(Dispatchers.IO).launch {
            val list = repo.fetchSongs()
            _songs.postValue(list)
        }
    }

    // Simple controller functions; UI will call these. For production, these should delegate to a bound service.
    fun setCurrentIndex(index: Int) {
        _currentIndex.value = index
    }

    fun setPlaying(playing: Boolean) {
        _isPlaying.value = playing
    }
}