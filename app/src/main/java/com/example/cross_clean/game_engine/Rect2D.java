package com.example.cross_clean.game_engine;

import androidx.annotation.NonNull;

import com.example.cross_clean.game_engine.Math.Vectors;

/**
 * this class does simple 2D recting (XZ)
 */
public class Rect2D {
    float[] XYPos; // the shift relative to the start pos of the GameObject
    float[] Size; // the size of the rect

    /**
     * @return the rect's shift (XYPos)
     */
    public float[] getXYPos() {
        return XYPos.clone();
    }

    /**
     * @return the rect size
     */
    public float[] getSize() {
        return Size.clone();
    }

    /**
     * creates a 2D rect
     * @param xy_pos the shift relative to the start pos of the GameObject
     * @param size the size of the rect
     */
    public Rect2D(float[] xy_pos, float[] size) {
        XYPos = xy_pos.clone();
        Size = size.clone();
    }

    /**
     * creates an empty rect
     */
    public Rect2D() {
        this(new float[2], new float[2]);
    }

    /**
     * creates a react from a Model3D
     * @param m the Model3D to create the rect from
     */
    public Rect2D(Model3D m) { // Calculates the Rect size and position from a Model3D
        XYPos = new float[2];
        float[] xyz_max = new float[] {Float.MIN_VALUE, Float.MIN_VALUE, Float.MIN_VALUE};
        float[] xyz_min = new float[] {Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE};
        float[] tri = m.getTriangles();

        for (int i = 0; i < tri.length / 3; i++) {
            float[] vertex = new float[] {tri[3 * i], tri[3 * i + 1], tri[3 * i + 2]};
            Vectors.Max(xyz_max, vertex);
            Vectors.Min(xyz_min, vertex);
        }

        float[] size = xyz_max.clone();
        Vectors.Subtract(size, xyz_min);
        Vectors.Multiply(size, 0.5f);
        Size = new float[] {size[0], size[1]};

        float[] xz_pos = xyz_max.clone();
        Vectors.Add(xz_pos, xyz_min);
        Vectors.Multiply(xz_pos, 0.5f);
        XYPos = new float[] {xz_pos[0], xz_pos[2]};
    }

    @NonNull
    @Override
    public Rect2D clone() {
        return new Rect2D(XYPos, Size);
    }


    /**
     * checks if a dot is in the rect2D
     * @param pos the pos of the dot
     * @param considerShift do we want to consider the rect shift (XYPos)
     * @return is the dot in the rect
     */
    public boolean dotInRect(float[] pos, boolean considerShift) {
        float[] dot = pos.clone();
        if (considerShift) {
            Vectors.Add(dot, XYPos);
        }
        Vectors.Abs(dot);

        return Size[0] > dot[0] && Size[1] > dot[1];
    }

    /**
     * @return 4 corners in local space
     */
    public float[][] getCorners() {
        float x = XYPos[0], y = XYPos[1];
        float w = Size[0], h = Size[1];

        return new float[][]{
                {x,     y    }, // bottom-left
                {x + w, y    }, // bottom-right
                {x + w, y + h}, // top-right
                {x,     y + h}  // top-left
        };
    }

    /**
     * transforms all of the points
     * @param matrix the RT (S) matrix of the transformation
     * @param points the array of the points
     * @return the points transformed in a new array
     */
    public static float[][] transformPoints(float[] matrix, float[][] points) {
        float tx;
        float ty;

        for (int i = 0; i < points.length; i++) {
            // Assume 2D transform in 4x4 matrix (ignore z, set w = 1)
            tx = matrix[0] * points[i][0] + matrix[4] * points[i][1] + matrix[12];
            ty = matrix[1] * points[i][0] + matrix[5] * points[i][1] + matrix[13];
            points[i][0] = tx;
            points[i][1] = ty;
        }

        return points;
    }
}
