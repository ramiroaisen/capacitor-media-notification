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

export type EventKind = 
  | "music-controls-play"
  | "music-controls-pause"
  | "music-controls-next"
  | "music-controls-previous"
  | "music-controls-toggle-play-pause"
  | "music-controls-stop"
  | "music-controls-skip-forward"
  | "music-controls-skip-backward"
  | `music-controls-media-button-uknown-${string}`

  // what is this
  | "music-controls-stop-listening"

  | "music-controls-headset-unplugged"
  | "music-controls-headset-plugged"

  | "music-controls-fast-forward"
  | "music-controls-fast-rewind"
  | "music-controls-step-backward"
  | "music-controls-step-forward"

  | "music-controls-meta-left"
  | "music-controls-meta-right"

  | "music-controls-music"
  | "music-controls-volume-up"
  | "music-controls-volume-down"
  | "music-controls-volume-mute"

  | "music-controls-headset-hook"
  | "music-controls-destroy"

export interface CapacitorMusicControlsPlugin {
    /**
     * Create the media controls
     * @param options {MusicControlsOptions}
     * @returns {Promise<any>}
     */
    create(options: CapacitorMusicControlsInfo): Promise<any>;
    /**
     * Destroy the media controller
     * @returns {Promise<any>}
     */
    destroy(): Promise<any>;
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
    updateDismissable(dismissable: boolean): void;
    addListener(event: "event", callback: ({ kind }: { kind: EventKind }) => void): Promise<PluginListenerHandle>;
}
