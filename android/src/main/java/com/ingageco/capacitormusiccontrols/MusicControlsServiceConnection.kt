package com.ingageco.capacitormusiccontrols

import android.app.Activity
import android.app.Notification
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.IBinder
import android.util.Log
import com.ingageco.capacitormusiccontrols.MusicControlsNotificationKiller

class MusicControlsServiceConnection internal constructor(protected var activity: Activity) :
    ServiceConnection {
    protected var service: MusicControlsNotificationKiller? = null

    override fun onServiceConnected(className: ComponentName, binder: IBinder) {
        Log.i(TAG, "onServiceConnected")

        this.service = (binder as KillBinder).service

        // this.service.startService(new Intent(activity, MusicControlsNotificationKiller.class));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            service!!.startForegroundService(
                Intent(
                    activity,
                    MusicControlsNotificationKiller::class.java
                )
            )
        } else {
            service!!.startService(Intent(activity, MusicControlsNotificationKiller::class.java))
        }
    }

    override fun onServiceDisconnected(className: ComponentName) {
        Log.i(TAG, "onServiceDisconnected")
    }

    fun setNotification(notification: Notification?, isPlaying: Boolean) {
        if (this.service == null) {
            return
        }

        Log.i(TAG, "setNotification")

        if (isPlaying) {
            service!!.setForeground(notification)
        } else {
            service!!.clearForeground()
        }
    }

    companion object {
        private const val TAG = "MusContServConn"
    }
}