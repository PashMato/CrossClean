package com.example.cross_clean.game_engine;

import android.graphics.Color;
import android.opengl.GLES30;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.util.Log;

import com.example.cross_clean.cross_clean.Lane;
import com.example.cross_clean.game_engine.Math.Vectors;
import com.example.cross_clean.game_engine.shaders.Rect2D;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

public class GameObject {

    /// All the gameObjects that not in a group
    protected static List<GameObject> AllGameObjects;

    /// Object Groups; each group (List<GameObjects>) has one model (and one texture)
    /// this prevents OpenGL to switch textures for all the objects in the group which optimize run-time
    protected static List<GameObject>[] objectGroups;

    // ------ GameObjects non static fields ------

    public ObjectTypes objType;
    private ObjectGroups objectGroup = ObjectGroups.NULL;


    // Update function list
    private final List<UpdateFunction> updateFunctions = new ArrayList<>();


    // Object state
    public boolean isActive = true; // Should draw & run update
    boolean didCallOnCreate = false;


    // Collisions
    protected Rect2D rect2D;
    protected Model3D model3D;

    // Buffers from the 3D model
    protected FloatBuffer vertexBuffer;
    protected FloatBuffer normalBuffer;
    protected FloatBuffer uvMapBuffer;

    protected int textureId = 0; // set for null


    // Position, Rotation & Scale
    public float[] position = new float[3]; // current position (left, up, forward)
    public float[] rotation = new float[3];
    public float[] scale = new float[] { 1f, 1f, 1f};

    // Old Position, Rotation & Scale to optimize run-time
    private float[] positionOld = new float[] {Float.NaN, Float.NaN, Float.NaN};
    private float[] rotationOld = new float[] {Float.NaN, Float.NaN, Float.NaN};
    private float[] scaleOld = new float[] {Float.NaN, Float.NaN, Float.NaN};


    // RT (S) Matrices
    protected float[] modelMatrix = new float[16]; // T (S) R - Transform, Scale & Rotation to move the vertexes
    protected float[] rotationScaleMatrix = new float[16]; // R (S) - Rotation & Scale Matrix for the Normals
    protected float[] translationMat = new float[16]; // T - Transform to approve performance


    // Velocities for Position & Rotation
    public float[] velocity = new float[] {0f, 0f, 0f};
    public float[] angularVelocity = new float[] {0f, 0f, 0f};


    public GameObject(float[] pos, Rect2D rect2D, Model3D m, ObjectTypes objType) {
        // Add to the correct object list
        switch (objType) {
            case DynamicGameObjects:
            case Background:
                AllGameObjects.add(this);
                break;
            case Camera:
            case Null:
            default:
                break;
        }

        this.objType = objType;

        this.rect2D = rect2D.clone();

        model3D = new Model3D(m);

        // Copy the position, rotation & scale
        position = pos.clone();

        // Prepare and Load the vertex buffer
        if (m != null) {
            vertexBuffer = createDirectNativeOrderFloatBuffer(model3D.Triangles);
            normalBuffer = createDirectNativeOrderFloatBuffer(model3D.Normals);
            uvMapBuffer = createDirectNativeOrderFloatBuffer(model3D.UVMap);
        } else {
            vertexBuffer = null;
            uvMapBuffer = null;
        }
    }

    /**
     * a function that runs once when the object is created
     * this is different from the constructor because the object can be created before the Camera (OpenGL)
     * and this runs only after the camera has been created
     */
    protected void onCreate() {
        loadTexture();
    }

    /**
     * a function called each time we render a frame
     * if the object type ObjectTypes.Background this will not run every frame to optimize run-time
     */
    protected void onUpdate() {
        if (!didCallOnCreate) {
            didCallOnCreate = true;
            onCreate();
        }

        // Add Velocity & Angular Velocity
        float[] temp = velocity.clone();
        Vectors.Multiply(temp, CameraOH.getDt());
        Vectors.Add(position, temp);

        temp = angularVelocity.clone();
        Vectors.Multiply(temp, CameraOH.getDt());
        Vectors.Add(rotation, temp);

        // Calculate the new RT (S) Matrices
        if (Vectors.any(position, positionOld)) {
            Vectors.setRtMatrix(translationMat, position, null, null);
        }

        if (Vectors.any(rotation, rotationOld) || Vectors.any(scale, scaleOld)) {
            Vectors.setRtMatrix(rotationScaleMatrix, null, rotation, scale);
        }

        if (Vectors.any(position, positionOld) || Vectors.any(rotation, rotationOld) || Vectors.any(scale, scaleOld)) {
            System.arraycopy(position, 0, positionOld, 0, 3);
            System.arraycopy(rotation, 0, rotationOld, 0, 3);
            System.arraycopy(scale, 0, scaleOld, 0, 3);
            Matrix.multiplyMM(modelMatrix, 0, translationMat, 0, rotationScaleMatrix, 0);
        }

        // Call all the update functions
        for (UpdateFunction listener : updateFunctions) {
            listener.onUpdate(this);
        }
    }


    /**
     *  This function runs when the object is deleted
     */
    protected void onDelete() {
        if (vertexBuffer != null) {
            vertexBuffer.clear();
            vertexBuffer = null;
        }

        if (normalBuffer != null) {
            normalBuffer.clear();
            normalBuffer = null;
        }

        if (uvMapBuffer != null) {
            uvMapBuffer.clear();
            uvMapBuffer = null;
        }

        if (textureId != 0) // Delete the texture on OpenGL
        {
            int[] texture = {textureId};
            GLES30.glDeleteTextures(1, texture, 0);
            textureId = 0;
        }

        model3D.onDelete();
        model3D = null;
    }

    public boolean checkCollision(GameObject g) {
        float[] temp = position.clone();
        Vectors.Subtract(temp, g.position);

        float sum = Vectors.Dot(temp, temp);
        temp = getRect2D().getSize();
        sum -= Vectors.Dot(temp, temp);

        temp = g.getRect2D().getSize();
        sum -= Vectors.Dot(temp, temp);


        temp = getRect2D().getXYPos();
        sum -= Vectors.Dot(temp, temp);

        temp = g.getRect2D().getXYPos();
        sum -= Vectors.Dot(temp, temp);

        if (sum > 0) {
            return false;
        }

        float[] rtMatrix = new float[16];
        temp = new float[16];

        Vectors.setCameraMatrix(temp, position, rotation);
        Matrix.multiplyMM(rtMatrix, 0, temp, 0, g.modelMatrix, 0);

        Rect2D rect = g.getRect2D();
        float[] shift = Rect2D.transformPoints(rtMatrix, new float[][] {rect.getXYPos().clone()})[0];
        Vectors.Subtract(shift, rect2D.getXYPos());

        float[][] new_corners = Rect2D.transformPoints(rtMatrix, rect.getCorners());

        boolean any = false;
        for (float[] newCorner : new_corners) {
            Vectors.Add(newCorner, shift);
            any |= rect.dotInRect(newCorner, false);
        }

        return any;
    }

    /**
     * This function add an update listener
     * @param listener the function that we want to call on the update
     */
    public void addListener(UpdateFunction listener) {
        if (updateFunctions.contains(listener)) {
            return;
        }

        updateFunctions.add(listener);
    }

    /**
     * This function removes an update listener
     * @param listener the function that we want to remove from the update
     */
    public void removeListener(UpdateFunction listener) {
        updateFunctions.remove(listener);
    }

    public ObjectGroups getObjectGroup() {
        return objectGroup;
    }

    public Rect2D getRect2D() {
        return rect2D.clone();
    }

    public void setObjectGroup(ObjectGroups og) {
        // Add and remove from the correct list so we wouldn't save the object twice
        if (objectGroup != ObjectGroups.NULL) {
            objectGroups[objectGroup.ordinal()].remove(this);
        } else {
            AllGameObjects.remove(this);
        }

        objectGroup = og;
        if (objectGroup != ObjectGroups.NULL) {
            objectGroups[objectGroup.ordinal()].add(this);
        } else {
            AllGameObjects.add(this);
        }
    }

    /**
     * this function can be called once a camera has been created
     */
    protected void loadTexture() {
        // make sure everything we needs exists
        if (model3D == null || model3D.TextureBitmap == null) {
            return;
        }

        // get a place from OpenGL for the texture
        int[] textureHandle = new int[1];
        GLES30.glGenTextures(1, textureHandle, 0);
        textureId = textureHandle[0];

        if (textureId == 0) { // make sure we didn't got null
            return;
        }

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

    protected void unloadTexture() {
        if (textureId != 0) {
            GLES30.glDeleteTextures(1, new int[]{textureId}, 0);
            textureId = 0; // reset to avoid accidental reuse
        }
    }

    /**
     * this function deletes a GameObject
     * @param g the gameObject we want to delete
     */
    public static void Delete(GameObject g) {
        g.onDelete();
        if (g.getObjectGroup() == ObjectGroups.NULL) {
            AllGameObjects.remove(g);
        } else {
            objectGroups[g.getObjectGroup().ordinal()].remove(g);
        }
    }

    /**
     * create a float buffer for openGL that can be "moved" to the GPU
     * @param array the content we want to save in the buffer
     * @return a buffer object that represents the array
     */
    protected static FloatBuffer createDirectNativeOrderFloatBuffer(float[] array) {
        // Allocate a direct ByteBuffer with the desired capacity (in bytes)
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(array.length * Float.BYTES);
        byteBuffer.order(ByteOrder.nativeOrder()); // Set the buffer so OpenGL can use it in the GPU

        FloatBuffer floatBuffer = byteBuffer.asFloatBuffer();
        floatBuffer.put(array);
        floatBuffer.position(0);

        return floatBuffer;
    }
}

