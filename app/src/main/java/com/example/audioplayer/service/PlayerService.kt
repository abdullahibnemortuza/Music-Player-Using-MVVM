package com.example.audioplayer.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.support.v4.media.session.MediaSessionCompat
import androidx.core.app.NotificationCompat
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.example.audioplayer.view.MainActivity

class PlayerService : Service() {

    companion object {
        const val CHANNEL_ID = "music_playback_channel"
        const val NOTIF_ID = 1
    }

    private lateinit var player: ExoPlayer
    private lateinit var mediaSession: MediaSessionCompat

    override fun onCreate() {
        super.onCreate()
        player = ExoPlayer.Builder(this).build()
        mediaSession = MediaSessionCompat(this, "MusicService")
        createNotificationChannel()
    }

    fun setPlaylist(songs: List<com.example.audioplayer.model.Song>, startIndex: Int = 0) {
        player.clearMediaItems()
        songs.forEach { s ->
            val item = MediaItem.fromUri(s.uri)
            player.addMediaItem(item)
        }
        player.prepare()
        player.seekTo(startIndex, 0)
    }

    fun play() { player.play() }
    fun pause() { player.pause() }
    fun isPlaying(): Boolean = player.isPlaying
    fun seekTo(positionMs: Long) { player.seekTo(positionMs) }
    fun next(){ if (player.hasNextMediaItem()) player.seekToNext() }
    fun previous(){ if (player.hasPreviousMediaItem()) player.seekToPrevious() }
    fun getCurrentPosition() = player.currentPosition
    fun getDuration() = player.duration
    fun getCurrentIndex() = player.currentMediaItemIndex

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // handle media button intents if needed
        val pendingIntent = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notif = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Music Player")
            .setContentText("Playing")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentIntent(pendingIntent)
            .build()

        startForeground(NOTIF_ID, notif)
        return START_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Music playback",
                NotificationManager.IMPORTANCE_LOW
            )
            nm.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        player.release()
        mediaSession.release()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}