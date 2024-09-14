package org.oisin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.Direction;
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
import net.minecraft.world.item.Items;

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
	private final NumberSetting<Float> range = new NumberSetting<>("Range", 3f, 3f, 5f)
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
		BlockPos closestPumpkin = null;
		double closestDistance = Double.MAX_VALUE;

		// Loop through blocks in a cubic area around the player, within the specified range
		for (int x = (int) -rangeValue; x <= rangeValue; x++) {
			for (int y = (int) -rangeValue; y <= rangeValue; y++) {
				for (int z = (int) -rangeValue; z <= rangeValue; z++) {
					BlockPos blockPos = playerPos.offset(x, y, z);

					// Check if the block is a pumpkin
					if (mc.level.getBlockState(blockPos).getBlock() == Blocks.PUMPKIN) {
						double distance = blockPos.distSqr(playerPos);

						// If this is the closest pumpkin so far, update closestPumpkin
						if (distance < closestDistance) {
							closestDistance = distance;
							closestPumpkin = blockPos;
						}
					}
				}
			}
		}

		// If a pumpkin was found, interact with it to carve it
		if (closestPumpkin != null) {
			// ChatUtils.print("Carving Pumpkin at " + closestPumpkin.toShortString());

			// Simulate right-clicking the pumpkin with shears to carve it
			mc.gameMode.useItemOn(mc.player, InteractionHand.MAIN_HAND,
					new BlockHitResult(
							new Vec3(closestPumpkin.getX() + 0.5, closestPumpkin.getY() + 0.5, closestPumpkin.getZ() + 0.5),
							Direction.UP, closestPumpkin, false
					)
			);
		}
	}
}
