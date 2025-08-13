package com.example.cross_clean.game_engine;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.example.cross_clean.game_engine.Math.Vectors;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Model3D {
    float[] Triangles;
    float[] Normals;
    float[] UVMap;
    Bitmap TextureBitmap;

    private Model3D(float[] t, float[] n, float[] uv, Bitmap bitmap) {
        Triangles = t;
        Normals = n;
        UVMap = uv;
        TextureBitmap = bitmap;
    }

    public Model3D(Model3D m) {
        if (m == null) {
            Triangles = new float[0];
            Normals = new float[0];
            UVMap = new float[0];
            TextureBitmap = null;
            return;
        }

        // Deep copy arrays
        this.Triangles = m.Triangles != null ? m.Triangles.clone() : null;
        this.Normals = m.Normals != null ? m.Normals.clone() : null;
        this.UVMap = m.UVMap != null ? m.UVMap.clone() : null;

        // Deep copy bitmap (if not null)
        if (m.TextureBitmap != null && !m.TextureBitmap.isRecycled()) {
            this.TextureBitmap = m.TextureBitmap.copy(
                    m.TextureBitmap.getConfig(), true // true = mutable
            );
        } else {
            this.TextureBitmap = null;
        }
    }

    public static Model3D createEmpty() {
        return new Model3D(new float[] {}, new float[] {}, new float[] {}, null);
    }

    /**
     * Loads a .ply model to a 3D model object
     * @param is_3d InputStream leading to the file
     * @return a Model3D Object representing the 3D model
     */
    protected static Model3D loadModelByPath(InputStream is_3d, InputStream is_texture) {
        try {
            return parse(is_3d, is_texture);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Loads a .ply model to a 3D model object
     * @param context the Intent context
     * @param modelId the Id of the model
     * @param textureId the Id of the model's texture
     * @return a Model3D Object representing the 3D model
     */
    public static Model3D loadModelById(Context context, int modelId, int textureId) {
        return Model3D.loadModelByPath(context.getResources().openRawResource(modelId),
                context.getResources().openRawResource(textureId));
    }
    private static Model3D parse(InputStream is_3d, InputStream is_texture) throws IOException {
        /*
            a parser from .ply
         */
        List<float[]> vertices = new ArrayList<>();
        ArrayList<Integer> triangles = new ArrayList<>();
        List<float[]> uv_map = new ArrayList<>();

        int vertexCount = 0;
        int faceCount = 0;
        boolean headerEnded = false;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is_3d))) {
            String line;

            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!headerEnded) {
                    if (line.startsWith("element vertex")) {
                        vertexCount = Integer.parseInt(line.split(" ")[2]);
                    } else if (line.startsWith("element face")) {
                        faceCount = Integer.parseInt(line.split(" ")[2]);
                    } else if (line.startsWith("end_header")) {
                        headerEnded = true;
                    }
                    continue;
                }

                // Read vertices
                if (vertices.size() < vertexCount) {
                    String[] parts = line.split(" ");
                    float x = Float.parseFloat(parts[0]);
                    float y = Float.parseFloat(parts[2]);
                    float z = Float.parseFloat(parts[1]);
                    float s = Float.parseFloat(parts[3]);
                    float t = 1 - Float.parseFloat(parts[4]); // Blender (0, 0) is on the top-left and OpenGL (0, 0) is on the bottom-left

                    vertices.add(new float[] {x, y, z});
                    uv_map.add(new float[] {s, t});
                }

                // Read faces
                else if (triangles.size() < faceCount * 3) {
                    String[] parts = line.split(" ");

                    int[] face = new int[parts.length - 1];
                    for (int i = 0; i < face.length; i++)
                    { // the first number indicates how many vertices are in the face
                        triangles.add(Integer.parseInt(parts[i + 1]));
                    }

                }
            }
        }


        float[] tri = new float[triangles.size() * 3];
        for (int i = 0; i < tri.length; i += 3) {
            float[] vertex = vertices.get(triangles.get(i / 3));
            tri[i    ] = vertex[0];
            tri[i + 1] = vertex[1];
            tri[i + 2] = vertex[2];
        }

        // Combine face uv_map based on average of vertex uv_map
        float[] triangleUVCoords = new float[triangles.size() * 2];
        for (int i = 0; i < triangles.size(); i++) {
            triangleUVCoords[2 * i    ] = uv_map.get(triangles.get(i))[0];
            triangleUVCoords[2 * i + 1] = uv_map.get(triangles.get(i))[1];
        }

        float[] normals = new float[tri.length];
        for (int i = 0; i < tri.length; i += 9) {
            // Get triangle vertices
            float[] v0 = Arrays.copyOfRange(tri, i + 0, i + 3);
            float[] v1 = Arrays.copyOfRange(tri, i + 3, i + 6);
            float[] v2 = Arrays.copyOfRange(tri, i + 6, i + 9);


            float[] n = new float[3];
            Vectors.Cross(n, v0, v1, v2);

            // Assign normal to all 3 vertices
            for (int j = 0; j < 3; j++) {
                normals[i + j * 3    ] = n[0];
                normals[i + j * 3 + 1] = n[1];
                normals[i + j * 3 + 2] = n[2];
            }
        }


        Bitmap bitmap = BitmapFactory.decodeStream(is_texture);

        return new Model3D(tri, normals, triangleUVCoords, bitmap);
    }

    public float[] getTriangles() {
        return Triangles == null ? null : Triangles.clone();
    }

    public void onDelete() {
        Triangles = null;
        Normals = null;
        UVMap = null;

        if (TextureBitmap != null && !TextureBitmap.isRecycled()) {
            TextureBitmap.recycle(); // Frees the native memory
            TextureBitmap = null;    // Helps GC clean up the Java reference
        }
    }
}
