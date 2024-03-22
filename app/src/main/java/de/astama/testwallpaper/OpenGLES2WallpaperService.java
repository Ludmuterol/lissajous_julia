package de.astama.testwallpaper;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.opengl.GLSurfaceView;
import android.view.SurfaceHolder;

abstract class OpenGLES2WallpaperService extends GLWallpaperService {
    @Override
    public Engine onCreateEngine(){
        return new OpenGLES3Engine();
    }
    class OpenGLES3Engine extends GLWallpaperService.MyWallpaperEngine{
        @Override
        public void onCreate(SurfaceHolder surfaceHolder) {
            super.onCreate(surfaceHolder);

            // Check if the system supports OpenGL ES 2.0.
            final ActivityManager activityManager =
                    (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
            final ConfigurationInfo configurationInfo =
                    activityManager.getDeviceConfigurationInfo();
            final boolean supportsEs2 =
                    configurationInfo.reqGlEsVersion >= 0x30001;

            if (supportsEs2)
            {
                // Request an OpenGL ES 3.0 compatible context.
                setEGLContextClientVersion(3);

                // On Honeycomb+ devices, this improves the performance when
                // leaving and resuming the live wallpaper.
                setPreserveEGLContextOnPause(true);

                // Set the renderer to our user-defined renderer.
                setRenderer(getNewRenderer());
            }
            else
            {
                // This is where you could create an OpenGL ES 1.x compatible
                // renderer if you wanted to support both ES 1 and ES 2.
                return;
            }
        }
    }
    abstract GLSurfaceView.Renderer getNewRenderer();
}
