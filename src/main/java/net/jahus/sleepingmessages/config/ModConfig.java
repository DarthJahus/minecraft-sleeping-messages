package net.jahus.sleepingmessages.config;

import net.fabricmc.loader.api.FabricLoader;
import net.jahus.sleepingmessages.SleepingMessages;
import net.minecraft.ChatFormatting;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Simple key=value config in config/sleeping-messages.properties
 */
public final class ModConfig {
	public String messagePart1Singular = "is";
	public String messagePart2 = "sleeping...";
	public String messagePart1Plural = "are";
	public String joiner = "and";
	public String msgOtherPlayers = "other players";
	public String messageLeftBed = "has left the bed.";
	/** Set to false to disable leave-bed announcements. */
	public boolean enableLeftBedMessage = true;
	public ChatFormatting playerColor = ChatFormatting.GOLD;
	public ChatFormatting messageColor = ChatFormatting.WHITE;
	/** Min ms between broadcasts (0 = every sleep event). */
	public long cooldownMs = 0L;

	private ModConfig() {}

	public static ModConfig load() {
		Path path = FabricLoader.getInstance().getConfigDir().resolve("sleeping-messages.properties");
		ModConfig cfg = new ModConfig();
		if (!Files.exists(path)) {
			cfg.save(path);
			return cfg;
		}
		try {
			Map<String, String> map = new LinkedHashMap<>();
			for (String line : Files.readAllLines(path, StandardCharsets.UTF_8)) {
				line = line.trim();
				if (line.isEmpty() || line.startsWith("#")) continue;
				int eq = line.indexOf('=');
				if (eq <= 0) continue;
				map.put(line.substring(0, eq).trim(), line.substring(eq + 1).trim());
			}
			cfg.messagePart1Singular = map.getOrDefault("message_part_1_singular", cfg.messagePart1Singular);
			cfg.messagePart2 = map.getOrDefault("message_part_2", cfg.messagePart2);
			cfg.messagePart1Plural = map.getOrDefault("message_part_1_plural", cfg.messagePart1Plural);
			cfg.joiner = map.getOrDefault("joiner", cfg.joiner);
			cfg.msgOtherPlayers = map.getOrDefault("msg_other_players", cfg.msgOtherPlayers);
			cfg.messageLeftBed = map.getOrDefault("message_left_bed", cfg.messageLeftBed);
			if (map.containsKey("enable_left_bed_message")) {
				cfg.enableLeftBedMessage = Boolean.parseBoolean(map.get("enable_left_bed_message"));
			}
			cfg.playerColor = parseColor(map.get("player_color"), cfg.playerColor);
			cfg.messageColor = parseColor(map.get("message_color"), cfg.messageColor);
			if (map.containsKey("cooldown_ms")) {
				try {
					cfg.cooldownMs = Long.parseLong(map.get("cooldown_ms"));
				} catch (NumberFormatException ignored) {}
			}
		} catch (IOException e) {
			SleepingMessages.LOGGER.error("Failed to read config, using defaults", e);
		}
		return cfg;
	}

	private void save(Path path) {
		String content = """
			# Sleeping Messages config
			# Singular:  [player] [message_part_1_singular] [message_part_2]
			# Two:       [p1] [joiner] [p2] [message_part_1_plural] [message_part_2]
			# Three:     [p1], [p2] [joiner] [p3] [message_part_1_plural] [message_part_2]
			# Four+:     [p1] [joiner] <N> [msg_other_players] [message_part_1_plural] [message_part_2]

			message_part_1_singular=is
			message_part_2=sleeping...
			message_part_1_plural=are
			joiner=and
			msg_other_players=other players
			message_left_bed=has left the bed.
			enable_left_bed_message=true
			player_color=gold
			message_color=white
			cooldown_ms=0
			""";
		try {
			Files.createDirectories(path.getParent());
			Files.writeString(path, content, StandardCharsets.UTF_8);
		} catch (IOException e) {
			SleepingMessages.LOGGER.error("Failed to write default config", e);
		}
	}

	private static ChatFormatting parseColor(String raw, ChatFormatting fallback) {
		if (raw == null || raw.isEmpty()) return fallback;
		try {
			return ChatFormatting.valueOf(raw.trim().toUpperCase(Locale.ROOT));
		} catch (IllegalArgumentException ignored) {
			return fallback;
		}
	}
}
