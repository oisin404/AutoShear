package org.oisin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BeehiveBlock;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.Direction;
import org.rusherhack.client.api.events.client.EventUpdate;
import org.rusherhack.client.api.feature.module.ModuleCategory;
import org.rusherhack.client.api.feature.module.ToggleableModule;
import org.rusherhack.core.event.subscribe.Subscribe;
import org.rusherhack.core.setting.BooleanSetting;
import org.rusherhack.core.setting.NumberSetting;

/**
 * AutoShear
 *
 * Automatically shears nearby pumpkins, sheep, or beehives/nests depending on selected targets.
 *
 * @author oisin404
 */
public class AutoShear extends ToggleableModule {

	/**
	 * Settings
	 */
	private final NumberSetting<Float> range = new NumberSetting<>("Range", 3f, 3f, 5f)
			.incremental(0.5);
	private final BooleanSetting shearSheep = new BooleanSetting("ShearSheep", "Shear nearby sheep", true);
	private final BooleanSetting carvePumpkins = new BooleanSetting("CarvePumpkins", "Carve nearby pumpkins", true);
	private final BooleanSetting harvestBeehives = new BooleanSetting("HarvestBeehives", "Harvest honeycombs from nearby beehives/nests", true);

	/**
	 * Constructor
	 */
	public AutoShear() {
		super("AutoShear", "Automatically shears nearby pumpkins, sheep, or beehives/nests", ModuleCategory.CLIENT);
		this.registerSettings(this.range, this.shearSheep, this.carvePumpkins, this.harvestBeehives);
	}

	/**
	 * Check if item is shears
	 */
	private boolean isShears(ItemStack item) {
		return item.getItem() == Items.SHEARS;
	}

	/**
	 * Check if there is a campfire under the hive/nest and within 5 blocks
	 */
	private boolean hasCampfireUnder(BlockPos hivePos) {
		// Check beneath the hive/nest up to 5 blocks down
		for (int i = 1; i <= 5; i++) {
			BlockPos belowPos = hivePos.below(i);
			BlockState belowState = mc.level.getBlockState(belowPos);

			// Check if the block is a campfire or soul campfire
			if (belowState.getBlock() == Blocks.CAMPFIRE || belowState.getBlock() == Blocks.SOUL_CAMPFIRE) {
				return true;
			}
		}
		return false;
	}

	@Subscribe
	public void onUpdate(EventUpdate event) {
		// Check if the module is enabled
		if (!this.isToggled()) {
			return;
		}

		// Check if main hand item is shears
		if (!isShears(mc.player.getMainHandItem())) {
			return;
		}

		BlockPos playerPos = mc.player.blockPosition();
		float rangeValue = this.range.getValue();

		// Check for sheep
		if (shearSheep.getValue()) {
			for (Entity entity : mc.level.getEntities(mc.player, mc.player.getBoundingBox().inflate(rangeValue))) {
				if (entity instanceof LivingEntity livingEntity && entity.getType().equals(EntityType.SHEEP)) {
					mc.gameMode.interact(mc.player, livingEntity, InteractionHand.MAIN_HAND);
				}
			}
		}

		// Check for pumpkins
		if (carvePumpkins.getValue()) {
			BlockPos closestPumpkin = null;
			double closestDistance = Double.MAX_VALUE;

			for (int x = (int) -rangeValue; x <= rangeValue; x++) {
				for (int y = (int) -rangeValue; y <= rangeValue; y++) {
					for (int z = (int) -rangeValue; z <= rangeValue; z++) {
						BlockPos blockPos = playerPos.offset(x, y, z);

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

			if (closestPumpkin != null) {
				mc.gameMode.useItemOn(mc.player, InteractionHand.MAIN_HAND,
						new BlockHitResult(
								new Vec3(closestPumpkin.getX() + 0.5, closestPumpkin.getY() + 0.5, closestPumpkin.getZ() + 0.5),
								Direction.UP, closestPumpkin, false
						)
				);
			}
		}

		// Check for beehives/nests with honey level 5 and a campfire below
		if (harvestBeehives.getValue()) {
			BlockPos closestHive = null;
			double closestDistance = Double.MAX_VALUE;

			for (int x = (int) -rangeValue; x <= rangeValue; x++) {
				for (int y = (int) -rangeValue; y <= rangeValue; y++) {
					for (int z = (int) -rangeValue; z <= rangeValue; z++) {
						BlockPos blockPos = playerPos.offset(x, y, z);
						BlockState blockState = mc.level.getBlockState(blockPos);

						// Check if it's a beehive or bee nest and honey level is 5
						if ((blockState.getBlock() == Blocks.BEEHIVE || blockState.getBlock() == Blocks.BEE_NEST)
								&& blockState.getValue(BeehiveBlock.HONEY_LEVEL) == 5
								&& hasCampfireUnder(blockPos)) {
							double distance = blockPos.distSqr(playerPos);

							if (distance < closestDistance) {
								closestDistance = distance;
								closestHive = blockPos;
							}
						}
					}
				}
			}

			if (closestHive != null) {
				mc.gameMode.useItemOn(mc.player, InteractionHand.MAIN_HAND,
						new BlockHitResult(
								new Vec3(closestHive.getX() + 0.5, closestHive.getY() + 0.5, closestHive.getZ() + 0.5),
								Direction.UP, closestHive, false
						)
				);
			}
		}
	}
}
