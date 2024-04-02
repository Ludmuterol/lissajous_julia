package de.astama.testwallpaper;

import android.opengl.GLES31;
import android.opengl.GLES32;
import android.opengl.GLSurfaceView;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Arrays;
import java.util.Random;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL;
import javax.microedition.khronos.opengles.GL10;

public class ActualWallpaperService extends OpenGLES2WallpaperService {
    @Override
    GLSurfaceView.Renderer getNewRenderer() {
        return new GLRenderer();
    }
    class GLRenderer implements GLSurfaceView.Renderer {

        private long time;

        private Square square;

        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            time = System.currentTimeMillis();
            square = new Square(this);
        }

        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height) {
            GLES31.glViewport(0,0,width,height);
            square.width = width;
            square.height = height;
        }

        @Override
        public void onDrawFrame(GL10 gl) {
            long dtime = (System.currentTimeMillis() - time);
            square.draw(dtime);
        }
        public int loadShader(int type, String shaderCode){

            // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
            // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
            int shader = GLES31.glCreateShader(type);

            // add the source code to the shader and compile it
            GLES31.glShaderSource(shader, shaderCode);
            GLES31.glCompileShader(shader);
            Log.d("TAG", GLES31.glGetShaderInfoLog(shader));

            return shader;
        }
        public String loadStringFromAssetFile(String filePath){
            StringBuilder shaderSource = new StringBuilder();
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(getAssets().open(filePath)));
                String line;
                while((line = reader.readLine()) != null){
                    shaderSource.append(line).append("\n");
                }
                reader.close();
                return shaderSource.toString();
            } catch (IOException e) {
                e.printStackTrace();
                Log.e("TAG", "Could not load shader file");
                return null;
            }
        }
    }
    public static class Square {
        public int MAX_DEPTH = 250;
        public int width = 1080;
        public int height = 2408;
        private FloatBuffer vertexBuffer;

        private final String vertexShaderCode =
                "#version 310 es\n" +
                "in vec4 vPosition;\n" +
                "out vec4 color;\n" +
                "void main() {" +
                "  color = vec4(1.0,1.0,1.0,1.0);" +
                "  gl_Position = vPosition;" +
                "}";
        private int mProgram;
        private int gProgram;
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
        private int fragmentBuffer;
        private int[] tmp_data = new int[width * height + 1];
        private IntBuffer data;

        int GmDepthHandle;
        int GresHandle;
        int GxHandle;
        int GyHandle;
        //int GtimeHandle2;
        int MresHandle;
        int MmDepthHandle;
        int GxMinHandle;
        int GxMaxHandle;
        int GyMinHandle;
        int GyMaxHandle;
        int MpositionHandle;
        int MtimeHandle;
        long kgV(int m, int n){
            if(m == 0 | n == 0)
            {
                return 0;
            } else
            {
                if(m > n)
                {
                    int tmp = n;
                    n = m;
                    m = tmp;
                }
                long mCounter = m;
                while(mCounter <= (long)m * (long)n){
                    if(mCounter % n == 0){
                        return mCounter;
                    }
                    mCounter += m;
                }
                return -1;
            }
        }
        public Square(GLRenderer renderer) {
            String code1 = renderer.loadStringFromAssetFile("fragment.glsl");
            String code2 = renderer.loadStringFromAssetFile("compute.glsl");
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

            int vertexShader = renderer.loadShader(GLES31.GL_VERTEX_SHADER, vertexShaderCode);
            int fragmentShader = renderer.loadShader(GLES31.GL_FRAGMENT_SHADER, code1);
            mProgram = GLES31.glCreateProgram();
            GLES31.glAttachShader(mProgram, vertexShader);
            GLES31.glAttachShader(mProgram, fragmentShader);
            GLES31.glLinkProgram(mProgram);
            GLES31.glUseProgram(mProgram);

            {
                IntBuffer tmp = IntBuffer.allocate(1);
                GLES31.glGenBuffers(1, tmp);
                fragmentBuffer = tmp.get(0);
                //GLES31.glBindBufferBase(GLES31.GL_SHADER_STORAGE_BUFFER, 3, fragmentBuffer);
                GLES31.glBindBufferBase(GLES31.GL_SHADER_STORAGE_BUFFER, 0, fragmentBuffer);
                Arrays.fill(tmp_data, 0);
                data = IntBuffer.wrap(tmp_data);
                GLES31.glBufferData(GLES31.GL_SHADER_STORAGE_BUFFER, (width * height + 1) * 4, null, GLES31.GL_STREAM_DRAW);
                MresHandle = GLES31.glGetUniformLocation(mProgram, "resolution");
                MmDepthHandle = GLES31.glGetUniformLocation(mProgram, "MAX_DEPTH");
                MpositionHandle = GLES31.glGetAttribLocation(mProgram, "vPosition");
                MtimeHandle = GLES31.glGetUniformLocation(mProgram, "time");

            }
            int computeShader = renderer.loadShader(GLES31.GL_COMPUTE_SHADER, code2);
            gProgram = GLES31.glCreateProgram();
            GLES31.glAttachShader(gProgram, computeShader);
            GLES31.glLinkProgram(gProgram);
            GLES31.glUseProgram(gProgram);

            {
                GxHandle = GLES31.glGetUniformLocation(gProgram, "x");
                GyHandle = GLES31.glGetUniformLocation(gProgram, "y");
                GresHandle = GLES31.glGetUniformLocation(gProgram, "resolution");
                GmDepthHandle = GLES31.glGetUniformLocation(gProgram,"MAX_DEPTH");
                GxMinHandle = GLES31.glGetUniformLocation(gProgram, "xMin");
                GxMaxHandle = GLES31.glGetUniformLocation(gProgram, "xMax");
                GyMinHandle = GLES31.glGetUniformLocation(gProgram, "yMin");
                GyMaxHandle = GLES31.glGetUniformLocation(gProgram, "yMax");
                //GtimeHandle2 = GLES31.glGetUniformLocation(gProgram, "time");

            }
            Random r = new Random(17);
            int res = 1;
            do {
                x_a = r.nextDouble() * 25.0 + 25.0;
                x_c = r.nextDouble() * 25.0 + 25.0;
                y_a = r.nextDouble() * 25.0 + 25.0;
                y_c = r.nextDouble() * 25.0 + 25.0;
                int tmp1 = (int)(x_a * 100000);
                int tmp2 = (int)(x_c * 100000);
                int tmp3 = (int)(y_a * 100000);
                int tmp4 = (int)(y_c * 100000);
                int tmp5 = (int)(kgV(tmp1, tmp2) / 10000000000L);
                int tmp6 = (int)(kgV(tmp3, tmp4) / 10000000000L);
                res = (int)kgV(tmp5, tmp6);
            } while (res < 2000000);
        }

        private final int vertexCount = triangleCoords.length / COORDS_PER_VERTEX;
        private final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex

        private double frameTime = 0.0;
        private double compTime = 0.0;
        private double fragTime = 0.0;
        private long frames = 0;
        private float x = -0.55f;
        private float y = 0.5f;
        float STEP = 1f;
//        float t = STEP;
        double x_a;
        double x_c;
        double y_a;
        double y_c;
        public static double mapRange(double s, double a1, double a2, double b1, double b2){
            return b1 + ((s - a1)*(b2 - b1))/(a2 - a1);
        }
        private void nextStep(double dtime) {
            x = (float) mapRange(Math.sin(dtime / x_a) + Math.sin(dtime / x_c), -2, 2, -2, 0.47);
            y = (float) mapRange(Math.cos(dtime / y_a) + Math.cos(dtime / y_c), -2, 2, -1.12, 1.12);
        }
        public void draw(long time) {
            long startTime = System.nanoTime();
            double dTime = ((double) time) / 1000.0;
            nextStep(dTime * STEP);
            GLES31.glUseProgram(gProgram);
            GLES31.glUniform2ui(GresHandle, width, height);
            GLES31.glUniform1ui(GmDepthHandle, MAX_DEPTH);
//            GLES31.glUniform1f(GxHandle, -0.55f);
//            GLES31.glUniform1f(GyHandle, 0.5f);
            GLES31.glUniform1f(GxHandle, x);
            GLES31.glUniform1f(GyHandle, y);
            Log.d("TAG", "x: " + x + " y: " + y);
            GLES31.glUniform1f(GxMinHandle, 0.0f - (float)width / 1000.0f);
            GLES31.glUniform1f(GxMaxHandle, 0.0f + (float) width / 1000.0f);
            GLES31.glUniform1f(GyMinHandle, 0.0f + (float) height / 1000.0f);
            GLES31.glUniform1f(GyMaxHandle, 0.0f - (float) height / 1000.0f);
            //GLES31.glUniform1f(GtimeHandle2, (float)dTime);


            GLES31.glBindBuffer(GLES31.GL_SHADER_STORAGE_BUFFER, fragmentBuffer);
            IntBuffer tmp = IntBuffer.allocate(4).put(0);
            tmp.position(0);
            GLES31.glBufferSubData(GLES31.GL_SHADER_STORAGE_BUFFER, 0, 4, tmp);

            //GLES31.glFinish();
            GLES31.glDispatchCompute(width, height, 1);
            GLES31.glMemoryBarrier(GLES31.GL_SHADER_STORAGE_BARRIER_BIT);
            //GLES31.glFinish();

            //WORKS BUT IS INCREDIBLY SLOW (from 1000fps down to 1fps)
//            GLES31.glBindBuffer(GLES31.GL_SHADER_STORAGE_BUFFER, fragmentBuffer);
//            ByteBuffer bufff2 = (ByteBuffer) GLES31.glMapBufferRange(GLES31.GL_SHADER_STORAGE_BUFFER, 0, width * height * 4, GLES31.GL_MAP_READ_BIT);
//            bufff2.order(ByteOrder.nativeOrder());
//            IntBuffer data = bufff2.asIntBuffer();
//            int actual_max_dep = 250;
//            for (int i = 0; i < width * height; i++) {
//                int tmp = data.get();
//                if (tmp > actual_max_dep && tmp != MAX_DEPTH){
//                    actual_max_dep = tmp;
//                }
//            }
//            GLES31.glUnmapBuffer(GLES31.GL_SHADER_STORAGE_BUFFER);

            // Add program to OpenGL ES environment
            long computeTime = System.nanoTime();
            GLES31.glUseProgram(mProgram);

            GLES31.glBindBuffer(GLES31.GL_SHADER_STORAGE_BUFFER, fragmentBuffer);

            GLES31.glUniform2ui(MresHandle,width,height);
            GLES31.glUniform1ui(MmDepthHandle,MAX_DEPTH);
            GLES31.glUniform1f(MtimeHandle, (float)dTime);

            // Enable a handle to the triangle vertices
            GLES31.glEnableVertexAttribArray(MpositionHandle);
            // Prepare the triangle coordinate data
            GLES31.glVertexAttribPointer(MpositionHandle, COORDS_PER_VERTEX,
                    GLES31.GL_FLOAT, false,
                    vertexStride, vertexBuffer);

            //GLES31.glFinish();
            // Draw the triangles
            GLES31.glDrawArrays(GLES31.GL_TRIANGLES, 0, vertexCount);
            //GLES31.glFinish();

            // Disable vertex array
            GLES31.glDisableVertexAttribArray(MpositionHandle);
            long endTime = System.nanoTime();

            frameTime = ((endTime - startTime) + frames * frameTime) / (double)(frames + 1);
            compTime = ((computeTime - startTime) + frames * compTime) / (double)(frames + 1);
            fragTime = ((endTime - computeTime) + frames * fragTime) / (double)(frames + 1);

            frames++;

                Log.d("TAG", "FrameTime: " + frameTime + "ns");
                Log.d("TAG", "Compute: " + compTime + "ns");
                Log.d("TAG", "Fragment: " + fragTime + "ns");
                Log.d("TAG", "FPS: " + (int) (1000000000.0 / frameTime) + " Frames: " + frames);
                Log.d("TAG", "--------------------------------------------------");

        }

    }
}
