package com.example.cross_clean.cross_clean;

import android.util.Log;

import com.example.cross_clean.cross_clean.records.WorldGenerationSettings;
import com.example.cross_clean.game_engine.CameraOH;
import com.example.cross_clean.game_engine.GameObject;
import com.example.cross_clean.game_engine.Math.Vectors;
import com.example.cross_clean.game_engine.Model3D;
import com.example.cross_clean.game_engine.ObjectGroups;
import com.example.cross_clean.game_engine.ObjectTypes;
import com.example.cross_clean.game_engine.shaders.Rect2D;

import java.util.ArrayDeque;
import java.util.Random;

public class Lane extends GameObject {
    WorldGenerationSettings settings;

    public boolean isRoad;
    public float timeSinceLastCarSpawn = 0;               // Time elapsed since last car was spawned
    public float nextCarSpawnTime = 0;             // Time required before spawning the next car
    float carsVelocity; // the cars velocity

    float direction;
    Random random;

    public Lane(float[] pos, Model3D m, boolean is_road, WorldGenerationSettings s) {
        super(pos, new Rect2D(m), m, is_road ? ObjectTypes.DynamicGameObjects : ObjectTypes.Background);

        isRoad = is_road;

        settings = s;

        random = new Random();
        direction = (random.nextInt(2) - 0.5f) * 2f;
        carsVelocity = settings.laneMinVelocity +
                random.nextFloat() * (settings.laneMaxVelocity - settings.laneMinVelocity);
    }

    @Override
    protected void onUpdate() {
        super.onUpdate();
        ChunkGC(this);
    }

    private void ChunkGC(GameObject g) {
        float[] dis = g.position.clone();
        Vectors.Subtract(dis, CameraOH.cameraPos);
        if (dis[2] > -settings.deleteDistance) {
            return;
        }
        Log.i("ChunkGC", "Deleted Chunk at lane at Z: " + g.position[2]);
        GameObject.Delete(g);
    }


}
