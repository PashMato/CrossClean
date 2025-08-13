package com.example.cross_clean.game_engine;

public enum ObjectGroups {
    OBJECT_GROUP_1,
    OBJECT_GROUP_2,
    OBJECT_GROUP_3,
    OBJECT_GROUP_4,
    OBJECT_GROUP_5,
    OBJECT_GROUP_6,
    OBJECT_GROUP_7,
    OBJECT_GROUP_8,
    OBJECT_GROUP_9,
    OBJECT_GROUP_10,
    OBJECT_GROUP_11,
    OBJECT_GROUP_12,
    OBJECT_GROUP_13,
    OBJECT_GROUP_14,
    OBJECT_GROUP_15,
    OBJECT_GROUP_16,
    OBJECT_GROUP_17,
    NULL;

    public static ObjectGroups fromValue(int value) {
        for (ObjectGroups group : values()) {
            if (group.ordinal() == value) {
                return group;
            }
        }
        return NULL;
    }
}
