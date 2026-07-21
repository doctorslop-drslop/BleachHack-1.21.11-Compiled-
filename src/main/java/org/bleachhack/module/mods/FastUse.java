package org.bleachhack.module.mods;

import java.util.Set;

import org.bleachhack.event.events.EventTick;
import org.bleachhack.eventbus.BleachSubscribe;
import org.bleachhack.module.Module;
import org.bleachhack.module.ModuleCategory;
import org.bleachhack.setting.module.SettingMode;
import org.bleachhack.setting.module.SettingSlider;
import org.bleachhack.setting.module.SettingToggle;

import com.google.common.collect.Sets;

import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.util.Hand;

public class FastUse extends Module {

	private static final Set<Item> THROWABLE = Sets.newHashSet(
			Items.SNOWBALL, Items.EGG, Items.EXPERIENCE_BOTTLE,
			Items.ENDER_EYE, Items.ENDER_PEARL, Items.SPLASH_POTION, Items.LINGERING_POTION);

	public FastUse() {
		this(new SettingMode("Mode", "Single", "Multi").withDesc("Whether to throw once per tick or multiple times."));
	}

	private FastUse(SettingMode mode) {
		super("FastUse", KEY_UNBOUND, ModuleCategory.PLAYER, "Allows you to use items faster.",
				mode,
				new SettingSlider("Multi", 1, 200, 20, 0).withDesc("How many items to use per tick if on multi mode.")
						.visibleWhen(() -> mode.getMode() == 1),
				new SettingToggle("Throwables Only", true).withDesc("Only uses throwables.").withChildren(
						new SettingToggle("XP Only", false).withDesc("Only uses XP bottles.")));
	}

	@BleachSubscribe
	public void onTick(EventTick event) {
		if (mc.player == null) return;

		boolean isThrowable = THROWABLE.contains(mc.player.getMainHandStack().getItem());
		boolean isXp = mc.player.getMainHandStack().getItem() == Items.EXPERIENCE_BOTTLE;

		if (getSetting(2).asToggle().getState()) {
			if (!(isThrowable && (!getSetting(2).asToggle().getChild(0).asToggle().getState() || isXp))) {
				return;
			}
		}

		mc.itemUseCooldown = 0;

		if (mc.options.useKey.isPressed()) {
			int mode = getSetting(0).asMode().getMode();
			int packets = mode == 1 ? getSetting(1).asSlider().getValueInt() : 1;

			if (isThrowable) {
				for (int i = 0; i < packets; i++) {
					mc.player.networkHandler.sendPacket(new PlayerInteractItemC2SPacket(
							Hand.MAIN_HAND,
							0,
							mc.player.getYaw(),
							mc.player.getPitch()
					));
				}
			} else {
				for (int i = 0; i < packets; i++) {
					mc.doItemUse();
				}
			}
		}
	}
}
