package com.ingageco.capacitormusiccontrols

import android.app.Notification
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log

class MusicControlsNotificationKiller : Service() {
    private var mNM: NotificationManager? = null
    private val mBinder: IBinder = KillBinder(this)

    override fun onBind(intent: Intent): IBinder {
        NOTIFICATION_ID = intent.getIntExtra("notificationID", 1)
        return mBinder
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.i(TAG, "onStartCommand")

        return START_STICKY
    }

    override fun onCreate() {
        Log.i(TAG, "onCreate")

        // this.startForeground();
        mNM = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        mNM?.cancel(NOTIFICATION_ID)
    }

    override fun onDestroy() {
        Log.i(TAG, "onDestroy")

        mNM = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        mNM?.cancel(NOTIFICATION_ID)
    }

    override fun onTaskRemoved(intent: Intent) {
        Log.i(TAG, "onTaskRemoved")

        mNM = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        mNM?.cancel(NOTIFICATION_ID)
    }

    fun setForeground(notification: Notification?) {
        Log.i(TAG, "setForeground")

        this.startForeground(NOTIFICATION_ID, notification)
    }

    fun clearForeground() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            return
        }

        Log.i(TAG, "clearForeground")

        this.stopForeground(STOP_FOREGROUND_DETACH)
    }

    companion object {
        private const val TAG = "MusContNotifKiller"

        private var NOTIFICATION_ID = 0
    }
}
