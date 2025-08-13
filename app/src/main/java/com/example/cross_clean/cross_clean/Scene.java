package com.example.cross_clean.cross_clean;

import android.content.Context;
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

public class Scene {
    float lastChunkPos = -75f;
    public float startPos = -1f;

    ArrayDeque<GameObject> carPool = new ArrayDeque<>();
    ArrayDeque<GameObject> treePool = new ArrayDeque<>();

    private final Random random;

    public WorldGenerationSettings settings;

    public Scene(Context c) {
        random = new Random();

        settings = new WorldGenerationSettings(c);
    }

    public void UpdateRoad(GameObject g) {
        // This functions run each frame for the scene and generate road
        if (lastChunkPos - CameraOH.cameraPos[2] >= settings.generateDis) {
            return;
        }

        int laneNumber = random.nextInt(settings.maxLaneNumber - settings.minLaneNumber) + settings.minLaneNumber;
        int grassNumber = random.nextInt(settings.maxGrassNumber - settings.minGrassNumber) + settings.minGrassNumber;

        float defaultX = 0;
        float defaultY = 0;

        Model3D model;
        ObjectGroups laneGroup;

        for (int i = 0; i < laneNumber; i++) {
            lastChunkPos += settings.laneWidth;
            float[] Pos = {defaultX, defaultY, lastChunkPos};


            if (i == 0) {
                model = settings.laneStartModels[0];
                laneGroup = ObjectGroups.OBJECT_GROUP_2;
            } else if (i + 1 != laneNumber) {
                model = settings.laneMiddleModels[0];
                laneGroup = ObjectGroups.OBJECT_GROUP_3;
            } else {
                model = settings.laneEndModels[0];
                laneGroup = ObjectGroups.OBJECT_GROUP_4;
            }

            Lane road = new Lane(Pos, model, true, settings);
            road.setObjectGroup(laneGroup);
            road.addListener(this::GenerateCars);
        }

        for (int i = 0; i < grassNumber; i++) {
            lastChunkPos += settings.laneWidth;
            float[] Pos = {defaultX, defaultY, lastChunkPos};
            Lane grass = new Lane(Pos, settings.grassModel, false, settings);
            grass.setObjectGroup(ObjectGroups.OBJECT_GROUP_1);
            GenerateTrees(grass);

            if (lastChunkPos >= 0) {
                startPos = lastChunkPos;
            }
        }
    }

    private void GenerateCars(GameObject gameObject) {
        Lane g = (Lane) gameObject;

        // limit the distance we generate from
        float[] dis = g.position.clone();
        Vectors.Subtract(dis, CameraOH.cameraPos);
        Vectors.Abs(dis);

        // calculate if we can send another car
        g.timeSinceLastCarSpawn += CameraOH.getDt();
        if (g.timeSinceLastCarSpawn < g.nextCarSpawnTime + (float) Math.min(Math.pow(dis[2] / 10f, 5), 80) * Math.max(random.nextFloat(), 0.6)) {
            return;
        }
        g.timeSinceLastCarSpawn = 0;


        g.nextCarSpawnTime = settings.baseCarSpawnInterval + (random.nextFloat() * settings.carSpawnIntervalVariance * Math.max((dis[2] / 20f), 2f));

        GameObject car;
        car = carPool.poll();

        if (car != null) {
            car.isActive = true;

            Log.i("LaneUpdate", "Re-using a car at lane at Z: " + g.position[2]);
        } else {
            int carModelIndex = random.nextInt(settings.carsModels.length);
            Model3D carModel = settings.carsModels[carModelIndex];
            Rect2D rect = new Rect2D(carModel);
            car = new GameObject(g.position, rect, carModel, ObjectTypes.DynamicGameObjects);
            car.addListener(this::CarGC);

            car.setObjectGroup(ObjectGroups.fromValue(7 + carModelIndex));
            car.isActive = true;

            Log.i("LaneUpdate", "Created a car at lane at Z: " + g.position[2]);
        }
        car.position[0] = g.direction * -0.5f * g.getRect2D().getSize()[0];
        car.position[2] = g.position[2];
        car.rotation[1] = g.direction * 90f;
        car.velocity[0] = g.direction * g.carsVelocity;
    }
    private void CarGC(GameObject g) {
        float[] dis = g.position.clone();
        Vectors.Subtract(dis, CameraOH.cameraPos);
        Vectors.Abs(dis);
        if (dis[0] < settings.deleteDistance) {
            return;
        }

        Log.i("CarGC", "Pooled-away car at lane at Z: " + g.position[2]);
        g.isActive = false;
        g.velocity[0] = 0;

        carPool.add(g);
    }

    private void GenerateTrees(GameObject g) {
        float[] dis = g.position.clone();
        Vectors.Subtract(dis, CameraOH.cameraPos);
        Vectors.Abs(dis);

        if (dis[2] > settings.treeGenerateDis) {
            return;
        }

        int treeNum = random.nextInt(settings.maxTreeNumberInLane);
        float[] size = g.getRect2D().getSize();

        int treeModelIndex;
        Model3D treeModel;
        GameObject tree;

        for (int i = 0; i < treeNum; i++) {
            float[] pos = new float[]
                    {
                            i * size[0] / treeNum - size[0] / 2f + 2 * random.nextFloat() * settings.treeMaxXError - 1,
                            0f,
                            g.position[2]
                    };

            if (Math.abs(pos[0]) < settings.safeTreeRadius) {
                continue;
            }

            tree = treePool.poll();
            if (tree != null) {
                tree.isActive = true;
                tree.position = pos;
                Log.i("GenerateTrees", "Re-using tree at (" + pos[0] + ", " + pos[1] + ", " + pos[2] + ")");
            } else {
                treeModelIndex = random.nextInt(settings.treesModels.length);
                treeModel = settings.treesModels[treeModelIndex];
                Log.i("GenerateTrees", "Creating tree at (" + pos[0] + ", " + pos[1] + ", " + pos[2] + ")");
                tree = new GameObject(pos, new Rect2D(), treeModel, ObjectTypes.Background);
                tree.isActive = true;

                tree.setObjectGroup(treeModelIndex == 0 ? ObjectGroups.OBJECT_GROUP_5 : ObjectGroups.OBJECT_GROUP_6);
                tree.addListener(this::TreeGC);
            }

            tree.rotation[1] = (float) random.nextInt(360);
        }
    }

    private void TreeGC(GameObject g) {
        float[] dis = g.position.clone();
        Vectors.Subtract(dis, CameraOH.cameraPos);

        if (dis[2] > -settings.treeGenerateDis) {
            return;
        }

        Log.i("TreeGC", "Pooled-away tree at (" + g.position[0] + ", " + g.position[1] + ", " + g.position[2] + ")");

        g.isActive = false;
        treePool.add(g);
    }
}
