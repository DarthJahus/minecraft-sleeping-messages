package net.jahus.sleepingmessages;

import net.jahus.sleepingmessages.config.ModConfig;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;

/**
 * Build and broadcast the sleeping message for all currently sleeping players.
 */
public final class SleepAnnounce {
	private static long lastBroadcastMs;

	private SleepAnnounce() {}

	
	public static void onPlayerLeftBed(ServerPlayer player) {
		ModConfig cfg = SleepingMessages.CONFIG;
		if (cfg == null || !cfg.enableLeftBedMessage) return;

		MinecraftServer server = player.level().getServer();
		if (server == null) return;

		// Alone on the server: a leave-bed message makes no sense
		if (server.getPlayerList().getPlayers().size() <= 1) return;

		MutableComponent message = Component.literal("")
			.append(playerName(cfg, player))
			.append(colored(cfg, " " + cfg.messageLeftBed));

		server.getPlayerList().broadcastSystemMessage(message, false);
	}

	public static void onPlayerStartedSleeping(ServerPlayer sleeper) {
		ModConfig cfg = SleepingMessages.CONFIG;
		if (cfg == null) return;

		MinecraftServer server = sleeper.level().getServer();
		if (server == null) return;

		// Alone on the server: "[player] [message_sleeping_alone]", or nothing if empty
		if (server.getPlayerList().getPlayers().size() <= 1) {
			if (cfg.messageSleepingAlone.isEmpty()) return;
			MutableComponent alone = Component.literal("")
				.append(playerName(cfg, sleeper))
				.append(colored(cfg, " " + cfg.messageSleepingAlone));
			server.getPlayerList().broadcastSystemMessage(alone, false);
			return;
		}

		long now = System.currentTimeMillis();
		if (cfg.cooldownMs > 0 && now - lastBroadcastMs < cfg.cooldownMs) {
			return;
		}

		List<ServerPlayer> sleeping = new ArrayList<>();
		for (ServerPlayer p : server.getPlayerList().getPlayers()) {
			if (p.isSleeping()) {
				sleeping.add(p);
			}
		}
		// Ensure the trigger is included (isSleeping may lag one tick)
		if (sleeping.stream().noneMatch(p -> p.getUUID().equals(sleeper.getUUID()))) {
			sleeping.add(sleeper);
		}
		if (sleeping.isEmpty()) return;

		lastBroadcastMs = now;
		// Everyone connected is in bed (only reachable with 2+ players: solo is handled above)
		boolean everyone = sleeping.size() >= server.getPlayerList().getPlayers().size();
		MutableComponent message = buildMessage(cfg, sleeping, everyone);
		server.getPlayerList().broadcastSystemMessage(message, false);
	}

	static MutableComponent buildMessage(ModConfig cfg, List<ServerPlayer> sleeping, boolean everyone) {
		int n = sleeping.size();
		MutableComponent msg = Component.literal("");

		if (n == 1) {
			msg.append(playerName(cfg, sleeping.get(0)));
			msg.append(colored(cfg, " " + cfg.messagePart1Singular + " " + cfg.messagePart2));
			return msg;
		}

		if (n == 2) {
			msg.append(playerName(cfg, sleeping.get(0)));
			msg.append(colored(cfg, " " + cfg.joiner + " "));
			msg.append(playerName(cfg, sleeping.get(1)));
			msg.append(colored(cfg, " " + pluralTail(cfg, everyone)));
			return msg;
		}

		if (n == 3) {
			msg.append(playerName(cfg, sleeping.get(0)));
			msg.append(colored(cfg, ", "));
			msg.append(playerName(cfg, sleeping.get(1)));
			msg.append(colored(cfg, " " + cfg.joiner + " "));
			msg.append(playerName(cfg, sleeping.get(2)));
			msg.append(colored(cfg, " " + pluralTail(cfg, everyone)));
			return msg;
		}

		// 4+: first player + joiner + (n-1) + other players
		int others = n - 1;
		msg.append(playerName(cfg, sleeping.get(0)));
		msg.append(colored(cfg, " " + cfg.joiner + " " + others + " " + cfg.msgOtherPlayers));
		msg.append(colored(cfg, " " + pluralTail(cfg, everyone)));
		return msg;
	}

	/**
	 * Text after the names for 2+ players: "[message_part_1_plural] [message_part_2]",
	 * or "[all_players_sleep_message]" when everyone is in bed and that message is set.
	 */
	private static String pluralTail(ModConfig cfg, boolean everyone) {
		if (everyone && !cfg.allPlayersSleepMessage.isEmpty()) {
			return cfg.allPlayersSleepMessage;
		}
		return cfg.messagePart1Plural + " " + cfg.messagePart2;
	}

	private static MutableComponent playerName(ModConfig cfg, ServerPlayer player) {
		MutableComponent name = Component.literal(player.getName().getString()).withStyle(cfg.playerColor);
		// Hover shows the same display name (hoverable as requested)
		name.withStyle(style -> style.withHoverEvent(
			new HoverEvent.ShowText(Component.literal(player.getGameProfile().name()))
		));
		return name;
	}

	private static MutableComponent colored(ModConfig cfg, String text) {
		return Component.literal(text).withStyle(cfg.messageColor);
	}
}
