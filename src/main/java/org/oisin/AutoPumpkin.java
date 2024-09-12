package org.oisin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.rusherhack.client.api.RusherHackAPI;
import org.rusherhack.client.api.events.client.EventUpdate;
import org.rusherhack.client.api.events.render.EventRender2D;
import org.rusherhack.client.api.events.render.EventRender3D;
import org.rusherhack.client.api.feature.module.ModuleCategory;
import org.rusherhack.client.api.feature.module.ToggleableModule;
import org.rusherhack.client.api.render.IRenderer2D;
import org.rusherhack.client.api.render.IRenderer3D;
import org.rusherhack.client.api.render.font.IFontRenderer;
import org.rusherhack.client.api.setting.BindSetting;
import org.rusherhack.client.api.setting.ColorSetting;
import org.rusherhack.client.api.utils.ChatUtils;
import org.rusherhack.client.api.utils.WorldUtils;
import org.rusherhack.core.bind.key.NullKey;
import org.rusherhack.core.event.subscribe.Subscribe;
import org.rusherhack.core.setting.BooleanSetting;
import org.rusherhack.core.setting.NumberSetting;
import org.rusherhack.core.setting.StringSetting;
import org.rusherhack.core.utils.ColorUtils;

import java.awt.*;

/**
 * AutoPumpkin
 *
 * @author oisin404
 */

public class AutoPumpkin extends ToggleableModule {
	
	/**
	 * Settings
	 */
	private final NumberSetting<Float> range = new NumberSetting<>("Range", 3f, 0f, 6f)

	/**
	 * Constructor
	 */

	public ExampleModule() {
		super("AutoPumpkin", "Automatically shears nearby pumpkins.", ModuleCategory.CLIENT);
		
		//register settings
		this.registerSettings(
				this.range
		);

		public void onUpdate(EventInteract event) {
			//check if the module is enabled
			if (!this.isToggled()) {
				return;
			}

			try {
				if (!(mc.player.getMainHandItem().getItem() == Items.SHEARS)) {
					return;
				}
				ChatUtils.print("Shears Detected")
			}
		}
}
