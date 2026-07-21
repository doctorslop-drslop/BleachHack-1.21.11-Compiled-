package org.bleachhack.module.mods;

import org.bleachhack.event.events.EventClientMove;
import org.bleachhack.event.events.EventTick;
import org.bleachhack.eventbus.BleachSubscribe;
import org.bleachhack.module.Module;
import org.bleachhack.module.ModuleCategory;
import org.bleachhack.setting.module.SettingMode;
import org.bleachhack.setting.module.SettingSlider;
import org.bleachhack.setting.module.SettingToggle;

import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.util.math.Vec3d;

public class Speed extends Module {

	private boolean jumping;

	public Speed() {
		super("Speed", KEY_UNBOUND, ModuleCategory.MOVEMENT, "Allows you to go faster.",
				new SettingMode("Mode", "StrafeHop", "Strafe", "OnGround", "MiniHop", "Bhop").withDesc("Speed mode."),
				new SettingSlider("Speed", 0.1, 10.0, 2.0, 2).withDesc("Speed multiplier."),
				new SettingToggle("NoInertia", false).withDesc("Prevents you from moving forcefully."));
	}

	@BleachSubscribe
	public void onTick(EventTick event) {
		if (mc.player == null || mc.options.sneakKey.isPressed()) return;

		int mode = getSetting(0).asMode().getMode();
		double speed = getSetting(1).asSlider().getValue();

		if (mode <= 1) {
			if (mc.player.forwardSpeed != 0 || mc.player.sidewaysSpeed != 0) {
				if (!mc.player.isSprinting()) {
					mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.START_SPRINTING));
				}

				mc.player.setVelocity(new Vec3d(0, mc.player.getVelocity().y, 0));
				
				float strafeSpeed = (float) (speed * 0.135);
				mc.player.updateVelocity(strafeSpeed, new Vec3d(mc.player.sidewaysSpeed, 0, mc.player.forwardSpeed));
				
				double vel = Math.abs(mc.player.getVelocity().getX()) + Math.abs(mc.player.getVelocity().getZ());
				
				if (mode == 0 && vel >= 0.12 && mc.player.isOnGround()) {
					mc.player.updateVelocity(vel >= 0.3 ? 0.0f : 0.15f, new Vec3d(mc.player.sidewaysSpeed, 0, mc.player.forwardSpeed));
					mc.player.jump();
				}
			}
		} else if (mode == 2) {
			if (mc.options.jumpKey.isPressed() || mc.player.fallDistance > 0.25) return;
			
			double speeds = 0.85 + speed / 30.0;

			if (jumping && mc.player.getY() >= mc.player.lastY + 0.399994D) {
				mc.player.setVelocity(mc.player.getVelocity().x, -0.9, mc.player.getVelocity().z);
				mc.player.setPos(mc.player.getX(), mc.player.lastY, mc.player.getZ());
				jumping = false;
			}

			if (mc.player.forwardSpeed != 0.0F && !mc.player.horizontalCollision) {
				if (mc.player.verticalCollision) {
					mc.player.setVelocity(mc.player.getVelocity().x * speeds, mc.player.getVelocity().y, mc.player.getVelocity().z * speeds);
					jumping = true;
					mc.player.jump();
				}

				if (jumping && mc.player.getY() >= mc.player.lastY + 0.399994D) {
					mc.player.setVelocity(mc.player.getVelocity().x, -100, mc.player.getVelocity().z);
					jumping = false;
				}
			}
		} else if (mode == 3) {
			if (mc.player.horizontalCollision || mc.options.jumpKey.isPressed() || mc.player.forwardSpeed == 0) return;
			
			double speeds = 0.9 + speed / 30.0;
			
			if (mc.player.isOnGround()) {
				mc.player.jump();
			} else if (mc.player.getVelocity().y > 0) {
				mc.player.setVelocity(mc.player.getVelocity().x * speeds, -1, mc.player.getVelocity().z * speeds);
				mc.player.sidewaysSpeed += 1.5F;
			}
		} else if (mode == 4) {
			if (mc.player.forwardSpeed > 0 && mc.player.isOnGround()) {
				double speeds = 0.65 + speed / 30.0;
				
				mc.player.jump();
				mc.player.setVelocity(mc.player.getVelocity().x * speeds, 0.255556, mc.player.getVelocity().z * speeds);
				mc.player.sidewaysSpeed += 3.0F;
				mc.player.jump();
				mc.player.setSprinting(true);
			}
		}
	}

	@BleachSubscribe
	public void onMove(EventClientMove event) {
		if (mc.player == null) return;
		if (mc.player.forwardSpeed == 0 && mc.player.sidewaysSpeed == 0 && getSetting(2).asToggle().getState()) {
			event.setVec(new Vec3d(0, event.getVec().y, 0));
		}
	}
}
