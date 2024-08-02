import { PluginListenerHandle } from "@capacitor/core";

export interface CapacitorMusicControlsInfo {
    track?: string;
    artist?: string;
    cover?: string;
    isPlaying?: boolean;
    dismissable?: boolean;
    hasPrev?: boolean;
    hasNext?: boolean;
    hasSkipForward?: boolean;
    hasSkipBackward?: boolean;
    skipForwardInterval?: number;
    skipBackwardInterval?: number;
    hasScrubbing?: boolean;
    hasClose?: boolean;
    album?: string;
    duration?: number;
    elapsed?: number;
    ticker?: string;
    playIcon?: string;
    pauseIcon?: string;
    prevIcon?: string;
    nextIcon?: string;
    closeIcon?: string;
    notificationIcon?: string;
}

export interface CapacitorMusicControlsPlugin {
    /**
     * Create the media controls
     * @param options {MusicControlsOptions}
     * @returns {Promise<void>}
     */
    create(options: CapacitorMusicControlsInfo): Promise<void>;

    /**
     * Destroy the media controller
     * @returns {Promise<void>}
     */
    destroy(): Promise<void>;

    /**
     * Toggle play/pause:
     * @param isPlaying {Object}
     */
    updateIsPlaying(args: {
        isPlaying: boolean;
    }): void;

    /**
     * Update elapsed time, optionally toggle play/pause:
     * @param args {Object}
     */
    updateElapsed(args: {
        elapsed: number;
        isPlaying: boolean;
    }): void;

    /**
     * Toggle dismissable:
     * @param dismissable {boolean}
     */
    updateDismissable({ dismissable } : { dismissable: boolean }): void;

    addListener(event: string, callback: (info: any) => void): Promise<PluginListenerHandle>
}