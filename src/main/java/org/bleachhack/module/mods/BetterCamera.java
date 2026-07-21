package org.bleachhack.module.mods;

import org.bleachhack.module.Module;
import org.bleachhack.module.ModuleCategory;
import org.bleachhack.setting.module.SettingSlider;
import org.bleachhack.setting.module.SettingToggle;

public class BetterCamera extends Module {

	private static BetterCamera INSTANCE;

	public BetterCamera() {
		super("BetterCamera", KEY_UNBOUND, ModuleCategory.RENDER, "Improves the 3rd person camera.",
				new SettingToggle("CameraClip", true).withDesc("Makes the camera clip into walls."),
				new SettingToggle("Distance", true).withDesc("Sets a custom camera distance.").withChildren(
						new SettingSlider("Distance", 0.5, 15, 4, 1).withDesc("The desired camera distance.")),
				new SettingToggle("UnderFeet", false).withDesc("Positions the camera under the player's feet.").withChildren(
						new SettingSlider("Offset", -10.0, 10.0, -1.0, 1).withDesc("Vertical offset from the feet.")));
		INSTANCE = this;
	}

	public static boolean isActive() {
		return INSTANCE != null && INSTANCE.isEnabled();
	}

	public static boolean isDistanceEnabled() {
		return isActive() && INSTANCE.getSetting(1).asToggle().getState();
	}

	public static double getDistance() {
		if (!isDistanceEnabled()) return 4.0;
		return INSTANCE.getSetting(1).asToggle().getChild(0).asSlider().getValue();
	}

	public static boolean isUnderFeetEnabled() {
		return isActive() && INSTANCE.getSetting(2).asToggle().getState();
	}

	public static double getOffset() {
		if (!isUnderFeetEnabled()) return 0.0;
		return INSTANCE.getSetting(2).asToggle().getChild(0).asSlider().getValue();
	}
}
