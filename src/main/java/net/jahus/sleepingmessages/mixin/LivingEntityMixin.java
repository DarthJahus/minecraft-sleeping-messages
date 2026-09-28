package net.jahus.sleepingmessages.mixin;

import net.jahus.sleepingmessages.SleepAnnounce;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * stopSleeping lives on LivingEntity (not ServerPlayer).
 * Only handle server players; skip morning auto-wake (daytime).
 */
@Mixin(LivingEntity.class)
public class LivingEntityMixin {
	@Inject(method = "stopSleeping", at = @At("HEAD"))
	private void sleepingMessages$onStopSleeping(CallbackInfo ci) {
		LivingEntity self = (LivingEntity) (Object) this;
		if (!(self instanceof ServerPlayer player)) {
			return;
		}
		if (!player.isSleeping()) {
			return;
		}
		// Morning / day skip: don't announce (everyone wakes at once)
		if (!player.level().isDarkOutside()) {
			return;
		}
		SleepAnnounce.onPlayerLeftBed(player);
	}
}
