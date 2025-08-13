package com.example.cross_clean.game_engine;

public enum ObjectTypes {
    DynamicGameObjects, // this type of object will "act" regular
    Camera, // won't be saved into the AllGameObject list
    Null, // won't be saved into the AllGameObject list
    Background, // this' onUpdate will only be called once every few frames to optimize run-time
}
