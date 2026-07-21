package org.bleachhack.mixin;

import org.bleachhack.module.Module;
import org.bleachhack.module.ModuleManager;
import org.bleachhack.module.mods.BetterCamera;
import org.bleachhack.module.mods.NoRender;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.block.enums.CameraSubmersionType;

@Mixin(Camera.class)
public class MixinCamera {

	@Unique private boolean bypassCameraClip;

	@Shadow private float clipToSpace(float desiredCameraDistance) { return 0; }
	@Shadow protected void setPos(double x, double y, double z) {}

	@Inject(method = "getSubmersionType", at = @At("HEAD"), cancellable = true)
	private void getSubmergedFluidState(CallbackInfoReturnable<CameraSubmersionType> ci) {
		if (ModuleManager.getModule(NoRender.class).isOverlayToggled(3)) {
			ci.setReturnValue(CameraSubmersionType.NONE);
		}
	}

	@Inject(method = "clipToSpace", at = @At("HEAD"), cancellable = true)
	private void onClipToSpace(float desiredCameraDistance, CallbackInfoReturnable<Float> info) {
		if (bypassCameraClip) {
			bypassCameraClip = false;
		} else {
			Module betterCamera = ModuleManager.getModule(BetterCamera.class);

			if (betterCamera.isEnabled()) {
				if (betterCamera.getSetting(0).asToggle().getState()) {
					info.setReturnValue(betterCamera.getSetting(1).asToggle().getState()
							? betterCamera.getSetting(1).asToggle().getChild(0).asSlider().getValueFloat() : desiredCameraDistance);
				} else if (betterCamera.getSetting(1).asToggle().getState()) {
					bypassCameraClip = true;
					info.setReturnValue(clipToSpace(betterCamera.getSetting(1).asToggle().getChild(0).asSlider().getValueFloat()));
				}
			}
		}
	}

	@Inject(method = "setPos(DDD)V", at = @At("HEAD"), cancellable = true)
	private void onSetPos(double x, double y, double z, CallbackInfo ci) {
		if (BetterCamera.isUnderFeetEnabled()) {
			MinecraftClient mc = MinecraftClient.getInstance();
			if (mc.player != null && mc.player.getVehicle() == null) {
				double feetY = mc.player.getY();
				double offset = BetterCamera.getOffset();
				double newY = feetY + offset;
				this.setPos(x, newY, z);
				ci.cancel();
			}
		}
	}
}
