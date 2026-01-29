package uk.co.hexeption.omnibind

import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents
import org.slf4j.LoggerFactory
import uk.co.hexeption.omnibind.gui.HudRenderer
import uk.co.hexeption.omnibind.keybind.KeybindHandler
import uk.co.hexeption.omnibind.keybind.SettingsRegistry

/**
 * OmniBind - Assign custom keybinds to any toggleable boolean setting.
 */
class OmniBind : ClientModInitializer {

    companion object {
        const val MOD_ID = "omnibind"
        private val LOGGER = LoggerFactory.getLogger("OmniBind")
    }

    override fun onInitializeClient() {
        LOGGER.info("Initializing OmniBind...")

        SettingsRegistry.initialize()
        KeybindHandler.registerKeybindings()
        KeybindHandler.registerTickHandler()
        HudRenderer.register()

        ClientLifecycleEvents.CLIENT_STARTED.register { _ ->
            LOGGER.info("Client started, discovering settings...")
            SettingsRegistry.discoverSettings()
        }

        LOGGER.info("OmniBind initialized successfully!")
    }
}