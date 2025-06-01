package com.example.mygame;

import android.content.Context;

import com.example.mygame.game_engine.CameraOH;
import com.example.mygame.game_engine.GameObject;
import com.example.mygame.game_engine.Math.Vectors;
import com.example.mygame.game_engine.Model3D;
import com.example.mygame.game_engine.ObjectTypes;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Scene {
    float lastChunkPos = 0f;
    float generateDis = 50f;
    public float deleteDistance = 100f;
    public Model3D laneModel;
    public Model3D grassModel;
    public Model3D carModel;

    private Random random;

    public float laneWidth = 2f;
    public int maxLaneNumber = 4;
    public int minLaneNumber = 3;

    public int maxGrassNumber = 4;
    public int minGrassNumber = 1;

    public Scene(Context context, CameraOH camera) {
        laneModel = Model3D.loadModelByPath(context.getResources().openRawResource(R.raw.chunck),
                context.getResources().openRawResource(R.raw.road_texture));

        grassModel = Model3D.loadModelByPath(context.getResources().openRawResource(R.raw.chunck),
                context.getResources().openRawResource(R.raw.grass_texture));
        random = new Random();
    }

    public void UpdateRoad(GameObject g) {
        if (lastChunkPos - CameraOH.cameraPos[2] >= generateDis) {
            return;
        }

        int laneNumber = random.nextInt(maxLaneNumber - minLaneNumber) + minLaneNumber;
        int grassNumber = random.nextInt(maxGrassNumber - minGrassNumber) + minGrassNumber;

        float defaultX = 0;
        float defaultY = 0;

        for (int i = 0; i < laneNumber; i++) {
            lastChunkPos += laneWidth;
            float[] Pos = {defaultX, defaultY, lastChunkPos};
            GameObject lane = new GameObject(Pos, new float[] {0, 0}, laneModel, ObjectTypes.Background);
            lane.addListener(this::GenerateCars);
            lane.addListener(this::ChunkGC);
        }

        for (int i = 0; i < grassNumber; i++) {
            lastChunkPos += laneWidth;
            float[] Pos = {defaultX, defaultY, lastChunkPos};
            GameObject lane = new GameObject(Pos, new float[2], grassModel, ObjectTypes.Background);
            GenerateTrees(lane);
            lane.addListener(this::ChunkGC);
        }

//        Vectors.lookAt(g.position, g.rotation, new
//                float[] {defaultX, defaultY, lastChunkPos}, 0, 4);
    }

    private void GenerateTrees(GameObject g) {
        // TODO: implement this
    }
    private void GenerateCars(GameObject g) {
        // TODO: implement this
    }
    private void CarGC(GameObject g) {
        float[] dis = g.position.clone();
        Vectors.Subtract(dis, CameraOH.cameraPos);
        Vectors.Abs(dis);
        if (dis[0] < deleteDistance) {
            return;
        }
        GameObject.Delete(g);
    }

    private void ChunkGC(GameObject g) {
        float[] dis = g.position.clone();
        Vectors.Subtract(dis, CameraOH.cameraPos);
        Vectors.Abs(dis);
        if (dis[2] < deleteDistance) {
            return;
        }
        GameObject.Delete(g);
    }
    private void TreeGC(GameObject g) {
        float[] dis = g.position.clone();
        Vectors.Subtract(dis, CameraOH.cameraPos);
        Vectors.Abs(dis);

        if (dis[0] < deleteDistance) {
            return;
        }

        GameObject.Delete(g);
    }
}
