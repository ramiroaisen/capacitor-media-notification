package com.ingageco.capacitormusiccontrols

import com.getcapacitor.JSObject

class MusicControlsInfos(args: JSObject) {
	var artist: String?
    var album: String?
	var track: String?
	var ticker: String?
	var cover: String?
	var isPlaying: Boolean?
	var hasPrev: Boolean?
	var hasNext: Boolean?
	var hasClose: Boolean?
	var dismissable: Boolean?
	var playIcon: String?
	var pauseIcon: String?
	var prevIcon: String?
	var nextIcon: String?
	var closeIcon: String?
	var notificationIcon: String?

    init {
        val params: JSObject = args

        this.track = params.getString("track")
        this.artist = params.getString("artist")
        this.album = params.getString("album")
        this.ticker = params.getString("ticker")
        this.cover = params.getString("cover")
        this.isPlaying = params.getBoolean("isPlaying")
        this.hasPrev = params.getBoolean("hasPrev")
        this.hasNext = params.getBoolean("hasNext")
        this.hasClose = params.getBoolean("hasClose")
        this.dismissable = params.getBoolean("dismissable")
        this.playIcon = params.getString("playIcon")
        this.pauseIcon = params.getString("pauseIcon")
        this.prevIcon = params.getString("prevIcon")
        this.nextIcon = params.getString("nextIcon")
        this.closeIcon = params.getString("closeIcon")
        this.notificationIcon = params.getString("notificationIcon")
    }
}
