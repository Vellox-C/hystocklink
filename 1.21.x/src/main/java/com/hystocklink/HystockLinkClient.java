package com.hystocklink;

import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HystockLinkClient implements ClientModInitializer {

    public static final Logger LOGGER = LoggerFactory.getLogger("hystocklink");

    @Override
    public void onInitializeClient() {
        LOGGER.info("HystockLink initialized.");
    }
}
