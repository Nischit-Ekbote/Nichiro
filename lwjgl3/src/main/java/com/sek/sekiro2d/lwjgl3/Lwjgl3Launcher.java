package com.sek.sekiro2d.lwjgl3;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.Graphics;
import com.sek.sekiro2d.SekiroGame;

/** Launches the desktop (LWJGL3) application. */
public class Lwjgl3Launcher {
    public static void main(String[] args) {
        createApplication();
    }

    private static Lwjgl3Application createApplication() {
        return new Lwjgl3Application(new SekiroGame(), getDefaultConfiguration());
    }

    private static Lwjgl3ApplicationConfiguration getDefaultConfiguration() {
        Lwjgl3ApplicationConfiguration configuration = new Lwjgl3ApplicationConfiguration();
        configuration.setTitle("Sekiro2D");

        // VSync helps to eliminate screen tearing by limiting FPS to monitor refresh rate.
        configuration.useVsync(true);

        // Set the refresh rate to the monitor's current refresh rate.
        Graphics.DisplayMode displayMode = Lwjgl3ApplicationConfiguration.getDisplayMode();
        configuration.setForegroundFPS(displayMode.refreshRate);

        // Set window dimensions and fullscreen mode toggle
        configuration.setWindowedMode(800, 500);
        // Uncomment the following line to enable fullscreen.
//         configuration.setFullscreenMode(displayMode);

        // Specify window icons for different sizes
        configuration.setWindowIcon("libgdx128.png", "libgdx64.png", "libgdx32.png", "libgdx16.png");
        return configuration;
    }
}
