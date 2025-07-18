package com.example.mygame.game_engine;

import android.content.Context;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

import com.example.mygame.R;
import com.example.mygame.game_engine.Math.Vectors;
import com.example.mygame.game_engine.shaders.Rect2D;
import com.example.mygame.game_engine.shaders.ShaderUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class CameraOH extends GameObject implements GLSurfaceView.Renderer {
    public static float[] cameraPos;
    private long last_time; // The last frame time (for dt calculation)


    // RT (S) Matrices
    float[] viewMatrix = new float[16]; // Matrix to reverse the camera Position & Rotation
    float[] projectionMatrix = new float[16]; // Matrix to get a Vertex Position on the screen
    float[] camMatrix = new float[16];

    int program; // OpenGL program id

    final float nearDis = 1f;
    final float farDis = 70;
    final float fogDis = 65;
    final float[] bg_color = new float[] {0f, .5f, .5f, 1f};
    final float[] lightDirection = {1.5f, -3f, 3f}; // Light pointing toward negative Z

    // The shaders code
    private final String vertexShaderCode;
    private final String fragmentShaderCode;

    // OpenGL id
    int positionHandle = -1;

    int uvHandle = -1;
    int uTextureLocation = -1;

    int modelMatrixHandler = -1;
    int rotMatrixHandle = -1;
    int camMatrixHandle = -1;

    int normalHandle = -1;
    int lightDirHandle = -1;

    int fogColor = -1;
    int fogStart = -1;

    int uFar = -1;
    int uNear = -1;

    // Screen size
    float width = 0;
    float height = 0;

    int tmp = 0;

    public CameraOH(float[] p, float[] v, Context context, long delay) {
        super(p, new Rect2D(), null, ObjectTypes.Camera);

        isVisible = false; // to give the game time to load before drawing everything
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() { // Activate the camera
                isVisible = true;
            }
        }, delay);

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
        GLES30.glFrontFace(GLES30.GL_CW);    // CCW is considered front-facing
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

        // TODO: del me
        tmp++;
        if(tmp >= 100) {
            Log.d("FPS", "FPS: " + 1 / getDt());
            tmp = 0;
        }

        cameraPos = position.clone();
        onUpdate();

        // draw & update all the GameObjects
        GameObject g;

        // Cam matrix
        Matrix.multiplyMM(camMatrix, 0, projectionMatrix, 0, viewMatrix, 0);
        GLES30.glUniformMatrix4fv(camMatrixHandle, 1, false, camMatrix, 0);

        for (int i = AllGameObjects.size() - 1; i >= 0; i--)
        { // Deletion changes size
            g = AllGameObjects.get(i);

            if (!g.isActive) {
                continue;
            }

            g.onUpdate();

            if (!isVisible || !g.isVisible) {
                continue;
            }

            draw(g);
        }

        handleObjectGroups();
    }

    @Override
    protected void onDelete() {
        super.onDelete();

        clearShaders();
    }
    protected void draw(GameObject g) {
        if (g.vertexBuffer == null || g.normalBuffer == null || g.uvMapBuffer == null)
        { // this object's isn't valid
            return;
        }

        // Bind texture
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, g.textureId);

        // Enable & set aPosition
        GLES30.glVertexAttribPointer(positionHandle, 3, GLES30.GL_FLOAT, false, 0, g.vertexBuffer);

        // Enable & set the normals
        GLES30.glVertexAttribPointer(normalHandle, 3, GLES30.GL_FLOAT, false, 0, g.normalBuffer);

        // Enable & set aColor
        GLES30.glVertexAttribPointer(uvHandle, 2, GLES30.GL_FLOAT, false, 0, g.uvMapBuffer);

        GLES30.glUniformMatrix4fv(modelMatrixHandler, 1, false, g.modelMatrix, 0);
        GLES30.glUniformMatrix4fv(rotMatrixHandle, 1, false, g.rotationScaleMatrix, 0);

        // Draw the triangle
        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, g.model3D.Triangles.length / 3);
    }

    protected void handleObjectGroups() {
        GameObject g;
        for (List<GameObject> group : objectGroups) {

            if (group != null && group.isEmpty()) {
                continue;
            }

            g = group.get(0);

            if (g == null || g.vertexBuffer == null || g.normalBuffer == null || g.uvMapBuffer == null) { // this object's isn't valid
                continue;
            }

            /// ------> IMPORTANT DISCLAIMER <------
            ///  the next chuck of code isn't affected by is active because the first object
            ///  isn't can be unactive but wee still need to draw the rest of the array
            // Bind texture
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, g.textureId);
            GLES30.glUniform1i(uTextureLocation, 0); // Ensure correct sampler

            // Enable & set aPosition
            GLES30.glVertexAttribPointer(positionHandle, 3, GLES30.GL_FLOAT, false, 0, g.vertexBuffer);

            // Enable & set the normals
            GLES30.glVertexAttribPointer(normalHandle, 3, GLES30.GL_FLOAT, false, 0, g.normalBuffer);

            // Enable & set aColor
            GLES30.glVertexAttribPointer(uvHandle, 2, GLES30.GL_FLOAT, false, 0, g.uvMapBuffer);

            for (int j = 0; j < group.size(); j++) {
                g = group.get(j);

                if (!g.isActive) {
                    continue;
                }

                g.onUpdate();

                if (!isVisible) { // draw only if the camera is active
                    continue;
                }

                drawInObjectGroup(g.modelMatrix, g.rotationScaleMatrix, g.textureId, g.model3D.Triangles.length / 3);
            }
        }
    }
    protected void drawInObjectGroup(float[] modelMatrixGO, float[] rotationScaleMatrixGO, int textureIdGO, int length) {
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureIdGO); // sadly we have to rebind the texture every frame because elsewhere OpenGl rewrites it
        GLES30.glUniformMatrix4fv(modelMatrixHandler, 1, false, modelMatrixGO, 0);
        GLES30.glUniformMatrix4fv(rotMatrixHandle, 1, false, rotationScaleMatrixGO, 0);

        // Draw the triangle
        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, length);
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
        clearShaders();

        // Use shader program
        GLES30.glUseProgram(program);

        int vertexShader = ShaderUtils.loadShader(GLES30.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShader = ShaderUtils.loadShader(GLES30.GL_FRAGMENT_SHADER, fragmentShaderCode);

        GLES30.glAttachShader(program, vertexShader);
        GLES30.glAttachShader(program, fragmentShader);
        GLES30.glLinkProgram(program);

        positionHandle = GLES30.glGetAttribLocation(program, "aPosition");

        uvHandle = GLES30.glGetAttribLocation(program, "aTexCoord");
        uTextureLocation = GLES30.glGetUniformLocation(program, "uTexture");

        modelMatrixHandler = GLES30.glGetUniformLocation(program, "uModelMatrix");
        rotMatrixHandle = GLES30.glGetUniformLocation(program, "uRotMatrix");
        camMatrixHandle = GLES30.glGetUniformLocation(program, "uCamMatrix");

        normalHandle = GLES30.glGetAttribLocation(program, "aNormal");
        lightDirHandle = GLES30.glGetUniformLocation(program, "uLightDir");

        fogColor = GLES30.glGetUniformLocation(program, "fogColor");
        fogStart = GLES30.glGetUniformLocation(program, "fogStart");

        uFar = GLES30.glGetUniformLocation(program, "uFar");
        uNear = GLES30.glGetUniformLocation(program, "uNear");


        GLES30.glEnableVertexAttribArray(positionHandle);
        GLES30.glEnableVertexAttribArray(normalHandle);
        GLES30.glEnableVertexAttribArray(uvHandle);

        // Textures
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
        GLES30.glUniform1i(uTextureLocation, 0);

        // Fog
        GLES30.glUniform4f(fogColor, bg_color[0], bg_color[1], bg_color[2], bg_color[3]);
        GLES30.glUniform1f(fogStart, fogDis);
        GLES30.glUniform1f(uFar, farDis);
        GLES30.glUniform1f(uNear, nearDis);

        // Lighting
        GLES30.glUniform3fv(lightDirHandle, 1, lightDirection, 0);
    }

    private void clearShaders() {
        // Clean up
        GLES30.glDisableVertexAttribArray(positionHandle);
        GLES30.glDisableVertexAttribArray(normalHandle);
        GLES30.glDisableVertexAttribArray(uvHandle);

        GLES30.glDisableVertexAttribArray(uTextureLocation);
        GLES30.glDisableVertexAttribArray(modelMatrixHandler);
        GLES30.glDisableVertexAttribArray(rotMatrixHandle);
        GLES30.glDisableVertexAttribArray(lightDirHandle);

        positionHandle = -1;
        normalHandle = -1;
        uvHandle = -1;

        uTextureLocation = -1;
        modelMatrixHandler = -1;
        rotMatrixHandle = -1;


        lightDirHandle = -1;

        fogColor = -1;
        fogStart = -1;

        uFar = -1;
        uNear = -1;
    }
}
