package org.oisin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.Direction;
import org.rusherhack.client.api.events.client.EventUpdate;
import org.rusherhack.client.api.feature.module.ModuleCategory;
import org.rusherhack.client.api.feature.module.ToggleableModule;
import org.rusherhack.client.api.utils.ChatUtils;
import org.rusherhack.core.event.subscribe.Subscribe;
import org.rusherhack.core.setting.BooleanSetting;
import org.rusherhack.core.setting.NumberSetting;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;

/**
 * AutoPumpkin
 *
 * Automatically shears nearby pumpkins, with the option to silently swap to shears if they are in inventory.
 *
 * @author oisin404
 */
public class AutoPumpkin extends ToggleableModule {

	/**
	 * Settings
	 */
	private final NumberSetting<Float> range = new NumberSetting<>("Range", 3f, 3f, 5f)
			.incremental(0.5);
	private final BooleanSetting silent = new BooleanSetting("Silent", "Silently swaps to shears", true);

	/**
	 * Constructor
	 */
	public AutoPumpkin() {
		super("AutoPumpkin", "Automatically shears nearby pumpkins", ModuleCategory.CLIENT);

		// Register settings
		this.registerSettings(this.range, this.silent);
	}

	/**
	 * Check if an item is a pair of shears.
	 */
	private boolean isShears(ItemStack item) {
		return item.getItem() == Items.SHEARS;
	}

	/**
	 * Get the slot of the shears in the inventory.
	 */
	private int getShearsInInventory() {
		for (int i = 0; i < mc.player.getInventory().items.size(); i++) {
			ItemStack stack = mc.player.getInventory().items.get(i);
			if (isShears(stack)) {
				return i;
			}
		}
		return -1;
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

		// Check if the player is holding shears or needs to silent swap
		if (!isShears(mc.player.getMainHandItem())) {
			if (silent.getValue()) {
				int shearsSlot = getShearsInInventory();
				if (shearsSlot != -1) {
					// Swap to shears silently
					mc.setScreen(new InventoryScreen(mc.player));
					mc.gameMode.handleInventoryMouseClick(mc.player.containerMenu.containerId, shearsSlot, mc.player.getInventory().selected, ClickType.SWAP, mc.player);
					mc.setScreen(null);
				} else {
					ChatUtils.print("No shears in inventory.");
					this.setToggled(false);  // Disable module if no shears are available
					return;
				}
			} else {
				ChatUtils.print("No shears in hand.");
				this.setToggled(false);
				return;
			}
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
