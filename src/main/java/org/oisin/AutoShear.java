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
import org.rusherhack.client.api.RusherHackAPI;
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
	private final BooleanSetting rotate = new BooleanSetting("Rotate", "Rotates to target to bypass Anti-Cheat", true);
	private final BooleanSetting shearSheep = new BooleanSetting("ShearSheep", "Shear nearby sheep", false);
	private final BooleanSetting carvePumpkins = new BooleanSetting("CarvePumpkins", "Carve nearby pumpkins", false);
	private final BooleanSetting harvestBeehives = new BooleanSetting("HarvestBeehives", "Harvest honeycombs from nearby beehives/nests", false);

	/**
	 * Constructor
	 */
	public AutoShear() {
		super("AutoShear", "Automatically shears nearby pumpkins, sheep, or beehives/nests", ModuleCategory.CLIENT);
		this.registerSettings(this.range, this.shearSheep, this.carvePumpkins, this.harvestBeehives, this.rotate);
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
	private boolean isSmoked(BlockPos hivePos) {
		// check for campfire
		for (int i = 1; i <= 5; i++) {
			BlockPos belowPos = hivePos.below(i);
			BlockState belowState = mc.level.getBlockState(belowPos);

			if (belowState.getBlock() == Blocks.CAMPFIRE || belowState.getBlock() == Blocks.SOUL_CAMPFIRE) {
				return true;
			}
		}
		return false;
	}

	@Subscribe
	public void onUpdate(EventUpdate event) {
		// check enabled
		if (!this.isToggled()) {
			return;
		}

		// mainhand shears
		if (!isShears(mc.player.getMainHandItem())) {
			return;
		}

		BlockPos playerPos = mc.player.blockPosition();
		float rangeValue = this.range.getValue();
		double closestDistance;

		// check sheep
		if (shearSheep.getValue()) {
			LivingEntity target = null;
			closestDistance = Double.MAX_VALUE;

			// loop entitiys
			for (Entity entity : mc.level.getEntities(mc.player, mc.player.getBoundingBox().inflate(rangeValue))) {
				// check if sheep
				if (entity instanceof LivingEntity livingEntity && entity.getType().equals(EntityType.SHEEP)) {
					double distance = mc.player.distanceTo(livingEntity);

					// closest sheep target
					if (distance < closestDistance) {
						closestDistance = distance;
						target = livingEntity;
					}
				}
			}

			// rotate and shear
			if (target != null) {
				if (this.rotate.getValue()) {
					// rotate
					RusherHackAPI.getRotationManager().updateRotation(target);
				}
				// shear
				mc.gameMode.interact(mc.player, target, InteractionHand.MAIN_HAND);
			}
		}

		// check pumpkin
		if (carvePumpkins.getValue()) {
			BlockPos closestPumpkin = null;
			closestDistance = Double.MAX_VALUE;  // reuse closestDistance

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
				if (this.rotate.getValue()) {
					// rotate
					Vec3 targetVec = new Vec3(closestPumpkin.getX() + 0.5, closestPumpkin.getY() + 0.5, closestPumpkin.getZ() + 0.5);
					RusherHackAPI.getRotationManager().updateRotation(BlockPos.containing(targetVec));
				}
				// carve
				mc.gameMode.useItemOn(mc.player, InteractionHand.MAIN_HAND,
						new BlockHitResult(
								new Vec3(closestPumpkin.getX() + 0.5, closestPumpkin.getY() + 0.5, closestPumpkin.getZ() + 0.5),
								Direction.UP, closestPumpkin, false
						)
				);
			}
		}

		// bee logic
		if (harvestBeehives.getValue()) {
			BlockPos closestHive = null;
			closestDistance = Double.MAX_VALUE;  // reuse closetDistance

			for (int x = (int) -rangeValue; x <= rangeValue; x++) {
				for (int y = (int) -rangeValue; y <= rangeValue; y++) {
					for (int z = (int) -rangeValue; z <= rangeValue; z++) {
						BlockPos blockPos = playerPos.offset(x, y, z);
						BlockState blockState = mc.level.getBlockState(blockPos);

						// beehive/next honey 5
						if ((blockState.getBlock() == Blocks.BEEHIVE || blockState.getBlock() == Blocks.BEE_NEST)
								&& blockState.getValue(BeehiveBlock.HONEY_LEVEL) == 5
								&& isSmoked(blockPos)) {
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
				if (this.rotate.getValue()) {
					// rotate
					Vec3 targetVec = new Vec3(closestHive.getX() + 0.5, closestHive.getY() + 0.5, closestHive.getZ() + 0.5);
					RusherHackAPI.getRotationManager().updateRotation(BlockPos.containing(targetVec));
				}
				// harvest honeycomb
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
