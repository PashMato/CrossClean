package com.example.cross_clean.cross_clean;

import com.example.cross_clean.game_engine.CameraOH;
import com.example.cross_clean.game_engine.GameObject;
import com.example.cross_clean.game_engine.Model3D;
import com.example.cross_clean.game_engine.ObjectTypes;
import com.example.cross_clean.game_engine.shaders.Rect2D;

public class Player extends GameObject {
    float laneWidth;
    float movingTime = 1;
    float currentMovingTime;
    boolean isMoving;
    boolean isDead;

    CollisionFunction onCollisionFunction = null;

    public Player(float[] pos, Model3D m, float lane_width) {
        super(pos, new Rect2D(m), m, ObjectTypes.DynamicGameObjects);

        laneWidth = lane_width;
    }

    @Override
    protected void onUpdate() {
        super.onUpdate();

        if (isMoving) {
            if (movingTime < currentMovingTime) {
                isMoving = false;
                velocity[2] = 0;

                /* If the player moves a little more than laneWidth
                 * were going to consider it for the next move request
                 */
                currentMovingTime -= movingTime;
            } else {
                velocity[2] = laneWidth / movingTime;
                currentMovingTime += CameraOH.getDt();
            }
        }

        didHitCar();
    }

    /**
     * This function request the (from) player to move forward
     * @return false if the player is already moving and you made a spam request
     * and true if the player just started moving
     */
    public boolean requestMove() {
        if (isDead || isMoving) {
            return false;
        }

        isMoving = true;
        return true;
    }

    public boolean didHitCar() {
        for (int i = 6; i < objectGroups.length; i++) {
            for (int j = 0; j < objectGroups[i].size(); j++) {
                if (objectGroups[i].get(j).objType == ObjectTypes.DynamicGameObjects && checkCollision(objectGroups[i].get(j))) {
                    onCollision();
                    return true;
                }
            }
        }
        return false;
    }

    public void onCollision() {
        if (isDead) {
            return;
        }

        isDead = true;
        scale[1] = 0.1f;
        velocity[2] = 0;
        isMoving = false;

        if ( onCollisionFunction != null) {
            onCollisionFunction.onCollision(this);
        }
    }
}
