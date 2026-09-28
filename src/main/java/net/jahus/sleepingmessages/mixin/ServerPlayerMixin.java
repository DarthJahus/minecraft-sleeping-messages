package net.jahus.sleepingmessages.mixin;

import com.mojang.datafixers.util.Either;
import net.jahus.sleepingmessages.SleepAnnounce;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.attribute.BedRule;
import net.minecraft.world.entity.player.Player.BedSleepingProblem;
import net.minecraft.world.level.block.AbstractBedBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayer.class)
public class ServerPlayerMixin {
	@Inject(method = "startSleepInBed", at = @At("RETURN"))
	private void sleepingMessages$onSleep(
			AbstractBedBlock bedBlock,
			BlockState state,
			BedRule bedRule,
			BlockPos bedPos,
			CallbackInfoReturnable<Either<BedSleepingProblem, ?>> cir) {
		Either<BedSleepingProblem, ?> result = cir.getReturnValue();
		if (result == null || result.left().isPresent()) {
			return;
		}
		SleepAnnounce.onPlayerStartedSleeping((ServerPlayer) (Object) this);
	}
}
