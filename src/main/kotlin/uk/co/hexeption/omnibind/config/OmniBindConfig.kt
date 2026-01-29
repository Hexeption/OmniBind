package uk.co.hexeption.omnibind.config

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import net.fabricmc.loader.api.FabricLoader
import org.lwjgl.glfw.GLFW
import org.slf4j.LoggerFactory
import java.io.File

/**
 * Configuration manager for OmniBind keybind persistence.
 */
object OmniBindConfig {

    private val LOGGER = LoggerFactory.getLogger("OmniBind/Config")
    private val CONFIG_FILE: File = FabricLoader.getInstance().configDir.resolve("omnibind.json").toFile()
    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()

    data class ConfigData(
        val version: Int = 1,
        // Keybind assignments for all settings
        val keybinds: MutableMap<String, KeybindData> = mutableMapOf(),
        // Toggle settings enabled state
        val toggleEnabled: MutableMap<String, Boolean> = mutableMapOf(),
        // Slider settings
        val sliderSteps: MutableMap<String, Double> = mutableMapOf(),
        val sliderEnabled: MutableMap<String, Boolean> = mutableMapOf(),
        // Per-setting notification preferences
        val notificationsDisabled: MutableMap<String, Boolean> = mutableMapOf(),
        // Global notification settings
        val showToasts: Boolean = true,
        val showHudNotifications: Boolean = false,
        val hudNotificationDuration: Int = 2000
    )

    data class KeybindData(
        val keyCode: Int = GLFW.GLFW_KEY_UNKNOWN, val modifiers: Int = 0
    ) {
        companion object {
            const val MODIFIER_SHIFT = 1
            const val MODIFIER_CTRL = 2
            const val MODIFIER_ALT = 4
        }

        fun hasShift(): Boolean = (modifiers and MODIFIER_SHIFT) != 0
        fun hasCtrl(): Boolean = (modifiers and MODIFIER_CTRL) != 0
        fun hasAlt(): Boolean = (modifiers and MODIFIER_ALT) != 0
        fun isUnbound(): Boolean = keyCode == GLFW.GLFW_KEY_UNKNOWN || keyCode == -1

        fun toDisplayString(): String {
            if (isUnbound()) return "Not Bound"
            val parts = mutableListOf<String>()
            if (hasCtrl()) parts.add("Ctrl")
            if (hasAlt()) parts.add("Alt")
            if (hasShift()) parts.add("Shift")
            parts.add(getKeyName(keyCode))

            return parts.joinToString(" + ")
        }

        private fun getKeyName(keyCode: Int): String {
            return when (keyCode) {
                GLFW.GLFW_KEY_SPACE -> "Space"
                GLFW.GLFW_KEY_APOSTROPHE -> "'"
                GLFW.GLFW_KEY_COMMA -> ","
                GLFW.GLFW_KEY_MINUS -> "-"
                GLFW.GLFW_KEY_PERIOD -> "."
                GLFW.GLFW_KEY_SLASH -> "/"
                GLFW.GLFW_KEY_0 -> "0"
                GLFW.GLFW_KEY_1 -> "1"
                GLFW.GLFW_KEY_2 -> "2"
                GLFW.GLFW_KEY_3 -> "3"
                GLFW.GLFW_KEY_4 -> "4"
                GLFW.GLFW_KEY_5 -> "5"
                GLFW.GLFW_KEY_6 -> "6"
                GLFW.GLFW_KEY_7 -> "7"
                GLFW.GLFW_KEY_8 -> "8"
                GLFW.GLFW_KEY_9 -> "9"
                GLFW.GLFW_KEY_SEMICOLON -> ";"
                GLFW.GLFW_KEY_EQUAL -> "="
                GLFW.GLFW_KEY_A -> "A"
                GLFW.GLFW_KEY_B -> "B"
                GLFW.GLFW_KEY_C -> "C"
                GLFW.GLFW_KEY_D -> "D"
                GLFW.GLFW_KEY_E -> "E"
                GLFW.GLFW_KEY_F -> "F"
                GLFW.GLFW_KEY_G -> "G"
                GLFW.GLFW_KEY_H -> "H"
                GLFW.GLFW_KEY_I -> "I"
                GLFW.GLFW_KEY_J -> "J"
                GLFW.GLFW_KEY_K -> "K"
                GLFW.GLFW_KEY_L -> "L"
                GLFW.GLFW_KEY_M -> "M"
                GLFW.GLFW_KEY_N -> "N"
                GLFW.GLFW_KEY_O -> "O"
                GLFW.GLFW_KEY_P -> "P"
                GLFW.GLFW_KEY_Q -> "Q"
                GLFW.GLFW_KEY_R -> "R"
                GLFW.GLFW_KEY_S -> "S"
                GLFW.GLFW_KEY_T -> "T"
                GLFW.GLFW_KEY_U -> "U"
                GLFW.GLFW_KEY_V -> "V"
                GLFW.GLFW_KEY_W -> "W"
                GLFW.GLFW_KEY_X -> "X"
                GLFW.GLFW_KEY_Y -> "Y"
                GLFW.GLFW_KEY_Z -> "Z"
                GLFW.GLFW_KEY_LEFT_BRACKET -> "["
                GLFW.GLFW_KEY_BACKSLASH -> "\\"
                GLFW.GLFW_KEY_RIGHT_BRACKET -> "]"
                GLFW.GLFW_KEY_GRAVE_ACCENT -> "`"
                GLFW.GLFW_KEY_ESCAPE -> "Escape"
                GLFW.GLFW_KEY_ENTER -> "Enter"
                GLFW.GLFW_KEY_TAB -> "Tab"
                GLFW.GLFW_KEY_BACKSPACE -> "Backspace"
                GLFW.GLFW_KEY_INSERT -> "Insert"
                GLFW.GLFW_KEY_DELETE -> "Delete"
                GLFW.GLFW_KEY_RIGHT -> "Right"
                GLFW.GLFW_KEY_LEFT -> "Left"
                GLFW.GLFW_KEY_DOWN -> "Down"
                GLFW.GLFW_KEY_UP -> "Up"
                GLFW.GLFW_KEY_PAGE_UP -> "Page Up"
                GLFW.GLFW_KEY_PAGE_DOWN -> "Page Down"
                GLFW.GLFW_KEY_HOME -> "Home"
                GLFW.GLFW_KEY_END -> "End"
                GLFW.GLFW_KEY_CAPS_LOCK -> "Caps Lock"
                GLFW.GLFW_KEY_SCROLL_LOCK -> "Scroll Lock"
                GLFW.GLFW_KEY_NUM_LOCK -> "Num Lock"
                GLFW.GLFW_KEY_PRINT_SCREEN -> "Print Screen"
                GLFW.GLFW_KEY_PAUSE -> "Pause"
                GLFW.GLFW_KEY_F1 -> "F1"
                GLFW.GLFW_KEY_F2 -> "F2"
                GLFW.GLFW_KEY_F3 -> "F3"
                GLFW.GLFW_KEY_F4 -> "F4"
                GLFW.GLFW_KEY_F5 -> "F5"
                GLFW.GLFW_KEY_F6 -> "F6"
                GLFW.GLFW_KEY_F7 -> "F7"
                GLFW.GLFW_KEY_F8 -> "F8"
                GLFW.GLFW_KEY_F9 -> "F9"
                GLFW.GLFW_KEY_F10 -> "F10"
                GLFW.GLFW_KEY_F11 -> "F11"
                GLFW.GLFW_KEY_F12 -> "F12"
                GLFW.GLFW_KEY_KP_0 -> "Numpad 0"
                GLFW.GLFW_KEY_KP_1 -> "Numpad 1"
                GLFW.GLFW_KEY_KP_2 -> "Numpad 2"
                GLFW.GLFW_KEY_KP_3 -> "Numpad 3"
                GLFW.GLFW_KEY_KP_4 -> "Numpad 4"
                GLFW.GLFW_KEY_KP_5 -> "Numpad 5"
                GLFW.GLFW_KEY_KP_6 -> "Numpad 6"
                GLFW.GLFW_KEY_KP_7 -> "Numpad 7"
                GLFW.GLFW_KEY_KP_8 -> "Numpad 8"
                GLFW.GLFW_KEY_KP_9 -> "Numpad 9"
                GLFW.GLFW_KEY_KP_DECIMAL -> "Numpad ."
                GLFW.GLFW_KEY_KP_DIVIDE -> "Numpad /"
                GLFW.GLFW_KEY_KP_MULTIPLY -> "Numpad *"
                GLFW.GLFW_KEY_KP_SUBTRACT -> "Numpad -"
                GLFW.GLFW_KEY_KP_ADD -> "Numpad +"
                GLFW.GLFW_KEY_KP_ENTER -> "Numpad Enter"
                else -> "Key $keyCode"
            }
        }
    }

    private var config: ConfigData = ConfigData()

    fun load() {
        try {
            if (CONFIG_FILE.exists()) {
                val content = CONFIG_FILE.readText()
                config = gson.fromJson(content, ConfigData::class.java) ?: ConfigData()
                LOGGER.info("Loaded OmniBind configuration with ${config.keybinds.size} keybinds")
            } else {
                config = ConfigData()
                save()
                LOGGER.info("Created new OmniBind configuration file")
            }
        } catch (e: Exception) {
            LOGGER.error("Failed to load OmniBind configuration, using defaults", e)
            config = ConfigData()
        }
    }

    fun save() {
        try {
            CONFIG_FILE.parentFile?.mkdirs()
            CONFIG_FILE.writeText(gson.toJson(config))
            LOGGER.debug("Saved OmniBind configuration")
        } catch (e: Exception) {
            LOGGER.error("Failed to save OmniBind configuration", e)
        }
    }

    fun getKeybind(settingId: String): KeybindData? = config.keybinds[settingId]

    fun setKeybind(settingId: String, keybind: KeybindData) {
        config.keybinds[settingId] = keybind
        save()
    }

    fun clearKeybind(settingId: String) {
        config.keybinds.remove(settingId)
        save()
    }

    fun getAllKeybinds(): Map<String, KeybindData> = config.keybinds.toMap()

    fun showToasts(): Boolean = config.showToasts

    fun setShowToasts(show: Boolean) {
        config = config.copy(showToasts = show)
        save()
    }

    fun showHudNotifications(): Boolean = config.showHudNotifications

    fun setShowHudNotifications(show: Boolean) {
        config = config.copy(showHudNotifications = show)
        save()
    }

    fun isToggleEnabled(settingId: String): Boolean {
        return config.toggleEnabled[settingId] ?: false
    }

    fun setToggleEnabled(settingId: String, enabled: Boolean) {
        config.toggleEnabled[settingId] = enabled
        save()
    }

    fun getSliderStep(sliderId: String, defaultStep: Double): Double {
        return config.sliderSteps[sliderId] ?: defaultStep
    }

    fun setSliderStep(sliderId: String, step: Double) {
        config.sliderSteps[sliderId] = step
        save()
    }

    fun isSliderEnabled(sliderId: String): Boolean {
        return config.sliderEnabled[sliderId] ?: false
    }

    fun setSliderEnabled(sliderId: String, enabled: Boolean) {
        config.sliderEnabled[sliderId] = enabled
        save()
    }

    fun isNotificationDisabled(settingId: String): Boolean {
        return config.notificationsDisabled[settingId] ?: false
    }

    fun setNotificationDisabled(settingId: String, disabled: Boolean) {
        config.notificationsDisabled[settingId] = disabled
        save()
    }

    fun findConflicts(keybind: KeybindData, excludeSettingId: String? = null): List<String> {
        if (keybind.isUnbound()) return emptyList()

        return config.keybinds.filter { (id, kb) ->
                id != excludeSettingId && kb.keyCode == keybind.keyCode && kb.modifiers == keybind.modifiers
            }.keys.toList()
    }
}

