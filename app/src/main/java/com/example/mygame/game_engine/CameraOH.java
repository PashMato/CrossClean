package com.example.mygame.game_engine;

import android.content.Context;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import com.example.mygame.R;
import com.example.mygame.game_engine.Math.Vectors;
import com.example.mygame.game_engine.shaders.ShaderUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class CameraOH extends GameObject implements GLSurfaceView.Renderer {
    public static float[] cameraPos;
    long last_time; // The last frame time (for dt calculation)


    // RT (S) Matrices
    float[] viewMatrix = new float[16]; // Matrix to reverse the camera Position & Rotation
    float[] projectionMatrix = new float[16]; // Matrix to get a Vertex Position on the screen

    float[] mvpMatrix = new float[16]; // The final RT (S) Matrix (changes per object)

    int program; // OpenGL program id

    float nearDis = 3;
    float farDis = 100;
    float fogDis = 90;
    float[] bg_color = new float[] {0f, .5f, .5f, 1f};
    float[] lightDirection = {2f, 3f, -0.5f}; // Light pointing toward negative Z

    // The shaders code
    private final String vertexShaderCode;
    private final String fragmentShaderCode;
    float width = 0;
    float height = 0;

    public CameraOH(float[] p, float[] v, Context context) {
        super(p, new float[2], null, ObjectTypes.Camera);
        velocity = v;
        rotation[1] = 0;

        last_time = System.nanoTime();

        vertexShaderCode = getShaderCode(context, R.raw.vertex_shader);
        fragmentShaderCode = getShaderCode(context, R.raw.fragment_shader);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES30.glClearColor(bg_color[0], bg_color[1], bg_color[2], bg_color[3]); // color the background

        program = GLES30.glCreateProgram();
        loadShaders();

        // Enable depth testing
        GLES30.glEnable(GLES30.GL_DEPTH_TEST);
        GLES30.glDepthFunc(GLES30.GL_LEQUAL);

        // Draw only the triangles facing the camera
        GLES30.glEnable(GLES30.GL_CULL_FACE); // optional, but good for performance
        GLES30.glCullFace(GLES30.GL_BACK);    // cull back faces
        GLES30.glFrontFace(GLES30.GL_CCW);    // CCW is considered front-facing
    }

    @Override
    public void onSurfaceChanged(javax.microedition.khronos.opengles.GL10 gl, int width, int height) {
        // Runs when the surface is created or his size is changed
        GLES30.glViewport(0, 0, width, height);
        float ratio = (float) width / height;

        this.width = width;
        this.height = height;

        Matrix.frustumM(projectionMatrix, 0, -ratio, ratio, -1, 1, nearDis, farDis);

        loadShaders();
    }

    @Override
    public void onDrawFrame(javax.microedition.khronos.opengles.GL10 gl) {
        // clear the buffers from the last frame
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT | GLES30.GL_DEPTH_BUFFER_BIT);

        // Set camera position
        Vectors.setCameraMatrix(viewMatrix, position, rotation);

        long time = System.nanoTime();
        dt = (time - last_time) / 1_000_000_000.0f;
        last_time = time;

        cameraPos = position.clone();
        update();

        drawAll();
    }

    public void drawAll() {
        GameObject g;

        for (int i = ALLGameObjects.size() - 1; i >= 0; i--)
        { // Deletion changes size
            g = ALLGameObjects.get(i);

            g.update();

            draw(g);
        }
    }
    private void draw(GameObject g) {
        // Combine matrices
        float[] tempMatrix = new float[16];
        Matrix.multiplyMM(tempMatrix, 0, viewMatrix, 0, g.modelMatrix, 0);
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, tempMatrix, 0);

        // Use shader program
        GLES30.glUseProgram(program);

        int positionHandle = GLES30.glGetAttribLocation(program, "aPosition");

        int uvHandle = GLES30.glGetAttribLocation(program, "aTexCoord");
        int uTextureLocation = GLES30.glGetUniformLocation(program, "uTexture");

        int mvpMatrixHandle = GLES30.glGetUniformLocation(program, "uMVPMatrix");
        int rotMatrixHandle = GLES30.glGetUniformLocation(program, "uRotMatrix");

        int normalHandle = GLES30.glGetAttribLocation(program, "aNormal");
        int lightDirHandle = GLES30.glGetUniformLocation(program, "uLightDir");

        int fogColor = GLES30.glGetUniformLocation(program, "fogColor");
        int fogStart = GLES30.glGetUniformLocation(program, "fogStart");
        int fogEnd = GLES30.glGetUniformLocation(program, "fogEnd");

        // Bind texture
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, g.textureId);
        GLES30.glUniform1i(uTextureLocation, 0);

        // Fog
        GLES30.glUniform4f(fogColor, bg_color[0], bg_color[1], bg_color[2], bg_color[3]);
        GLES30.glUniform1f(fogStart, fogDis);
        GLES30.glUniform1f(fogEnd, farDis);

        // Enable and set aPosition
        GLES30.glEnableVertexAttribArray(positionHandle);
        GLES30.glVertexAttribPointer(positionHandle, 3, GLES30.GL_FLOAT, false, 0, g.vertexBuffer);

        GLES30.glEnableVertexAttribArray(normalHandle);
        GLES30.glVertexAttribPointer(normalHandle, 3, GLES30.GL_FLOAT, false, 0, g.normalBuffer);

        // Enable and set aColor
        GLES30.glEnableVertexAttribArray(uvHandle);
        GLES30.glVertexAttribPointer(uvHandle, 2, GLES30.GL_FLOAT, false, 0, g.uvMapBuffer);

        // Set uniforms
        GLES30.glUniform3fv(lightDirHandle, 1, lightDirection, 0);

        GLES30.glUniformMatrix4fv(mvpMatrixHandle, 1, false, mvpMatrix, 0);
        GLES30.glUniformMatrix4fv(rotMatrixHandle, 1, false, g.rotationMatrix, 0);

        // Draw the triangle
        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, g.model3D.Triangles.length / 3);

        // Clean up
        GLES30.glDisableVertexAttribArray(positionHandle);
        GLES30.glDisableVertexAttribArray(uvHandle);
        GLES30.glDisableVertexAttribArray(uTextureLocation);
        GLES30.glDisableVertexAttribArray(mvpMatrixHandle);
        GLES30.glDisableVertexAttribArray(rotMatrixHandle);
        GLES30.glDisableVertexAttribArray(normalHandle);
        GLES30.glDisableVertexAttribArray(lightDirHandle);
    }

    private static String getShaderCode(Context context, int id) {
        try {
            InputStream inputStream = context.getResources().openRawResource(id);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder shaderCode = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                shaderCode.append(line).append('\n');
            }
            reader.close();

            return shaderCode.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void loadShaders() {
        int vertexShader = ShaderUtils.loadShader(GLES30.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShader = ShaderUtils.loadShader(GLES30.GL_FRAGMENT_SHADER, fragmentShaderCode);

        GLES30.glAttachShader(program, vertexShader);
        GLES30.glAttachShader(program, fragmentShader);
        GLES30.glLinkProgram(program);
    }
}
