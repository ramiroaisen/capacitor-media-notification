package com.ingageco.capacitormusiccontrols

import android.R
import android.app.Activity
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.session.MediaSession
import android.net.Uri
import android.os.Build
import android.util.Log
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.net.HttpURLConnection
import java.net.URL

open class MusicControlsNotification(
    private val cordovaActivity: Activity,
    private val notificationID: Int,
    private val token: MediaSession.Token
) {
    private val notificationManager: NotificationManager
    private var notificationBuilder: Notification.Builder? = null
    protected var infos: MusicControlsInfos? = null
    private var bitmapCover: Bitmap? = null
    private val CHANNEL_ID = "capacitor-music-channel-id"


    // Public Constructor
    init {
        val context = cordovaActivity.applicationContext
        this.notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // use channelId for Oreo and higher
        if (Build.VERSION.SDK_INT >= 26) {
            // The user-visible name of the channel.
            val name: CharSequence = "Audio Controls"
            // The user-visible description of the channel.
            val description = "Control Playing Audio"

            val importance = NotificationManager.IMPORTANCE_LOW

            val mChannel = NotificationChannel(this.CHANNEL_ID, name, importance)

            // Configure the notification channel.
            mChannel.description = description

            notificationManager.createNotificationChannel(mChannel)
        }
    }

    // Show or update notification
    fun updateNotification(newInfos: MusicControlsInfos) {
        Log.i(TAG, "updateNotification: infos: $newInfos")
        // Check if the cover has changed
        val cover = newInfos.cover
        if(cover == null) {
            this.bitmapCover = null
        } else {
            if (this.infos?.cover != cover) {
                this.getBitmapCover(cover)
            }
        }

        this.infos = newInfos
        this.createBuilder()
        this.createNotification()
    }

    private fun createNotification() {
        // FORK: Attention, is this correct or should it set this.notification
        val notification = notificationBuilder?.build()
        if(notification != null) {
            notificationManager.notify(this.notificationID, notification)
            this.onNotificationUpdated(notification)
        }
    }

    // Toggle the play/pause button
    fun updateIsPlaying(isPlaying: Boolean) {
        Log.i(TAG, "updateIsPlaying: isPlaying: $isPlaying")
        Log.i(TAG, "updateIsPlaying: pre:this.infos.isPlaying: ${infos?.isPlaying}")
        infos?.isPlaying = isPlaying
        Log.i(TAG, "updateIsPlaying: post:this.infos.isPlaying: ${infos?.isPlaying}")
        this.createBuilder()
        this.createNotification()
    }

    // Toggle the dismissable status
    fun updateDismissable(dismissable: Boolean) {
        infos?.dismissable = dismissable
        this.createBuilder()
        this.createNotification()
    }

    // Get image from url
    private fun getBitmapCover(coverURL: String) {
        try {
            if (coverURL.matches("^(https?|ftp)://.*$".toRegex())) // Remote image
                this.bitmapCover = getBitmapFromURL(coverURL)
            else {
                // Local image
                this.bitmapCover = getBitmapFromLocal(coverURL)
            }
        } catch (ex: Exception) {
            this.bitmapCover = null
            ex.printStackTrace()
        }
    }

    // get Local image
    private fun getBitmapFromLocal(localURL: String): Bitmap? {
        try {
            val uri = Uri.parse(localURL)
            val file = File(uri.path)
            val fileStream = FileInputStream(file)
            val buf = BufferedInputStream(fileStream)
            val myBitmap = BitmapFactory.decodeStream(buf)
            buf.close()
            return myBitmap
        } catch (ex: Exception) {
            try {
                val fileStream = cordovaActivity.assets.open("public/$localURL")
                val buf = BufferedInputStream(fileStream)
                val myBitmap = BitmapFactory.decodeStream(buf)
                buf.close()
                return myBitmap
            } catch (ex2: Exception) {
                ex.printStackTrace()
                ex2.printStackTrace()
                return null
            }
        }
    }

    // get Remote image
    private fun getBitmapFromURL(strURL: String): Bitmap? {
        try {
            val url = URL(strURL)
            val connection = url.openConnection() as HttpURLConnection
            connection.doInput = true
            connection.connect()
            val input = connection.inputStream
            val myBitmap = BitmapFactory.decodeStream(input)
            return myBitmap
        } catch (ex: Exception) {
            ex.printStackTrace()
            return null
        }
    }

    private fun createBuilder() {
        val context = cordovaActivity.applicationContext
        val builder = Notification.Builder(context)

        // use channelId for Oreo and higher
        if (Build.VERSION.SDK_INT >= 26) {
            builder.setChannelId(this.CHANNEL_ID)
        }

        val track = infos?.track
        if(track != null) builder.setContentTitle(track)

        val artist = infos?.artist
        if(artist != null) builder.setContentText(artist)

        builder.setWhen(0)

        val dismissable = infos?.dismissable ?: true
        // set if the notification can be destroyed by swiping
        if (dismissable) {
            builder.setOngoing(false)
            val dismissIntent = Intent("music-controls-destroy")
            val dismissPendingIntent = PendingIntent.getBroadcast(
                context,
                1,
                dismissIntent,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) PendingIntent.FLAG_IMMUTABLE else 0
            )
            builder.setDeleteIntent(dismissPendingIntent)
        } else {
            builder.setOngoing(true)
        }

        val ticker = infos?.ticker
        if (ticker != null) {
            builder.setTicker(ticker)
        }

        builder.setPriority(Notification.PRIORITY_MAX)

        builder.setVisibility(Notification.VISIBILITY_PUBLIC)

        //Set SmallIcon
        val notificationIcon = infos?.notificationIcon
        var usePlayingIcon = notificationIcon != null
        if (notificationIcon != null) {
            val resId = this.getResourceId(notificationIcon, 0)
            usePlayingIcon = resId == 0
            if (!usePlayingIcon) {
                builder.setSmallIcon(resId)
            }
        }

        if (usePlayingIcon) {
            if (infos?.isPlaying == true) {
                val playIcon = infos?.playIcon
                if(playIcon != null) {
                    builder.setSmallIcon(this.getResourceId(playIcon, R.drawable.ic_media_play))
                }
            } else {
                val pauseIcon = infos?.pauseIcon
                if(pauseIcon != null) {
                    builder.setSmallIcon(this.getResourceId(pauseIcon, R.drawable.ic_media_pause))
                }
            }
        }

        // Set LargeIcon0
        val bitmapCover = this.bitmapCover
        if(infos?.cover != null && bitmapCover != null) {
            builder.setLargeIcon(bitmapCover)
        }

        // Open app if tapped
        val resultIntent = Intent(context, cordovaActivity.javaClass)
        resultIntent.setAction(Intent.ACTION_MAIN)
        resultIntent.addCategory(Intent.CATEGORY_LAUNCHER)
        val resultPendingIntent = PendingIntent.getActivity(
            context,
            0,
            resultIntent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) PendingIntent.FLAG_IMMUTABLE else 0
        )
        builder.setContentIntent(resultPendingIntent)

        // Controls
        var nbControls = 0
        /* Previous  */
        if (infos?.hasPrev == true) {
            Log.i(TAG, "controls hasPrev")
            nbControls++
            val previousIntent = Intent("music-controls-previous")
            val previousPendingIntent = PendingIntent.getBroadcast(
                context,
                1,
                previousIntent,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) PendingIntent.FLAG_IMMUTABLE else 0
            )
            val prevIcon = infos?.prevIcon
            if(prevIcon != null) {
                builder.addAction(
                    this.getResourceId(prevIcon, R.drawable.ic_media_previous),
                    "",
                    previousPendingIntent
                )
            }

        }
        if (infos?.isPlaying == true) {
            /* Pause  */
            nbControls++
            val pauseIntent = Intent("music-controls-pause")
            val pausePendingIntent = PendingIntent.getBroadcast(
                context,
                1,
                pauseIntent,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) PendingIntent.FLAG_IMMUTABLE else 0
            )
            val pauseIcon = infos?.pauseIcon
            if(pauseIcon != null) {
                builder.addAction(
                    this.getResourceId(pauseIcon, R.drawable.ic_media_pause),
                    "",
                    pausePendingIntent
                )
            }
        } else {
            /* Play  */
            nbControls++
            val playIntent = Intent("music-controls-play")
            val playPendingIntent = PendingIntent.getBroadcast(
                context,
                1,
                playIntent,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) PendingIntent.FLAG_IMMUTABLE else 0
            )
            val playIcon = infos?.playIcon
            if(playIcon != null) {
                builder.addAction(
                    this.getResourceId(playIcon, R.drawable.ic_media_play),
                    "",
                    playPendingIntent
                )
            }
        }
        /* Next */
        if (infos?.hasNext == true) {
            Log.i(TAG, "controls hasNext")
            nbControls++
            val nextIntent = Intent("music-controls-next")
            val nextPendingIntent = PendingIntent.getBroadcast(
                context,
                1,
                nextIntent,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) PendingIntent.FLAG_IMMUTABLE else 0
            )
            val nextIcon = infos?.nextIcon
            if(nextIcon != null) {
                builder.addAction(
                    this.getResourceId(nextIcon, R.drawable.ic_media_next),
                    "",
                    nextPendingIntent
                )
            }
        }
        /* Close */
        if (infos?.hasClose == true) {
            Log.i(TAG, "controls hasClose")
            nbControls++
            val destroyIntent = Intent("music-controls-destroy")
            val destroyPendingIntent = PendingIntent.getBroadcast(
                context,
                1,
                destroyIntent,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) PendingIntent.FLAG_IMMUTABLE else 0
            )
            val closeIcon = infos?.closeIcon
            if(closeIcon != null) {
                builder.addAction(
                    this.getResourceId(
                        closeIcon,
                        R.drawable.ic_menu_close_clear_cancel
                    ), "", destroyPendingIntent
                )
            }
        }

        val args = IntArray(nbControls)
        for (i in 0 until nbControls) {
            args[i] = i
        }
        builder.setStyle(
            Notification.MediaStyle().setShowActionsInCompactView(*args).setMediaSession(
                this.token
            )
        )

        this.notificationBuilder = builder
    }

    private fun getResourceId(name: String, fallback: Int): Int {
        try {
            if (name.isEmpty()) {
                return fallback
            }

            val resId = cordovaActivity.resources.getIdentifier(
                name, "drawable",
                cordovaActivity.packageName
            )
            return if (resId == 0) fallback else resId
        } catch (ex: Exception) {
            return fallback
        }
    }

    fun destroy() {
        Log.i(TAG, "Destroying notification")
        notificationManager.cancel(this.notificationID)
        this.onNotificationDestroyed()
        Log.i(TAG, "Notification destroyed")
    }

    protected open fun onNotificationUpdated(notification: Notification?) {}
    protected open fun onNotificationDestroyed() {}

    companion object {
        private const val TAG = "CMCNotification"
    }
}
