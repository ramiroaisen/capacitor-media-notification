package com.ingageco.capacitormusiccontrols

import android.os.Binder

class KillBinder(@JvmField val service: MusicControlsNotificationKiller) : Binder()
