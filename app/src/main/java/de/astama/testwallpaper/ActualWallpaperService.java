package de.astama.testwallpaper;

import android.opengl.GLES31;
import android.opengl.GLSurfaceView;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class ActualWallpaperService extends OpenGLES2WallpaperService {
    @Override
    GLSurfaceView.Renderer getNewRenderer() {
        return new GLRenderer();
    }
    static class GLRenderer implements GLSurfaceView.Renderer {

        private long time;

        private Square square;

        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            time = System.currentTimeMillis();
            square = new Square();
        }

        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height) {
            GLES31.glViewport(0,0,width,height);
        }

        @Override
        public void onDrawFrame(GL10 gl) {
            long dtime = (System.currentTimeMillis() - time);
            square.draw(dtime);
        }
        public static int loadShader(int type, String shaderCode){

            // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
            // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
            int shader = GLES31.glCreateShader(type);

            // add the source code to the shader and compile it
            GLES31.glShaderSource(shader, shaderCode);
            GLES31.glCompileShader(shader);

            return shader;
        }
    }
    public static class Square {

        private FloatBuffer vertexBuffer;

        private final String vertexShaderCode =
                "attribute vec4 vPosition;" +
                        "void main() {" +
                        "  gl_Position = vPosition;" +
                        "}";
        private final String fragmentShaderCode =
                "precision mediump float;" +
                        "uniform vec4 vColor;" +
                        "void main() {" +
                        "  gl_FragColor = vColor;" +
                        "}";
        private int mProgram;
        // number of coordinates per vertex in this array
        static final int COORDS_PER_VERTEX = 3;
        static float triangleCoords[] = {   // in counterclockwise order:
                -1.0f,  1.0f, 0.0f,   // top left
                -1.0f, -1.0f, 0.0f,   // bottom left
                1.0f, -1.0f, 0.0f,   // bottom right
                1.0f, -1.0f, 0.0f,   // bottom right
                1.0f,  1.0f, 0.0f,  // top right
                -1.0f,  1.0f, 0.0f, // top left
        };

        public Square() {
            // initialize vertex byte buffer for shape coordinates
            ByteBuffer bb = ByteBuffer.allocateDirect(
                    // (number of coordinate values * 4 bytes per float)
                    triangleCoords.length * 4);
            // use the device hardware's native byte order
            bb.order(ByteOrder.nativeOrder());

            // create a floating point buffer from the ByteBuffer
            vertexBuffer = bb.asFloatBuffer();
            // add the coordinates to the FloatBuffer
            vertexBuffer.put(triangleCoords);
            // set the buffer to read the first coordinate
            vertexBuffer.position(0);


            int vertexShader = GLRenderer.loadShader(GLES31.GL_VERTEX_SHADER, vertexShaderCode);
            int fragmentShader = GLRenderer.loadShader(GLES31.GL_FRAGMENT_SHADER, fragmentShaderCode);
            mProgram = GLES31.glCreateProgram();
            GLES31.glAttachShader(mProgram, vertexShader);
            GLES31.glAttachShader(mProgram, fragmentShader);
            GLES31.glLinkProgram(mProgram);
        }
        private int positionHandle;
        private int colorHandle;

        private final int vertexCount = triangleCoords.length / COORDS_PER_VERTEX;
        private final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex

        public void draw(long time){
            double dtime = ((double) time) / 1000.0;
            // Add program to OpenGL ES environment
            GLES31.glUseProgram(mProgram);

            // get handle to vertex shader's vPosition member
            positionHandle = GLES31.glGetAttribLocation(mProgram, "vPosition");

            // Enable a handle to the triangle vertices
            GLES31.glEnableVertexAttribArray(positionHandle);

            // Prepare the triangle coordinate data
            GLES31.glVertexAttribPointer(positionHandle, COORDS_PER_VERTEX,
                    GLES31.GL_FLOAT, false,
                    vertexStride, vertexBuffer);
            // get handle to fragment shader's vColor member
            colorHandle = GLES31.glGetUniformLocation(mProgram, "vColor");

            // Set color for drawing the triangle
            GLES31.glUniform4fv(colorHandle, 1, new float[]{(float)(Math.sin(dtime)), (float)(Math.sin(dtime + 2.0)), (float)(Math.sin(dtime + 4.0)), 1.0f}, 0);

            // Draw the triangle
            GLES31.glDrawArrays(GLES31.GL_TRIANGLES, 0, vertexCount);

            // Disable vertex array
            GLES31.glDisableVertexAttribArray(positionHandle);
        }
    }
}
