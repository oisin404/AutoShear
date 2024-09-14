package org.oisin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
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

import net.minecraft.world.item.Items; // Only added this to use Items.SHEARS

/**
 * AutoPumpkin
 *
 * Automatically shears nearby pumpkins.
 *
 * @author oisin404
 */

public class AutoPumpkin extends ToggleableModule {

	/**
	 * Settings
	 */
	private final NumberSetting<Float> range = new NumberSetting<>("Range", 3f, 0f, 6f)
		.incremental(0.5);

	/**
	 * Constructor
	 */
	public AutoPumpkin() {
		super("AutoPumpkin", "Automatically shears nearby pumpkins.", ModuleCategory.CLIENT);

		// Register settings
		this.registerSettings(this.range);
	}

	/**
	 * Event listener for updating
	 */
	@Subscribe
	public void onUpdate(EventUpdate event) {
		// Check if the module is enabled
		if (!this.isToggled()) {
			return;
		}

		// Check if the player is holding shears
		if (mc.player.getMainHandItem().getItem() != Items.SHEARS) {
			return;
		}

		BlockPos playerPos = mc.player.blockPosition();
		float rangeValue = this.range.getValue();
		for (int x = (int) -rangeValue; x <= rangeValue; x++) {
			for (int y = (int) -rangeValue; y <= rangeValue; y++) {
				for (int z = (int) -rangeValue; z <= rangeValue; z++) {
					BlockPos blockPos = playerPos.offset(x, y, z);
					if (mc.level.getBlockState(blockPos).getBlock() == Blocks.PUMPKIN) {



					}
				}
			}
		}
	}
}
