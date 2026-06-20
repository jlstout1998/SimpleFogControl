package de.draradech.simplefog.util;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.dimension.DimensionType;

public final class DimensionClassifier {

    private DimensionClassifier() {}

    public static DimensionType type(ClientLevel level) {
        return level.dimensionType();
    }

    public static boolean isOverworldLike(ClientLevel level) {
        DimensionType t = type(level);
        return t.hasSkyLight() && !t.hasEnderDragonFight();
    }

    public static boolean isNetherLike(ClientLevel level) {
        DimensionType t = type(level);
        return !t.hasSkyLight() && t.hasCeiling();
    }

    public static boolean isEndLike(ClientLevel level) {
        DimensionType t = type(level);
        return t.hasSkyLight() && t.hasEnderDragonFight();
    }
}
