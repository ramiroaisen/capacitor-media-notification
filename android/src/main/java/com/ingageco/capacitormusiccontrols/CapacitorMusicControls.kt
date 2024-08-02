package com.ingageco.capacitormusiccontrols

import android.app.Activity
import android.app.Notification
import android.app.PendingIntent
import android.bluetooth.BluetoothHeadset
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.AudioManager
import android.media.session.MediaSession
import android.net.Uri
import android.os.Build
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.core.content.ContextCompat
import com.getcapacitor.JSObject
import com.getcapacitor.Plugin
import com.getcapacitor.PluginCall
import com.getcapacitor.PluginMethod
import com.getcapacitor.annotation.CapacitorPlugin
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

@CapacitorPlugin(name = "CapacitorMusicControls")
class CapacitorMusicControls : Plugin() {
    var mMessageReceiver: MusicControlsBroadcastReceiver? = null
    var notification: MusicControlsNotification? = null
    var mediaSessionCompat: MediaSessionCompat? = null
    val notificationID = 7824
    var mAudioManager: AudioManager? = null
    var mediaButtonPendingIntent: PendingIntent? = null
    var mediaButtonAccess = true
    var token: MediaSession.Token? = null
    var mConnection: MusicControlsServiceConnection? = null


    val mMediaSessionCallback = MediaSessionCallback(this)


    @PluginMethod
    fun create(call: PluginCall) {
        try {

            val options: JSObject = call.data

            val context: Context = activity.applicationContext

            initialize()

            val infos = MusicControlsInfos(options)

            val metadataBuilder: MediaMetadataCompat.Builder = MediaMetadataCompat.Builder()

            notification?.updateNotification(infos)

            // track title
            metadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_TITLE, infos.track)
            // artists
            metadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_ARTIST, infos.artist)
            //album
            metadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_ALBUM, infos.album)

            val cover = infos.cover;
            if(cover != null) {
                val art = getBitmapCover(cover)
                if (art != null) {
                    metadataBuilder.putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, art)
                    metadataBuilder.putBitmap(MediaMetadataCompat.METADATA_KEY_ART, art)
                }
            }

            mediaSessionCompat?.setMetadata(metadataBuilder.build())

            if (infos.isPlaying == true) setMediaPlaybackState(PlaybackStateCompat.STATE_PLAYING)
            else setMediaPlaybackState(PlaybackStateCompat.STATE_PAUSED)

            call.resolve()
        } catch (e: Exception) {
            call.reject("error in initializing MusicControlsInfos $e")
        }
    }


    private fun registerBroadcaster(mMessageReceiver: MusicControlsBroadcastReceiver?) {
        val context: Context = activity.applicationContext
        val filter: IntentFilter = IntentFilter()
        filter.addAction("music-controls-previous")
        filter.addAction("music-controls-pause")
        filter.addAction("music-controls-play")
        filter.addAction("music-controls-next")
        filter.addAction("music-controls-media-button")
        filter.addAction("music-controls-destroy")
        filter.addAction(Intent.ACTION_HEADSET_PLUG)
        filter.addAction(BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED)

        ContextCompat.registerReceiver(
            context,
            mMessageReceiver,
            filter,
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
    }

    // Register pendingIntent for broacast
    fun registerMediaButtonEvent() {
        mediaSessionCompat?.setMediaButtonReceiver(this.mediaButtonPendingIntent)
    }

    fun unregisterMediaButtonEvent() {
        mediaSessionCompat?.setMediaButtonReceiver(null)
        mediaSessionCompat?.release()
    }

    fun destroyPlayerNotification() {
        if (this.notification != null) {
            try {
                this.notification?.destroy()
                this.notification = null;
            } catch (e: NullPointerException) {
                e.printStackTrace()
            }
        }
    }


    fun initialize() {
        val context: Context = activity.applicationContext

        val mConnection = MusicControlsServiceConnection(activity)
        this.mConnection = mConnection


        // avoid spawning multiple receivers
        if (this.mMessageReceiver != null) {
            try {
                context.unregisterReceiver(this.mMessageReceiver)
            } catch (e: IllegalArgumentException) {
                e.printStackTrace()
            }

            unregisterMediaButtonEvent()
        }

        // end avoid spawn
        this.mMessageReceiver = MusicControlsBroadcastReceiver(this)
        this.registerBroadcaster(this.mMessageReceiver)

        this.mediaSessionCompat = MediaSessionCompat(
            context,
            "capacitor-music-controls-media-session",
            null,
            this.mediaButtonPendingIntent
        )
        mediaSessionCompat?.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS)

        val token: MediaSessionCompat.Token? = mediaSessionCompat?.sessionToken;
        this.token = token?.token as MediaSession.Token?

        setMediaPlaybackState(PlaybackStateCompat.STATE_PAUSED)

        mediaSessionCompat?.setActive(true)
        val callback: MediaSessionCallback = this.mMediaSessionCallback;
        if(callback != null){
            mediaSessionCompat?.setCallback(callback)
        }

        val thisToken = this.token;
        val thisNotificationID = this.notificationID;

        if(thisToken != null) {
            this.notification =
                object : MusicControlsNotification(activity, thisNotificationID, thisToken) {
                    override fun onNotificationUpdated(notification: Notification?) {
                        val isPlaying = infos?.isPlaying;
                        if(isPlaying != null) {
                            mConnection.setNotification(notification, isPlaying)
                        }
                    }

                    override fun onNotificationDestroyed() {
                        mConnection.setNotification(null, false)
                    }
                }
        }


        // Register media (headset) button event receiver
        try {
            this.mAudioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            val headsetIntent: Intent = Intent("music-controls-media-button")
            this.mediaButtonPendingIntent = PendingIntent.getBroadcast(
                context, 0, headsetIntent,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE else PendingIntent.FLAG_UPDATE_CURRENT
            )
            this.registerMediaButtonEvent()
        } catch (e: Exception) {
            this.mediaButtonAccess = false
            e.printStackTrace()
        }

        val startServiceIntent: Intent =
            Intent(activity, MusicControlsNotificationKiller::class.java)
        startServiceIntent.putExtra("notificationID", this.notificationID)

        val mConn = this.mConnection;
        if(mConn != null) {
            activity.bindService(startServiceIntent, mConn, Context.BIND_AUTO_CREATE)
        }
    }


    @PluginMethod
    fun destroy(call: PluginCall) {
        val context: Context = activity.applicationContext

        this.destroyPlayerNotification()
        this.stopMessageReceiver(context)
        this.unregisterMediaButtonEvent()
        this.stopServiceConnection(activity)


        call.resolve()
    }


    override fun handleOnDestroy() {
        val context: Context = activity.applicationContext

        this.destroyPlayerNotification()
        this.stopMessageReceiver(context)
        this.unregisterMediaButtonEvent()
        this.stopServiceConnection(activity)
    }

    fun stopMessageReceiver(context: Context) {
        if (this.mMessageReceiver != null) {
            mMessageReceiver?.stopListening()
            try {
                context.unregisterReceiver(this.mMessageReceiver)
            } catch (e: IllegalArgumentException) {
                e.printStackTrace()
            }
            this.mMessageReceiver = null
        }
    }

    fun stopServiceConnection(activity: Activity) {
        val mConn = this.mConnection;
        if (mConn != null) {
            val stopServiceIntent: Intent =
                Intent(activity, MusicControlsNotificationKiller::class.java)
            activity.unbindService(mConn)
            activity.stopService(stopServiceIntent)
            this.mConnection = null
        }
    }

    @PluginMethod
    fun updateIsPlaying(call: PluginCall) {
        try {
            val params: JSObject = call.data

            val notification = this.notification;

            if (notification == null) {
                call.resolve()
                return
            }

            val isPlaying: Boolean = params.getBoolean("isPlaying")
            notification.updateIsPlaying(isPlaying)

            if (isPlaying) setMediaPlaybackState(PlaybackStateCompat.STATE_PLAYING)
            else setMediaPlaybackState(PlaybackStateCompat.STATE_PAUSED)

            call.resolve()
        } catch (e: Exception) {
            call.reject("error updateIsPlaying: $e")
        }
    }

    @PluginMethod
    fun updateElapsed(call: PluginCall) {
        call.reject("updateElapsed() unimplemented");
        /*
        try {

            val params: JSObject = call.data

            val notification = this.notification;
            if(notification == null) {
                call.resolve()
                return;
            }

            // final JSONObject params = args.getJSONObject(0);
            val isPlaying: Boolean = params.getBoolean("isPlaying")

            if(isPlaying != null) {
                notification.updateIsPlaying(isPlaying)
                if (isPlaying) setMediaPlaybackState(PlaybackStateCompat.STATE_PLAYING)
                else setMediaPlaybackState(PlaybackStateCompat.STATE_PAUSED)
            }

            call.resolve()
        } catch (e: Exception) {
            call.reject("error updateElapsed: $e")
        }
        */
    }

    @PluginMethod
    fun updateDismissable(call: PluginCall) {
        // final JSONObject params = args.getJSONObject(0);
        try {
            val notification = this.notification;
            if(notification == null) {
                call.resolve();
                return;
            }

            val params: JSObject = call.data
            val dismissable: Boolean = params.getBoolean("dismissable")
            notification.updateDismissable(dismissable)
            call.resolve()
        } catch (e: Exception) {
            call.reject("error updateDismissable: $e")
        }
    }

    fun controlsNotification(ret: JSObject) {
        Log.i(TAG, "controlsNotification fired " + ret.getString("message"))
        this.notifyListeners("event", ret, true);
        // notifyListeners("controlsNotification", ret);
        // this.bridge.triggerJSEvent("controlsNotification", "document", ret.toString())
    }

    private fun setMediaPlaybackState(state: Int) {
        val playbackStateBuilder: PlaybackStateCompat.Builder = PlaybackStateCompat.Builder()
        if (state == PlaybackStateCompat.STATE_PLAYING) {
            playbackStateBuilder.setActions(
                PlaybackStateCompat.ACTION_PLAY_PAUSE or PlaybackStateCompat.ACTION_PAUSE or PlaybackStateCompat.ACTION_SKIP_TO_NEXT or PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS or
                        PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID or
                        PlaybackStateCompat.ACTION_PLAY_FROM_SEARCH
            )
            playbackStateBuilder.setState(
                state,
                PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN,
                1.0f
            )
        } else {
            playbackStateBuilder.setActions(
                PlaybackStateCompat.ACTION_PLAY_PAUSE or PlaybackStateCompat.ACTION_PLAY or PlaybackStateCompat.ACTION_SKIP_TO_NEXT or PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS or
                        PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID or
                        PlaybackStateCompat.ACTION_PLAY_FROM_SEARCH
            )
            playbackStateBuilder.setState(state, PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 0f)
        }
        mediaSessionCompat?.setPlaybackState(playbackStateBuilder.build())
    }

    // Get image from url
    private fun getBitmapCover(coverURL: String): Bitmap? {
        try {
            return if (coverURL.matches("^(https?|ftp)://.*$".toRegex())) // Remote image
                getBitmapFromURL(coverURL)
            else {
                // Local image
                getBitmapFromLocal(coverURL)
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            return null
        }
    }

    // get Local image
    private fun getBitmapFromLocal(localURL: String): Bitmap? {
        try {
            val uri = Uri.parse(localURL)
            val file = File(uri.path)
            val fileStream = FileInputStream(file)
            val buf = BufferedInputStream(fileStream)
            val myBitmap: Bitmap = BitmapFactory.decodeStream(buf)
            buf.close()
            return myBitmap
        } catch (ex: Exception) {
            try {
                val fileStream: InputStream = activity.assets.open("public/$localURL")
                val buf = BufferedInputStream(fileStream)
                val myBitmap: Bitmap = BitmapFactory.decodeStream(buf)
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
            val myBitmap: Bitmap = BitmapFactory.decodeStream(input)
            return myBitmap
        } catch (ex: Exception) {
            ex.printStackTrace()
            return null
        }
    }

    companion object {
        private const val TAG = "CapacitorMusicControls"
    }
}
