package de.draradech.simplefog.mixin;

import de.draradech.simplefog.SimpleFogConfig;
import de.draradech.simplefog.SimpleFogMain;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.environment.AtmosphericFogEnvironment;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AtmosphericFogEnvironment.class)
public class AtmosphericFogEnvironmentMixin {
    private static float currentRainFactor = 0.0f;
    private static float currentIndoorFactor = 0.0f;
    private static float approach(float current, float target, float step) {
        if (Float.isNaN(current)) return target;
        return current < target ? Math.min(target, current + step) : Math.max(target, current - step);
    }

    @Inject(at = @At("TAIL"), method = "setupFog(Lnet/minecraft/client/renderer/fog/FogData;Lnet/minecraft/client/Camera;Lnet/minecraft/client/multiplayer/ClientLevel;FLnet/minecraft/client/DeltaTracker;)V")
    public void tailSetupFog(FogData fogData, Camera camera, ClientLevel clientLevel, float viewDistance, DeltaTracker deltaTracker, CallbackInfo ci)
    {
        SimpleFogConfig config = SimpleFogMain.config;
        if (config.overworldToggle && clientLevel.dimension() == Level.OVERWORLD)
        {
            SimpleFogConfig.RainConfig rainConf = config.rainConfig;

            if (rainConf.rainToggle)
            {
                BlockPos blockPos = camera.blockPosition();
                boolean skylight = blockPos.getY() >= clientLevel.getHeight(Heightmap.Types.WORLD_SURFACE, blockPos.getX(), blockPos.getZ());
                float step = rainConf.rainFogApplySpeed * deltaTracker.getRealtimeDeltaTicks() * 0.01f;
                currentRainFactor = approach(currentRainFactor, clientLevel.isRaining() ? 1.0f : 0.0f, step);
                currentIndoorFactor = approach(currentIndoorFactor, skylight ? 0.0f : 1.0f, step);

                float rainStartPerc = Mth.lerp(currentIndoorFactor, rainConf.rainStart, rainConf.rainStartIndoor);
                float envStartPerc = Mth.lerp(currentRainFactor, config.overworldStart, rainStartPerc);
                float envEndPerc = Mth.lerp(currentRainFactor, config.overworldEnd, rainConf.rainEnd);
                float cloudEndClear = Minecraft.getInstance().options.cloudRange().get() * 16.0f;
                float cloudEndRain = cloudEndClear * rainConf.rainEnd / config.overworldEnd;
                fogData.environmentalStart = viewDistance * envStartPerc * 0.01f;
                fogData.environmentalEnd = viewDistance * envEndPerc * 0.01f;
                fogData.cloudEnd = Mth.lerp(currentRainFactor, cloudEndClear, cloudEndRain);
            }
            else
            {
                fogData.environmentalStart = viewDistance * config.overworldStart * 0.01f;
                fogData.environmentalEnd = viewDistance * config.overworldEnd * 0.01f;
                fogData.cloudEnd = Minecraft.getInstance().options.cloudRange().get() * 16.0f;
            }
            fogData.skyEnd = Math.min(fogData.environmentalEnd, viewDistance);
        }
        else if (config.netherToggle && clientLevel.dimension() == Level.NETHER)
        {
            fogData.environmentalStart = viewDistance * config.netherStart * 0.01f;
            fogData.environmentalEnd = viewDistance * config.netherEnd * 0.01f;
            fogData.cloudEnd = fogData.environmentalEnd;
            fogData.skyEnd = fogData.environmentalEnd;
        }
        else if (config.endToggle && clientLevel.dimension() == Level.END)
        {
            fogData.environmentalStart = viewDistance * config.endStart * 0.01f;
            fogData.environmentalEnd = viewDistance * config.endEnd * 0.01f;
            fogData.cloudEnd = fogData.environmentalEnd;
            fogData.skyEnd = fogData.environmentalEnd;
        }
    }
}
