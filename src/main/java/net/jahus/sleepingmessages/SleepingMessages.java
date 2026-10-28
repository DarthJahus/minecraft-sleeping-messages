package net.jahus.sleepingmessages;

import com.mojang.logging.LogUtils;
import net.jahus.sleepingmessages.config.ModConfig;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

@Mod(SleepingMessages.MOD_ID)
public class SleepingMessages {
	public static final String MOD_ID = "sleeping_messages";
	public static final Logger LOGGER = LogUtils.getLogger();
	public static ModConfig CONFIG;

	public SleepingMessages(IEventBus modEventBus) {
		CONFIG = ModConfig.load();
		LOGGER.info("Sleeping Messages loaded");
	}
}
