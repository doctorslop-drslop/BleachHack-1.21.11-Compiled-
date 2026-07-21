/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDev/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package org.bleachhack.module.mods;

import org.bleachhack.event.events.EventTick;
import org.bleachhack.eventbus.BleachSubscribe;
import org.bleachhack.module.Module;
import org.bleachhack.module.ModuleCategory;
import org.bleachhack.setting.module.SettingMode;
import org.bleachhack.setting.module.SettingSlider;

public class Flight extends Module {

	public Flight() {
		super("Flight", KEY_UNBOUND, ModuleCategory.MOVEMENT, "Perfect flight for singleplayer.",
				new SettingMode("Mode", "Vanilla", "Static").withDesc("Flight mode."),
				new SettingSlider("Speed", 0.1, 20, 1, 1).withDesc("Flight speed."));
	}

	@Override
	public void onDisable(boolean inWorld) {
		if (inWorld && mc.player != null) {
			if (mc.interactionManager != null && !mc.interactionManager.getCurrentGameMode().isCreative() && !mc.player.isSpectator()) {
				mc.player.getAbilities().allowFlying = false;
				mc.player.getAbilities().flying = false;
			}
			mc.player.getAbilities().setFlySpeed(0.05f);
		}
		super.onDisable(inWorld);
	}

	@BleachSubscribe
	public void onTick(EventTick event) {
		if (mc.player == null || mc.player.input == null) return;

		float speed = getSetting(1).asSlider().getValueFloat();
		mc.player.fallDistance = 0;

		if (getSetting(0).asMode().getMode() == 0) {
			mc.player.getAbilities().allowFlying = true;
			mc.player.getAbilities().flying = true;
			mc.player.getAbilities().setFlySpeed(speed / 10f);

		} else if (getSetting(0).asMode().getMode() == 1) {
			if (mc.interactionManager != null && !mc.interactionManager.getCurrentGameMode().isCreative() && !mc.player.isSpectator()) {
				mc.player.getAbilities().flying = false;
			}

			mc.player.setVelocity(0, 0, 0);

			float forward = mc.player.input.getMovementInput().y;
			float strafe = mc.player.input.getMovementInput().x;
			float up = (mc.player.input.playerInput.jump() ? 1 : 0) - (mc.player.input.playerInput.sneak() ? 1 : 0);
			if (forward == 0 && strafe == 0 && up == 0) {
				return;
			}

			double dist = Math.sqrt(forward * forward + strafe * strafe);
			if (dist > 0) {
				forward /= dist;
				strafe /= dist;
			}

			double rad = Math.toRadians(mc.player.getYaw());
			double sin = Math.sin(rad);
			double cos = Math.cos(rad);

			double velX = (forward * cos - strafe * sin) * speed;
			double velZ = (forward * sin + strafe * cos) * speed;
			double velY = up * speed;

			mc.player.setVelocity(velX, velY, velZ);
		}
	}
}
