package com.example.cross_clean.cross_clean.records;

import android.content.Context;

import com.example.cross_clean.R;
import com.example.cross_clean.game_engine.Model3D;

public class WorldGenerationSettings {
    /// Scene ///
    public float deleteDistance = 75f;
    public float generateDis = 75f;

    public float laneWidth = 1f;
    public int maxLaneNumber = 5;
    public int minLaneNumber = 2;

    public int maxGrassNumber = 4;
    public int minGrassNumber = 1;

    /// Cars ///
    public float baseCarSpawnInterval = 4;         // Minimum time between car spawns
    public float carSpawnIntervalVariance = 4;     // Maximum additional random time added to base interval


    public float laneMaxVelocity = 12;
    public float laneMinVelocity = 4;

    /// Trees ///
    public float treeGenerateDis = 50f;
    public int maxTreeNumberInLane = 8;
    public float safeTreeRadius = 5;
    public float treeMaxXError = 3;


    ///  Models 3D ///
    public Model3D[] laneMiddleModels;
    public Model3D[] laneStartModels;
    public Model3D[] laneEndModels;
    public Model3D[] carsModels;
    public Model3D grassModel;

    public Model3D[] treesModels;


    public WorldGenerationSettings(Context context) {
        laneStartModels = new Model3D[1];
        laneMiddleModels = new Model3D[1];
        laneEndModels = new Model3D[1];

        laneStartModels[0] = Model3D.loadModelById(context, R.raw.chunck, R.raw.road_start_texture);
        laneMiddleModels[0] = Model3D.loadModelById(context, R.raw.chunck, R.raw.road_texture);
        laneEndModels[0] = Model3D.loadModelById(context, R.raw.chunck, R.raw.road_end_texture);

        grassModel = Model3D.loadModelById(context, R.raw.chunck, R.raw.grass_texture);

        carsModels = new Model3D[] {
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
    }
}
