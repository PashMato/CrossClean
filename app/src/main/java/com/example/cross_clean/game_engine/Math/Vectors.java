package com.example.cross_clean.game_engine.Math;

import android.opengl.Matrix;

public class Vectors {
    public static void Add(float[] vOut, float[] vAdd) {
        for (int i = 0; i < vOut.length; i++) {
            vOut[i] += vAdd[i];
        }
    }

    public static void Add(float[] vOut, float sAdd) {
        for (int i = 0; i < vOut.length; i++) {
            vOut[i] += sAdd;
        }
    }

    public static void Subtract(float[] vOut, float[] vAdd) {
        for (int i = 0; i < vOut.length; i++) {
            vOut[i] -= vAdd[i];
        }
    }

    public static void Subtract(float[] vOut, float sAdd) {
        for (int i = 0; i < vOut.length; i++) {
            vOut[i] -= sAdd;
        }
    }

    public static void Multiply(float[] vOut, float[] vAdd) {
        for (int i = 0; i < vOut.length; i++) {
            vOut[i] *= vAdd[i];
        }
    }

    public static void Multiply(float[] vOut, float sAdd) {
        for (int i = 0; i < vOut.length; i++) {
            vOut[i] *= sAdd;
        }
    }

    public static void Max(float[] vOut, float[] vMax) {
        for (int i = 0; i < vOut.length; i++) {
            vOut[i] = Math.max(vOut[i], vMax[i]);
        }
    }

    public static void Min(float[] vOut, float[] vMin) {
        for (int i = 0; i < vOut.length; i++) {
            vOut[i] = Math.min(vOut[i], vMin[i]);
        }
    }

    public static float Dot(float[] v1, float[] v2) {
        float r = 0;

        for (int i = 0; i < v1.length; i++) {
            r += v1[i] * v2[i];
        }

        return r;
    }

    public static void Abs(float[] v) {
        for (int i = 0; i < v.length; i++) {
            v[i] = Math.abs(v[i]);
        }
    }

    public static float Norm(float[] v) {
        float r = 0;

        for (float value : v) {
            r += value * value;
        }

        return (float) Math.sqrt(r);
    }
    public static void Normalize(float[] vOut) {
        float norm = Vectors.Norm(vOut);
        if (norm != 0) {
            Vectors.Multiply(vOut, 1 / norm);
        }
    }
    public static void Cross(float[] vOut, float[] v0, float[] v1, float[] v2) {
        // Compute edges
        float[] edge1 = v1.clone();
        float[] edge2 = v2.clone();

        Vectors.Subtract(edge1, v0);
        Vectors.Subtract(edge2, v0);

        // Cross product (normal)
        vOut[0] = edge1[1] * edge2[2] - edge1[2] * edge2[1];
        vOut[1] = edge1[2] * edge2[0] - edge1[0] * edge2[2];
        vOut[2] = edge1[0] * edge2[1] - edge1[1] * edge2[0];

        Vectors.Normalize(vOut);
    }

    public static void setRtMatrix(float[] mat, float[] pos, float[] rot, float[] scale) {
        float[] tempMatrix = new float[16];
        // Start with identity
        Matrix.setIdentityM(mat, 0);

        if (scale != null) {
            // Apply scaling
            Matrix.setIdentityM(tempMatrix, 0);
            Matrix.scaleM(tempMatrix, 0, scale[0], scale[1], scale[2]);
            Matrix.multiplyMM(mat, 0, tempMatrix, 0, mat, 0);
        }

        if (rot != null) {
            // Apply rotation around Y
            Matrix.setIdentityM(tempMatrix, 0);
            Matrix.rotateM(tempMatrix, 0, rot[1], 0, 1, 0);
            Matrix.multiplyMM(mat, 0, tempMatrix, 0, mat, 0);

            // Apply rotation around X
            Matrix.setIdentityM(tempMatrix, 0);
            Matrix.rotateM(tempMatrix, 0, rot[0], 1, 0, 0);
            Matrix.multiplyMM(mat, 0, tempMatrix, 0, mat, 0);

            // Apply rotation around Z
            Matrix.setIdentityM(tempMatrix, 0);
            Matrix.rotateM(tempMatrix, 0, rot[2], 0, 0, 1);
            Matrix.multiplyMM(mat, 0, tempMatrix, 0, mat, 0);
        }

        if (pos != null) {
            // Apply translation
            Matrix.setIdentityM(tempMatrix, 0);
            Matrix.translateM(tempMatrix, 0, pos[0], pos[1], pos[2]);
            Matrix.multiplyMM(mat, 0, mat, 0, tempMatrix, 0);
        }
    }

    public static void setCameraMatrix(float[] outMatrix, float[] position, float[] rotation /* pitch, yaw, roll */) {
        float[] pos = position.clone();
        float[] rot = rotation.clone();
        Vectors.Multiply(pos, -1);
        Vectors.Multiply(rot, -1);

        rot[1] += 180f;
        Vectors.setRtMatrix(outMatrix, pos, rot, null);
    }

    /**
     * makes vOut 1 where v0 is bigger than v1
     * @param vOut the out vector
     * @param v0 the in vector (the vector you check)
     * @param v1 the limit vector
     */
    public static void isBiggerThan(float[] vOut, float[] v0, float[] v1) {
        for (int i = 0; i < vOut.length; i++) {
            vOut[i] = v0[i] > v1[i] ? 1 : 0;
        }
    }

    /**
     * makes vOut 1 where v is bigger than l
     * @param vOut the out vector
     * @param v the in vector (the vector you check)
     * @param l the limit vector
     */
    public static void isBiggerThan(float[] vOut, float[] v, float l) {
        for (int i = 0; i < vOut.length; i++) {
            vOut[i] = v[i] > l ? 1 : 0;
        }
    }

    public static void sign(float[] vOut) {
        for (int i = 0; i < vOut.length; i++) {
            vOut[i] = Math.signum(vOut[i]);
        }
    }

    public static boolean any(float[] vC1, float[] vC2) {
        for (int i = 0; i < vC1.length; i++) {
            if (vC1[i] != vC2[i]) {
                return true;
            }
        }
        return false;
    }

    public static void lookAt(float[] pOut, float[] rOut, float[] lookAtPos, float angle, float r) {
        float[] dir = {
                (float) Math.cos(angle * Math.PI / 180),
                0,
                (float) Math.sin(angle * Math.PI / 180),
            };

        // Change the pOut so we'll look at that direction (not in the y axis)
        pOut[0] = lookAtPos[0];
        pOut[2] = lookAtPos[2];

        Vectors.Multiply(dir, -r);
        Vectors.Add(pOut, dir);

        rOut[1] = -angle + 90;
    }
}
