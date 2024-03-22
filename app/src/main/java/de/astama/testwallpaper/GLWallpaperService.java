package de.astama.testwallpaper;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.service.wallpaper.WallpaperService;
import android.view.SurfaceHolder;

public class GLWallpaperService extends WallpaperService {
    @Override
    public WallpaperService.Engine onCreateEngine() {
        return new MyWallpaperEngine();
    }
    class MyWallpaperEngine extends WallpaperService.Engine {
        private WallpaperGLSurfaceView glSurfaceView;
        private boolean rendererHasBeenSet;
        public MyWallpaperEngine() {
        }

        @Override
        public void onCreate(SurfaceHolder surfaceHolder) {
            super.onCreate(surfaceHolder);
            glSurfaceView = new WallpaperGLSurfaceView(GLWallpaperService.this);
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);
            if (rendererHasBeenSet) {
                if (visible) {
                    glSurfaceView.onResume();
                } else {
                    glSurfaceView.onPause();
                }
            }
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            glSurfaceView.onDestroy();
        }
        protected void setRenderer(GLSurfaceView.Renderer renderer){
            glSurfaceView.setRenderer(renderer);
            rendererHasBeenSet = true;
        }
        protected void setEGLContextClientVersion(int version){
            glSurfaceView.setEGLContextClientVersion(version);
        }
        protected void setPreserveEGLContextOnPause(boolean preserve){
            glSurfaceView.setPreserveEGLContextOnPause(preserve);
        }
        /*
        private void draw() {
            if (visible) {
                Canvas canvas = holder.lockCanvas();
                canvas.save();
                int time = (int)(System.currentTimeMillis());
                canvas.drawARGB(255, (int)(Math.sin(time) * 255.0),(int)(Math.sin(time + 2) * 255.0),(int)(Math.sin(time + 4) * 255.0));
                canvas.restore();
                holder.unlockCanvasAndPost(canvas);
                handler.removeCallbacks(this::draw);
                handler.postDelayed(this::draw, frameDuration);
            }
        }
        */

        private class WallpaperGLSurfaceView extends GLSurfaceView{
            public WallpaperGLSurfaceView(Context context) {
                super(context);
            }
            @Override
            public SurfaceHolder getHolder(){
                return getSurfaceHolder();
            }
            public void onDestroy(){
                super.onDetachedFromWindow();
            }
        }
    }
}
