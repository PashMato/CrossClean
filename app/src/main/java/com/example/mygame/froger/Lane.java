package com.example.mygame.froger;

import android.util.Log;

import com.example.mygame.game_engine.CameraOH;
import com.example.mygame.game_engine.GameObject;
import com.example.mygame.game_engine.Math.Vectors;
import com.example.mygame.game_engine.Model3D;
import com.example.mygame.game_engine.ObjectGroups;
import com.example.mygame.game_engine.ObjectTypes;
import com.example.mygame.game_engine.shaders.Rect2D;

import java.util.ArrayDeque;
import java.util.Random;

public class Lane extends GameObject {
    public static Model3D[] carModels;
    public static Model3D[] treesModels;

    static ArrayDeque<GameObject> carPool = new ArrayDeque<>();
    static ArrayDeque<GameObject> treePool = new ArrayDeque<>();

    float deleteDistance;
    float generateDistance;

    boolean isRoad;

    float baseCarSpawnInterval = 4;         // Minimum time between car spawns
    float carSpawnIntervalVariance = 4;     // Maximum additional random time added to base interval
    float timeSinceLastCarSpawn = 0;        // Time elapsed since last car was spawned
    float nextCarSpawnTime = 0;             // Time required before spawning the next car

    float laneMaxVelocity = 12;
    float laneMinVelocity = 4;


    float carsVelocity; // the cars velocity

    public float treeGenerateDis = 50f;
    public int maxTreeNumberInLane = 8;
    public float safeTreeRadius = 5;
    public float treeMaxXError = 3;
    public boolean didGenerateTrees = false;

    float direction;
    Random random;

    public Lane(float[] pos, Model3D m, boolean is_road, float generate_distance, float delete_distance) {
        super(pos, new Rect2D(m), m, is_road ? ObjectTypes.DynamicGameObjects : ObjectTypes.Background);

        isRoad = is_road;

        generateDistance = generate_distance;
        deleteDistance = delete_distance;
        addListener(this::ChunkGC);

        random = new Random();
        direction = (random.nextInt(2) - 0.5f) * 2f;
        carsVelocity = laneMinVelocity + random.nextFloat() * (laneMaxVelocity - laneMinVelocity);
    }

    @Override
    protected void onUpdate() {
        super.onUpdate();

        if (isRoad) {
            GenerateCars();
        } else {
            GenerateTrees();
        }
    }

    private void GenerateTrees() {
        if (didGenerateTrees) {
            return;
        }


        float[] dis = position.clone();
        Vectors.Subtract(dis, CameraOH.cameraPos);
        Vectors.Abs(dis);

        if (dis[2] > treeGenerateDis) {
            return;
        }

        didGenerateTrees = true; // Makes sure we don't generate trees too far away

        int treeNum = random.nextInt(maxTreeNumberInLane);
        float[] size = getRect2D().getSize();

        int treeModelIndex;
        Model3D treeModel;
        GameObject tree;

        for (int i = 0; i < treeNum; i++) {
            float[] pos = new float[]
                    {
                            i * size[0] / treeNum - size[0] / 2f + 2 * random.nextFloat() * treeMaxXError - 1,
                            0f,
                            position[2]
                    };

            if (Math.abs(pos[0]) < safeTreeRadius) {
                continue;
            }

            tree = treePool.poll();
            if (tree != null) {
                tree.isActive = true;
                tree.position = pos;
                Log.i("GenerateTrees", "Re-using tree at (" + pos[0] + ", " + pos[1] + ", " + pos[2] + ")");
            } else {
                treeModelIndex = random.nextInt(treesModels.length);
                treeModel = treesModels[treeModelIndex];
                Log.i("GenerateTrees", "Creating tree at (" + pos[0] + ", " + pos[1] + ", " + pos[2] + ")");
                tree = new GameObject(pos, new Rect2D(), treeModel, ObjectTypes.Background);

                tree.setObjectGroup(treeModelIndex == 0 ? ObjectGroups.OBJECT_GROUP_5 : ObjectGroups.OBJECT_GROUP_6);
                tree.addListener(this::TreeGC);
            }

            tree.rotation[1] = (float) random.nextInt(360);
        }
    }

    private void GenerateCars() {
        // limit the distance we generate from
        float[] dis = position.clone();
        Vectors.Subtract(dis, CameraOH.cameraPos);
        Vectors.Abs(dis);

        // calculate if we can send another car
        timeSinceLastCarSpawn += getDt();
        if (timeSinceLastCarSpawn < nextCarSpawnTime + (float) Math.min(Math.pow(dis[2] / 10f, 5), 80) * Math.max(random.nextFloat(), 0.6)) {
            return;
        }
        timeSinceLastCarSpawn = 0;


        nextCarSpawnTime = baseCarSpawnInterval + (random.nextFloat() * carSpawnIntervalVariance * Math.max((dis[2] / 20f), 2f));

        GameObject car;
        car = carPool.poll();

        if (car != null) {
            car.isActive = true;

            Log.i("LaneUpdate", "Re-using a car at lane at Z: " + position[2]);
        } else {
            int carModelIndex = random.nextInt(carModels.length);
            Model3D carModel = carModels[carModelIndex];
            Rect2D rect = new Rect2D(carModel);
            car = new GameObject(position, rect, carModel, ObjectTypes.DynamicGameObjects);
            car.addListener(this::CarGC);

            car.setObjectGroup(ObjectGroups.fromValue(7 + carModelIndex));

            Log.i("LaneUpdate", "Created a car at lane at Z: " + position[2]);
        }
        car.position[0] = direction * -0.5f * rect2D.getSize()[0];
        car.position[2] = position[2];
        car.rotation[1] = direction * 90f;
        car.velocity[0] = direction * carsVelocity;
    }
    private void CarGC(GameObject g) {
        float[] dis = g.position.clone();
        Vectors.Subtract(dis, CameraOH.cameraPos);
        Vectors.Abs(dis);
        if (dis[0] < deleteDistance) {
            return;
        }

        Log.i("CarGC", "Pooled-away car at lane at Z: " + position[2]);
        g.isActive = false;
        g.velocity[0] = 0;

        carPool.add(g);
    }

    private void TreeGC(GameObject g) {
        float[] dis = g.position.clone();
        Vectors.Subtract(dis, CameraOH.cameraPos);

        if (dis[2] > -treeGenerateDis) {
            return;
        }

        Log.i("TreeGC", "Pooled-away tree at (" + g.position[0] + ", " + g.position[1] + ", " + g.position[2] + ")");

        g.isActive = false;
        treePool.add(g);
    }
    private void ChunkGC(GameObject g) {
        float[] dis = g.position.clone();
        Vectors.Subtract(dis, CameraOH.cameraPos);
        if (dis[2] > -deleteDistance) {
            return;
        }
        Log.i("ChunkGC", "Deleted Chunk at lane at Z: " + position[2]);
        GameObject.Delete(g);
    }

}
