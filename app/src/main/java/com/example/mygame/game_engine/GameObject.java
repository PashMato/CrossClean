package com.example.mygame.game_engine;

import static android.opengl.Matrix.scaleM;
import static android.opengl.Matrix.translateM;

import android.opengl.GLES30;
import android.opengl.GLUtils;

import com.example.mygame.game_engine.Math.Vectors;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

public class GameObject {
    static List<GameObject> ALLGameObjects = new ArrayList<>();
    static float dt = 0; // standard gameEngine dt

    boolean didCallOnCreate = false;

    // Position, Rotation & Scale
    public float[] position;
    public float[] rotation;
    public float[] scale;

    // Velocities for Position & Rotation
    public float[] velocity;
    public float[] angularVelocity;

    // Collisions
    Model3D model3D;

    ObjectTypes objType = ObjectTypes.Null;
    float[] rectSize;

    // Buffers from the 3D model
    protected FloatBuffer vertexBuffer;
    protected FloatBuffer normalBuffer;
    protected FloatBuffer uvMapBuffer;

    protected int textureId;

    // RT (S) Matrices
    protected float[] modelMatrix = new float[16];
    protected float[] rotationMatrix = new float[16]; // R (S) - Rotation & Scale Matrix for the Normals

    private List<UpdateFunction> updateFunctions = new ArrayList<>();

    public GameObject(float[] pos, float[] rectSize, Model3D m, ObjectTypes objType) {
        this(pos, rectSize, m);

        this.objType = objType;
        switch (this.objType) {
            case DynamicGameObjects:
            case RelativeToCameraGameObjects:
            case Background:
                ALLGameObjects.add(this);
                break;
            case Camera:
            case Null:
            default:
                break;
        }
    }

    private GameObject(float[] pos, float[] rectSize, Model3D m) {
        position = pos;
        rotation = new float[3];
        scale = new float[] {1f, 1f, 1f};

        velocity = new float[3];
        angularVelocity = new float[3];

        this.rectSize = rectSize;
        model3D = m;
        if (model3D == null) {
            model3D = Model3D.createEmpty();
        }

        // Prepare and Load the vertex buffer
        if (m != null) {
            vertexBuffer = createDirectNativeOrderFloatBuffer(model3D.Triangles);
            normalBuffer = createDirectNativeOrderFloatBuffer(model3D.Normals);
            uvMapBuffer = createDirectNativeOrderFloatBuffer(model3D.UVMap);

            textureId = 0; // set for null
        } else {
            vertexBuffer = null;
            uvMapBuffer = null;
        }
    }

    public boolean checkCollision(GameObject g) {
        float[] dPos = g.position.clone();
        Vectors.Subtract(dPos, position);
        if (g.objType == ObjectTypes.RelativeToCameraGameObjects) {
            Vectors.Add(dPos, CameraOH.cameraPos);
        }

        if (objType == ObjectTypes.RelativeToCameraGameObjects) {
            Vectors.Subtract(dPos, CameraOH.cameraPos);
        }
        return (Math.abs(dPos[0]) <= (rectSize[0] + g.rectSize[0]) / 2f) &&
            (Math.abs(dPos[1]) <= (rectSize[1] + g.rectSize[1]) / 2f);
    }

    protected void onCreate() {
        if (model3D.TextureBitmap != null) {
            final int[] textureHandle = new int[1];
            GLES30.glGenTextures(1, textureHandle, 0);
            textureId = textureHandle[0];

            if (textureId != 0) {
                GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureId);

                // Load the bitmap into the base level (level 0)
                GLUtils.texImage2D(GLES30.GL_TEXTURE_2D, 0, model3D.TextureBitmap, 0);

                // Set texture parameters
                GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR_MIPMAP_LINEAR);
                GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR);
                GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE);
                GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE);

                // Generate mipmaps
                GLES30.glGenerateMipmap(GLES30.GL_TEXTURE_2D);
            }
        }
    }

    protected void onDelete() {

    }

    /**
     * a function called each time we render a frame
     */
    protected void update() {
        if (!didCallOnCreate) {
            didCallOnCreate = true;
            onCreate();
        }

        // Add Velocity & Angular Velocity
        float[] temp = velocity.clone();
        Vectors.Multiply(temp, dt);
        Vectors.Add(position, temp);

        temp = angularVelocity.clone();
        Vectors.Multiply(temp, dt);
        Vectors.Add(rotation, temp);

        // Calculate the new RT (S) Matrices
        Vectors.setRtMatrix(modelMatrix, position, rotation, scale);
        Vectors.setRtMatrix(rotationMatrix, new float[3], rotation, scale);

        // Call all the update functions
        for (UpdateFunction listener : updateFunctions) {
            listener.onUpdate(this);
        }
    }

    public void addListener(UpdateFunction listener) {
        updateFunctions.add(listener);
    }

    public float getDt() {
        return dt;
    }

    protected static FloatBuffer createDirectNativeOrderFloatBuffer(float[] array) {
        // Allocate a direct ByteBuffer with the desired capacity (in bytes)
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(array.length * Float.BYTES);
        byteBuffer.order(ByteOrder.nativeOrder()); // Set the buffer so OpenGL can use it in the GPU

        FloatBuffer floatBuffer = byteBuffer.asFloatBuffer();
        floatBuffer.put(array);
        floatBuffer.position(0);

        return floatBuffer;
    }

    public static void Delete(GameObject g) {
        ALLGameObjects.remove(g);
    }
}

