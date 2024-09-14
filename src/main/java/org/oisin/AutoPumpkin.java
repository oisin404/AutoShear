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
	private final BooleanSetting silent = new BooleanSetting("SilentSwap", "Silently swaps to shears", false);

	/**
	 * Constructor
	 */
	public AutoPumpkin() {
		super("AutoPumpkin", "Automatically shears nearby pumpkins", ModuleCategory.CLIENT);
		this.registerSettings(this.range, this.silent);
	}

	/**
	 * check shears
	 */
	private boolean isShears(ItemStack item) {
		return item.getItem() == Items.SHEARS;
	}

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
	 * silent
	 */
	private void silentSwap(int itemSlot) {
		if (mc.screen == null) {
			mc.setScreen(new InventoryScreen(mc.player));
			return;
		}

		ChatUtils.print("Performing silent swap.");
		mc.gameMode.handleInventoryMouseClick(mc.player.containerMenu.containerId, itemSlot, mc.player.getInventory().selected, ClickType.SWAP, mc.player);
		mc.setScreen(null);
		ChatUtils.print("Swap performed. Closing inventory.");
	}


	@Subscribe
	public void onUpdate(EventUpdate event) {
		// Check if the module is enabled
		if (!this.isToggled()) {
			return;
		}

		// check mainhand
		if (!isShears(mc.player.getMainHandItem())) {
			if (silent.getValue()) {
				int shearsSlot = getShearsInInventory();
				if (shearsSlot != -1) {
					// silent swap
					silentSwap(shearsSlot);
				} else {
					ChatUtils.print("No shears in inventory.");
					this.setToggled(false);
					return;
				}
			} else {
				return;
			}
		}

		BlockPos playerPos = mc.player.blockPosition();
		float rangeValue = this.range.getValue();
		BlockPos closestPumpkin = null;
		double closestDistance = Double.MAX_VALUE;

		for (int x = (int) -rangeValue; x <= rangeValue; x++) {
			for (int y = (int) -rangeValue; y <= rangeValue; y++) {
				for (int z = (int) -rangeValue; z <= rangeValue; z++) {
					BlockPos blockPos = playerPos.offset(x, y, z);

					// check pumpkin
					if (mc.level.getBlockState(blockPos).getBlock() == Blocks.PUMPKIN) {
						double distance = blockPos.distSqr(playerPos);

						if (distance < closestDistance) {
							closestDistance = distance;
							closestPumpkin = blockPos;
						}
					}
				}
			}
		}

		// carve
		if (closestPumpkin != null) {
			mc.gameMode.useItemOn(mc.player, InteractionHand.MAIN_HAND,
					new BlockHitResult(
							new Vec3(closestPumpkin.getX() + 0.5, closestPumpkin.getY() + 0.5, closestPumpkin.getZ() + 0.5),
							Direction.UP, closestPumpkin, false
					)
			);
		}
	}
}
