package com.ingageco.capacitormusiccontrols

import android.content.Intent
import android.os.Bundle
import android.support.v4.media.session.MediaSessionCompat
import android.view.KeyEvent
import com.getcapacitor.JSObject

class MediaSessionCallback(private val musicControls: CapacitorMusicControls) :
    MediaSessionCompat.Callback() {
    override fun onPlay() {
        super.onPlay()
        val ret = JSObject()
        ret.put("message", "music-controls-play")
        musicControls.controlsNotification(ret)
    }

    override fun onPause() {
        super.onPause()
        val ret = JSObject()
        ret.put("message", "music-controls-pause")
        musicControls.controlsNotification(ret)
    }

    override fun onSkipToNext() {
        super.onSkipToNext()
        val ret = JSObject()
        ret.put("message", "music-controls-next")
        musicControls.controlsNotification(ret)
    }

    override fun onSkipToPrevious() {
        super.onSkipToPrevious()
        val ret = JSObject()
        ret.put("message", "music-controls-previous")
        musicControls.controlsNotification(ret)
    }

    override fun onPlayFromMediaId(mediaId: String, extras: Bundle) {
        super.onPlayFromMediaId(mediaId, extras)
    }

    override fun onMediaButtonEvent(mediaButtonIntent: Intent): Boolean {
        val event = mediaButtonIntent.extras?.get(Intent.EXTRA_KEY_EVENT) as KeyEvent?
        val ret = JSObject()

        if (event == null) {
            return super.onMediaButtonEvent(mediaButtonIntent)
        }

        if (event.action == KeyEvent.ACTION_DOWN) {
            when (event.keyCode) {
                KeyEvent.KEYCODE_MEDIA_PAUSE -> {
                    ret.put("message", "music-controls-pause")
                    musicControls.controlsNotification(ret)
                }

                KeyEvent.KEYCODE_MEDIA_PLAY -> {
                    ret.put("message", "music-controls-play")
                    musicControls.controlsNotification(ret)
                }

                KeyEvent.KEYCODE_MEDIA_PREVIOUS -> {
                    ret.put("message", "music-controls-previous")
                    musicControls.controlsNotification(ret)
                }

                KeyEvent.KEYCODE_MEDIA_NEXT -> {
                    ret.put("message", "music-controls-next")
                    musicControls.controlsNotification(ret)
                }

                KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE -> {
                    ret.put("message", "music-controls-toggle-play-pause")
                    musicControls.controlsNotification(ret)
                }

                KeyEvent.KEYCODE_MEDIA_STOP -> {
                    ret.put("message", "music-controls-stop")
                    musicControls.controlsNotification(ret)
                }

                KeyEvent.KEYCODE_MEDIA_FAST_FORWARD -> {
                    ret.put("message", "music-controls-skip-forward")
                    musicControls.controlsNotification(ret)
                }

                KeyEvent.KEYCODE_MEDIA_REWIND -> {
                    ret.put("message", "music-controls-skip-backward")
                    musicControls.controlsNotification(ret)
                }

                else -> {
                    ret.put("message", "music-controls-media-button-unknown-${event.keyCode}")
                    musicControls.controlsNotification(ret)

                    return super.onMediaButtonEvent(mediaButtonIntent)
                }
            }
        }

        return true
    }
}

