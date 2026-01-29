package uk.co.hexeption.omnibind.keybind

import com.mojang.blaze3d.platform.InputConstants
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.minecraft.client.KeyMapping
import net.minecraft.client.Minecraft
import net.minecraft.resources.Identifier
import org.lwjgl.glfw.GLFW
import org.slf4j.LoggerFactory
import uk.co.hexeption.omnibind.OmniBind
import uk.co.hexeption.omnibind.gui.ClothOmniBindScreen

/**
 * Handles keybind registration and processing for OmniBind.
 */
object KeybindHandler {

    private val LOGGER = LoggerFactory.getLogger("OmniBind/Keybinds")

    private val OmniBindCategory = KeyMapping.Category(Identifier.fromNamespaceAndPath(OmniBind.MOD_ID, "category"))

    private val OPEN_CONFIG_KEY: KeyMapping by lazy {
        KeyMapping(
            "key.omnibind.open_config", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, OmniBindCategory
        )
    }

    private val keyStates = mutableMapOf<Int, Boolean>()

    fun registerKeybindings() {
        KeyBindingHelper.registerKeyBinding(OPEN_CONFIG_KEY)
        LOGGER.info("Registered OmniBind keybindings")
    }

    fun registerTickHandler() {
        ClientTickEvents.END_CLIENT_TICK.register { client ->
            while (OPEN_CONFIG_KEY.consumeClick()) {
                if (client.screen == null) {
                    client.setScreen(ClothOmniBindScreen.create(null))
                }
            }
        }
        LOGGER.debug("Registered tick handler for keybind processing")
    }

    fun onKeyEvent(keyCode: Int, action: Int): Boolean {
        val client = Minecraft.getInstance()
        if (client.screen != null) return false

        if (action == GLFW.GLFW_RELEASE) {
            keyStates[keyCode] = false
            return false
        }

        if (SettingsRegistry.isModifierKey(keyCode)) return false

        val modifiers = SettingsRegistry.buildModifiers()

        // For REPEAT action, only process slider keybinds (allow holding)
        if (action == GLFW.GLFW_REPEAT) {
            return SettingsRegistry.processKeyPress(keyCode, modifiers, sliderOnly = true)
        }

        // For PRESS action, check if already pressed for toggle settings
        if (action == GLFW.GLFW_PRESS) {
            if (keyStates[keyCode] == true) return false
            keyStates[keyCode] = true
            return SettingsRegistry.processKeyPress(keyCode, modifiers, sliderOnly = false)
        }

        return false
    }

    fun resetKeyStates() {
        keyStates.clear()
    }
}
