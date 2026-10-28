package net.jahus.sleepingmessages.config;

import net.jahus.sleepingmessages.SleepingMessages;
import net.minecraft.ChatFormatting;
import net.minecraft.world.level.Level;
import net.neoforged.fml.loading.FMLPaths;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Simple key=value config in config/sleeping-messages.properties
 * (same filename as Fabric for shared configs).
 */
public final class ModConfig {
	public String messagePart1Singular = "is";
	public String messagePart2 = "sleeping...";
	public String messagePart1Plural = "are";
	public String joiner = "and";
	public String msgOtherPlayers = "other players";
	/** Shown after the player name when alone on the server. Empty = no message. */
	public String messageSleepingAlone = "";
	/** Replaces "[message_part_1_plural] [message_part_2]" when every connected player is in bed. Empty = standard message. */
	public String allPlayersSleepMessage = "";
	public String messageLeftBed = "has left the bed.";
	/** Set to false to disable leave-bed announcements. */
	public boolean enableLeftBedMessage = true;
	/**
	 * When true, suppress leave-bed messages while dayTime is inside
	 * [dawn_window_start, 24000) U [0, dawn_window_end). Default covers post-sleep morning.
	 */
	public boolean suppressLeftBedNearDawn = true;
	/** Inclusive lower bound of the dawn suppress window (0–23999). May be greater than end (wraps midnight). */
	public long dawnWindowStart = 23000L;
	/** Exclusive upper bound of the dawn suppress window after midnight (0–23999). */
	public long dawnWindowEnd = 1500L;
	public ChatFormatting playerColor = ChatFormatting.GOLD;
	public ChatFormatting messageColor = ChatFormatting.WHITE;
	/** Min ms between broadcasts (0 = every sleep event). */
	public long cooldownMs = 0L;

	private ModConfig() {}

	/** True if current default clock time is in the configured dawn suppress window. */
	public boolean isAroundDawn(Level level) {
		if (!suppressLeftBedNearDawn) {
			return false;
		}
		long t = level.getDefaultClockTime() % 24000L;
		if (t < 0) {
			t += 24000L;
		}
		long start = clampDayTime(dawnWindowStart);
		long end = clampDayTime(dawnWindowEnd);
		if (start > end) {
			// wraps midnight: e.g. 23000–24000 and 0–1500
			return t >= start || t < end;
		}
		return t >= start && t < end;
	}

	private static long clampDayTime(long v) {
		if (v < 0L) {
			return 0L;
		}
		if (v > 23999L) {
			return 23999L;
		}
		return v;
	}

	public static ModConfig load() {
		Path path = FMLPaths.CONFIGDIR.get().resolve("sleeping-messages.properties");
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
			cfg.messageSleepingAlone = map.getOrDefault("message_sleeping_alone", cfg.messageSleepingAlone);
			cfg.allPlayersSleepMessage = map.getOrDefault("all_players_sleep_message", cfg.allPlayersSleepMessage);
			cfg.messageLeftBed = map.getOrDefault("message_left_bed", cfg.messageLeftBed);
			if (map.containsKey("enable_left_bed_message")) {
				cfg.enableLeftBedMessage = Boolean.parseBoolean(map.get("enable_left_bed_message"));
			}
			if (map.containsKey("suppress_left_bed_near_dawn")) {
				cfg.suppressLeftBedNearDawn = Boolean.parseBoolean(map.get("suppress_left_bed_near_dawn"));
			}
			if (map.containsKey("dawn_window_start")) {
				try {
					cfg.dawnWindowStart = Long.parseLong(map.get("dawn_window_start"));
				} catch (NumberFormatException ignored) {}
			}
			if (map.containsKey("dawn_window_end")) {
				try {
					cfg.dawnWindowEnd = Long.parseLong(map.get("dawn_window_end"));
				} catch (NumberFormatException ignored) {}
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
			# Alone:     [p1] [message_sleeping_alone]  (empty = no message; no leave-bed message either)
			# Everyone:  same names as above, then [all_players_sleep_message] instead of [message_part_1_plural] [message_part_2]
			#            (everyone connected is in bed, 2+ players; empty = standard message)

			message_part_1_singular=is
			message_part_2=sleeping...
			message_part_1_plural=are
			joiner=and
			msg_other_players=other players
			message_sleeping_alone=
			all_players_sleep_message=
			message_left_bed=has left the bed.
			enable_left_bed_message=true
			# Suppress leave-bed chat after night skip / around dawn (dayTime window, may wrap midnight)
			suppress_left_bed_near_dawn=true
			dawn_window_start=23000
			dawn_window_end=1500
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
