package com.example.mygame.froger;

import android.content.Context;

import com.example.mygame.R;
import com.example.mygame.game_engine.CameraOH;
import com.example.mygame.game_engine.GameObject;
import com.example.mygame.game_engine.Model3D;
import com.example.mygame.game_engine.ObjectGroups;

import java.util.Random;

public class Scene {
    float deleteDistance = 75f;
    float lastChunkPos = -75f;
    public float startPos = -1f;
    float generateDis = 75f;
    float generateCarDis = 30f;
    public Model3D[] laneMiddleModels;
    public Model3D[] laneStartModels;
    public Model3D[] laneEndModels;
    public Model3D grassModel;
    public Model3D[] carsModel;

    public Model3D[] treesModels;


    private final Random random;

    public float laneWidth = 1f;
    public int maxLaneNumber = 5;
    public int minLaneNumber = 2;

    public int maxGrassNumber = 4;
    public int minGrassNumber = 1;

    public Scene(Context context) {
        random = new Random();

        laneStartModels = new Model3D[1];
        laneMiddleModels = new Model3D[1];
        laneEndModels = new Model3D[1];

        laneStartModels[0] = Model3D.loadModelById(context, R.raw.chunck, R.raw.road_start_texture);
        laneMiddleModels[0] = Model3D.loadModelById(context, R.raw.chunck, R.raw.road_texture);
        laneEndModels[0] = Model3D.loadModelById(context, R.raw.chunck, R.raw.road_end_texture);

        grassModel = Model3D.loadModelById(context, R.raw.chunck, R.raw.grass_texture);

        carsModel = new Model3D[] {
                Model3D.loadModelById(context, R.raw.car_1, R.raw.car_1_white_texture),
                Model3D.loadModelById(context, R.raw.car_1, R.raw.car_1_red_texture),
                Model3D.loadModelById(context, R.raw.car_1, R.raw.car_1_gray_texture),
                Model3D.loadModelById(context, R.raw.car_1, R.raw.car_1_green_texture),
                Model3D.loadModelById(context, R.raw.car_1, R.raw.car_1_blue_texture),
                Model3D.loadModelById(context, R.raw.car_1, R.raw.car_1_yellow_texture),

                Model3D.loadModelById(context, R.raw.car_2, R.raw.car_2_red_texture),
                Model3D.loadModelById(context, R.raw.car_2, R.raw.car_2_yellow_texture),
                Model3D.loadModelById(context, R.raw.car_2, R.raw.car_2_orange_texture),
            };

        treesModels = new Model3D[2];
        treesModels[0] = Model3D.loadModelById(context, R.raw.tree_1, R.raw.tree_1_texture);
        treesModels[1] = Model3D.loadModelById(context, R.raw.tree_2, R.raw.tree_2_texture);

        Lane.carModels = carsModel;
        Lane.treesModels = treesModels;
    }

    public void UpdateRoad(GameObject g) {
        // This functions run each frame for the scene and generate road
        if (lastChunkPos - CameraOH.cameraPos[2] >= generateDis) {
            return;
        }

        int laneNumber = random.nextInt(maxLaneNumber - minLaneNumber) + minLaneNumber;
        int grassNumber = random.nextInt(maxGrassNumber - minGrassNumber) + minGrassNumber;

        float defaultX = 0;
        float defaultY = 0;

        Model3D model;
        ObjectGroups laneGroup;

        for (int i = 0; i < laneNumber; i++) {
            lastChunkPos += laneWidth;
            float[] Pos = {defaultX, defaultY, lastChunkPos};


            if (i == 0) {
                model = laneStartModels[0];
                laneGroup = ObjectGroups.OBJECT_GROUP_2;
            } else if (i + 1 != laneNumber) {
                model = laneMiddleModels[0];
                laneGroup = ObjectGroups.OBJECT_GROUP_3;
            } else {
                model = laneEndModels[0];
                laneGroup = ObjectGroups.OBJECT_GROUP_4;
            }

            new Lane(Pos, model, true, generateCarDis, deleteDistance)
                    .setObjectGroup(laneGroup);
        }

        for (int i = 0; i < grassNumber; i++) {
            lastChunkPos += laneWidth;
            float[] Pos = {defaultX, defaultY, lastChunkPos};
            new Lane(Pos, grassModel, false, generateCarDis, deleteDistance)
                    .setObjectGroup(ObjectGroups.OBJECT_GROUP_1);

            if (lastChunkPos >= 0) {
                startPos = lastChunkPos;
            }
        }
    }
}
