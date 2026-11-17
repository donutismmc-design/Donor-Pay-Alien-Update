package com.donorpay.mixin;

import com.donorpay.AlienMode;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AvatarRenderer.class)
public class AvatarRendererMixin {

    // swap the skin texture
    @Inject(method = "getTextureLocation(Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;)Lnet/minecraft/resources/Identifier;",
            at = @At("HEAD"), cancellable = true)
    private void donorpay$alienSkin(AvatarRenderState state, CallbackInfoReturnable<Identifier> cir) {
        Identifier tex = AlienMode.textureFor(state.id);
        if (tex != null) cir.setReturnValue(tex);
    }

    // make aliens tall and skinny
    @Inject(method = "scale(Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;)V",
            at = @At("RETURN"))
    private void donorpay$alienShape(AvatarRenderState state, PoseStack poseStack, CallbackInfo ci) {
        if (AlienMode.textureFor(state.id) != null) poseStack.scale(0.80f, 1.14f, 0.80f);
    }
}
