package net.jahus.sleepingmessages;

import net.fabricmc.api.ModInitializer;
import net.jahus.sleepingmessages.config.ModConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SleepingMessages implements ModInitializer {
	public static final String MOD_ID = "sleeping-messages";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static ModConfig CONFIG;

	@Override
	public void onInitialize() {
		CONFIG = ModConfig.load();
		LOGGER.info("Sleeping Messages loaded");
	}
}
