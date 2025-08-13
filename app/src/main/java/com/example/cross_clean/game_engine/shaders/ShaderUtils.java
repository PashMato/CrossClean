package com.example.cross_clean.game_engine.shaders;

import android.opengl.GLES30;
import android.util.Log;

public class ShaderUtils {
    /**
     * built and compiles a shader by type
     * if failed will return 0
     * @param type the shader type (GLES30.GL_FRAGMENT_SHADER, GLES30.GL_VERTEX_SHADER)
     * @param shaderCode the actual shader code as a String
     * @return the shader cade
     */
    public static int loadShader(int type, String shaderCode) {
        int shader = GLES30.glCreateShader(type);
        GLES30.glShaderSource(shader, shaderCode);
        GLES30.glCompileShader(shader);

        int[] compiled = new int[1];
        GLES30.glGetShaderiv(shader, GLES30.GL_COMPILE_STATUS, compiled, 0);
        if (compiled[0] == 0) {
            Log.e("ShaderUtils", "Could not compile shader: " + GLES30.glGetShaderInfoLog(shader));
            GLES30.glDeleteShader(shader);
            return 0;
        }

        return shader;
    }
}
