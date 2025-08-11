package com.example.audioplayer.view

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.SeekBar
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Observer
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.audioplayer.R
import com.example.audioplayer.databinding.ActivityMainBinding
import com.example.audioplayer.service.PlayerService
import com.example.audioplayer.view.adapter.SongAdapter
import com.example.audioplayer.viewmodel.PlayerViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val vm: PlayerViewModel by viewModels()
    private lateinit var adapter: SongAdapter

    // local ExoPlayer for demo (you can move to Service or bind service in advanced step)
    private lateinit var player: ExoPlayer
    private val uiHandler = Handler(Looper.getMainLooper())
    private val progressRunnable = object : Runnable {
        override fun run() {
            if (::player.isInitialized) {
                val pos = player.currentPosition
                binding.seekBar.progress = pos.toInt()
                uiHandler.postDelayed(this, 500)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        player = ExoPlayer.Builder(this).build()

        adapter = SongAdapter(emptyList()) { pos ->
            playAt(pos)
        }

        binding.rvSongs.layoutManager = LinearLayoutManager(this)
        binding.rvSongs.adapter = adapter

        vm.songs.observe(this, Observer { list ->
            adapter.submitList(list)
        })

        vm.currentIndex.observe(this) { idx ->
            val s = vm.songs.value?.getOrNull(idx)
            binding.tvTitle.text = s?.title ?: "-"
            binding.tvArtist.text = s?.artist ?: "-"
        }

        requestPermissionsIfNeeded()

        binding.btnPlayPause.setOnClickListener {
            if (!::player.isInitialized) return@setOnClickListener
            if (player.isPlaying) {
                player.pause()
                binding.btnPlayPause.setImageResource(android.R.drawable.ic_media_play)
            } else {
                player.play()
                binding.btnPlayPause.setImageResource(android.R.drawable.ic_media_pause)
            }
        }
        binding.btnNext.setOnClickListener { next() }
        binding.btnPrev.setOnClickListener { previous() }

        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                // nothing
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                seekBar?.let {
                    player.seekTo(it.progress.toLong())
                }
            }
        })
    }

    private fun playAt(position: Int) {
        val list = vm.songs.value ?: return
        vm.setCurrentIndex(position)

        player.clearMediaItems()
        list.forEach { s ->
            player.addMediaItem(MediaItem.fromUri(s.uri))
        }
        player.prepare()
        player.seekTo(position, 0)
        player.play()

        binding.btnPlayPause.setImageResource(android.R.drawable.ic_media_pause)

        binding.seekBar.max = player.duration.toInt().takeIf { it > 0 } ?: 0
        uiHandler.post(progressRunnable)

        // start foreground service if you want persistent notification (optional)
        val intent = Intent(this, PlayerService::class.java)
        startService(intent)
    }

    private fun next() {
        if (player.hasNextMediaItem()) {
            player.seekToNext()
        }
    }

    private fun previous() {
        if (player.hasPreviousMediaItem()) {
            player.seekToPrevious()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        uiHandler.removeCallbacks(progressRunnable)
        player.release()
    }

    private fun requestPermissionsIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_MEDIA_AUDIO), 1001)
            }
        } else {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 1000)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == 1000 || requestCode == 1001) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // reload songs
                vm // already loaded in init; but we can reload if needed
            } else {
                Toast.makeText(this, "Permission required to read audio files", Toast.LENGTH_LONG).show()
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }
}