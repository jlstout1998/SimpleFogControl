package de.draradech.simplefog.mixin;

import de.draradech.simplefog.SimpleFogMain;
import de.draradech.simplefog.util.DimensionClassifier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.FogRenderer;
import net.minecraft.world.level.Level;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(FogRenderer.class)
public class FogRendererMixin {
    private boolean active() {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) return false;
        if (!SimpleFogMain.config.overworldToggle && DimensionClassifier.isOverworldLike(level)) return false;
        if (!SimpleFogMain.config.netherToggle && DimensionClassifier.isNetherLike(level)) return false;
        if (!SimpleFogMain.config.endToggle && DimensionClassifier.isEndLike(level)) return false;
        return true;
    }

    @Redirect(method = "setupFog", at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/fog/FogData;renderDistanceStart:F", opcode = Opcodes.PUTFIELD))
    private void modifyRenderDistanceStart(FogData data, float renderDistanceStart) {
        if (active()) data.renderDistanceStart = 1e5f;
        else data.renderDistanceStart = renderDistanceStart;
    }

    @Redirect(method = "setupFog", at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/fog/FogData;renderDistanceEnd:F", opcode = Opcodes.PUTFIELD))
    private void modifyRenderDistanceEnd(FogData data, float renderDistanceEnd) {
        if (active()) data.renderDistanceEnd = 1e5f;
        else data.renderDistanceEnd = renderDistanceEnd;
    }
}
