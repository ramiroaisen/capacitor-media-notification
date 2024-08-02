package com.ingageco.capacitormusiccontrols


import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Parcelable
import android.util.Log
import android.view.KeyEvent
import com.getcapacitor.JSObject

class MusicControlsBroadcastReceiver(private val musicControls: CapacitorMusicControls) :
    BroadcastReceiver() {
    fun stopListening() {
        val ret = JSObject()
        ret.put("message", "music-controls-stop-listening")

        musicControls.controlsNotification(ret)
    }

    override fun onReceive(context: Context, intent: Intent) {
        val message = intent.action
        val ret = JSObject()

        Log.i(TAG, "onReceive fired $message")



        if (message == Intent.ACTION_HEADSET_PLUG) {
            // Headphone plug/unplug
            val state: Int = intent.getIntExtra("state", -1)
            when (state) {
                0 -> {
                    ret.put("message", "music-controls-headset-unplugged")

                    musicControls.controlsNotification(ret)

                    musicControls.unregisterMediaButtonEvent()
                }

                1 -> {
                    ret.put("message", "music-controls-headset-plugged")

                    musicControls.registerMediaButtonEvent()
                }

                else -> {}
            }
        } else if (message == "music-controls-media-button") {
            // Media button
            val event = intent.getParcelableExtra<Parcelable>(Intent.EXTRA_KEY_EVENT) as KeyEvent
            if (event.action == KeyEvent.ACTION_DOWN) {
                val keyCode = event.keyCode
                when (keyCode) {
                    KeyEvent.KEYCODE_MEDIA_NEXT -> {
                        ret.put("message", "music-controls-next")
                        musicControls.controlsNotification(ret)
                    }

                    KeyEvent.KEYCODE_MEDIA_PAUSE -> {
                        ret.put("message", "music-controls-pause")
                        musicControls.controlsNotification(ret)
                    }

                    KeyEvent.KEYCODE_MEDIA_PLAY -> {
                        ret.put("message", "music-controls-play")
                        musicControls.controlsNotification(ret)
                    }

                    KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE -> {
                        ret.put("message", "music-controls-toggle-play-pause")
                        musicControls.controlsNotification(ret)
                    }

                    KeyEvent.KEYCODE_MEDIA_PREVIOUS -> {
                        ret.put("message", "music-controls-previous")
                        musicControls.controlsNotification(ret)
                    }

                    KeyEvent.KEYCODE_MEDIA_STOP -> {
                        ret.put("message", "music-controls-stop")
                        musicControls.controlsNotification(ret)
                    }

                    KeyEvent.KEYCODE_MEDIA_FAST_FORWARD -> {
                        ret.put("message", "music-controls-fast-forward")
                        musicControls.controlsNotification(ret)
                    }

                    KeyEvent.KEYCODE_MEDIA_REWIND -> {
                        ret.put("message", "music-controls-rewind")
                        musicControls.controlsNotification(ret)
                    }

                    KeyEvent.KEYCODE_MEDIA_SKIP_BACKWARD -> {
                        ret.put("message", "music-controls-skip-backward")
                        musicControls.controlsNotification(ret)
                    }

                    KeyEvent.KEYCODE_MEDIA_SKIP_FORWARD -> {
                        ret.put("message", "music-controls-skip-forward")
                        musicControls.controlsNotification(ret)
                    }

                    KeyEvent.KEYCODE_MEDIA_STEP_BACKWARD -> {
                        ret.put("message", "music-controls-step-backward")
                        musicControls.controlsNotification(ret)
                    }

                    KeyEvent.KEYCODE_MEDIA_STEP_FORWARD -> {
                        ret.put("message", "music-controls-step-forward")
                        musicControls.controlsNotification(ret)
                    }

                    KeyEvent.KEYCODE_META_LEFT -> {
                        ret.put("message", "music-controls-meta-left")
                        musicControls.controlsNotification(ret)
                    }

                    KeyEvent.KEYCODE_META_RIGHT -> {
                        ret.put("message", "music-controls-meta-right")
                        musicControls.controlsNotification(ret)
                    }

                    KeyEvent.KEYCODE_MUSIC -> {
                        ret.put("message", "music-controls-music")
                        musicControls.controlsNotification(ret)
                    }

                    KeyEvent.KEYCODE_VOLUME_UP -> {
                        ret.put("message", "music-controls-volume-up")
                        musicControls.controlsNotification(ret)
                    }

                    KeyEvent.KEYCODE_VOLUME_DOWN -> {
                        ret.put("message", "music-controls-volume-down")
                        musicControls.controlsNotification(ret)
                    }

                    KeyEvent.KEYCODE_VOLUME_MUTE -> {
                        ret.put("message", "music-controls-volume-mute")
                        musicControls.controlsNotification(ret)
                    }

                    KeyEvent.KEYCODE_HEADSETHOOK -> {
                        ret.put("message", "music-controls-headset-hook")
                        musicControls.controlsNotification(ret)
                    }

                    else -> {
                        ret.put("message", message)
                        musicControls.controlsNotification(ret)
                    }
                }
            }
        } else if (message == "music-controls-destroy") {
            // Close Button
            ret.put("message", "music-controls-destroy")
            musicControls.controlsNotification(ret)
            musicControls.destroyPlayerNotification()
        } else {
            ret.put("message", message)
            musicControls.controlsNotification(ret)
        }
    }

    companion object {
        private const val TAG = "CMCBroadRcvr"
    }
}
