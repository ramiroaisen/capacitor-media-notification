import { WebPlugin } from '@capacitor/core';
import { CapacitorMusicControlsPlugin } from ".";

export class CapacitorMusicControlsWeb extends WebPlugin implements CapacitorMusicControlsPlugin {
    constructor() {
        super({
            name: 'CapacitorMusicControls',
            platforms: ['web'],
        });
    }

    async create(): Promise<void> {}
    async destroy(): Promise<void> {}
    async updateDismissable(): Promise<void> {}
    async updateElapsed(): Promise<void> {}
    async updateIsPlaying(): Promise<void> {}

}