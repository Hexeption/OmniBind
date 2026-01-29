package uk.co.hexeption.omnibind.keybind

import net.minecraft.client.Minecraft
import org.lwjgl.glfw.GLFW
import org.slf4j.LoggerFactory
import uk.co.hexeption.omnibind.api.OmniBindApi
import uk.co.hexeption.omnibind.api.SliderSetting
import uk.co.hexeption.omnibind.api.ToggleableSetting
import uk.co.hexeption.omnibind.config.OmniBindConfig
import uk.co.hexeption.omnibind.config.OmniBindConfig.KeybindData
import uk.co.hexeption.omnibind.discovery.SettingsDiscovery
import uk.co.hexeption.omnibind.notification.NotificationManager

/**
 * Central registry for toggleable and slider settings with keybind processing.
 */
object SettingsRegistry {

    private val LOGGER = LoggerFactory.getLogger("OmniBind/Registry")
    private val settings = mutableMapOf<String, ToggleableSetting>()
    private val sliderSettings = mutableMapOf<String, SliderSetting>()
    private var initialized = false

    fun initialize() {
        if (initialized) {
            LOGGER.warn("SettingsRegistry already initialized!")
            return
        }

        LOGGER.info("Initializing OmniBind Settings Registry...")
        OmniBindConfig.load()

        OmniBindApi.setRegistrationCallback { setting ->
            registerSetting(setting)
        }

        initialized = true
        LOGGER.info("Settings Registry initialized")
    }

    fun discoverSettings() {
        LOGGER.info("Discovering settings...")
        val vanillaSettings = SettingsDiscovery.discoverVanillaSettings()
        vanillaSettings.forEach { registerSetting(it) }

        val vanillaSliders = SettingsDiscovery.discoverVanillaSliderSettings()
        vanillaSliders.forEach { registerSliderSetting(it) }

        LOGGER.info("Total settings registered: ${settings.size} toggles, ${sliderSettings.size} sliders")
    }

    fun registerSetting(setting: ToggleableSetting) {
        if (settings.containsKey(setting.id)) {
            LOGGER.debug("Overwriting existing setting: ${setting.id}")
        }
        settings[setting.id] = setting
        LOGGER.debug("Registered setting: ${setting.id}")
    }

    fun registerSliderSetting(setting: SliderSetting) {
        if (sliderSettings.containsKey(setting.id)) {
            LOGGER.debug("Overwriting existing slider setting: ${setting.id}")
        }
        sliderSettings[setting.id] = setting
        LOGGER.debug("Registered slider setting: ${setting.id}")
    }

    fun unregisterSetting(settingId: String): Boolean = settings.remove(settingId) != null

    fun getSetting(settingId: String): ToggleableSetting? = settings[settingId]

    fun getSliderSetting(settingId: String): SliderSetting? = sliderSettings[settingId]

    fun getAllSettings(): List<ToggleableSetting> = settings.values.toList()

    fun getAllSliderSettings(): List<SliderSetting> = sliderSettings.values.toList()

    fun getSettingsByCategory(category: String): List<ToggleableSetting> {
        return settings.values.filter { it.category == category }
    }

    fun getCategories(): List<String> = settings.values.map { it.category }.distinct().sorted()

    fun searchSettings(query: String): List<ToggleableSetting> {
        if (query.isBlank()) return getAllSettings()
        val lowerQuery = query.lowercase()
        return settings.values.filter { setting ->
            setting.displayName.lowercase().contains(lowerQuery) ||
            setting.id.lowercase().contains(lowerQuery) ||
            setting.category.lowercase().contains(lowerQuery) ||
            setting.description.lowercase().contains(lowerQuery)
        }
    }

    fun getKeybind(settingId: String): KeybindData? = OmniBindConfig.getKeybind(settingId)

    fun setKeybind(settingId: String, keybind: KeybindData) = OmniBindConfig.setKeybind(settingId, keybind)

    fun clearKeybind(settingId: String) = OmniBindConfig.clearKeybind(settingId)

    fun findConflicts(keybind: KeybindData, excludeSettingId: String? = null): List<ToggleableSetting> {
        val conflictIds = OmniBindConfig.findConflicts(keybind, excludeSettingId)
        return conflictIds.mapNotNull { getSetting(it) }
    }

    fun processKeyPress(keyCode: Int, modifiers: Int, sliderOnly: Boolean = false): Boolean {
        if (keyCode == GLFW.GLFW_KEY_UNKNOWN) return false

        val keybinds = OmniBindConfig.getAllKeybinds()
        var triggered = false

        for ((settingId, keybindData) in keybinds) {
            if (keybindData.keyCode == keyCode && keybindData.modifiers == modifiers) {
                // Check if it's a slider increment/decrement keybind
                if (settingId.endsWith(".increment")) {
                    val sliderId = settingId.removeSuffix(".increment")
                    if (!OmniBindConfig.isSliderEnabled(sliderId)) continue
                    val slider = getSliderSetting(sliderId)
                    if (slider != null) {
                        try {
                            val newValue = slider.increment()
                            if (!OmniBindConfig.isNotificationDisabled(sliderId)) {
                                NotificationManager.showSliderNotification(slider, newValue)
                            }
                            LOGGER.debug("Incremented ${slider.displayName} to $newValue")
                            triggered = true
                        } catch (e: Exception) {
                            LOGGER.error("Failed to increment slider ${slider.id}", e)
                        }
                    }
                } else if (settingId.endsWith(".decrement")) {
                    val sliderId = settingId.removeSuffix(".decrement")
                    if (!OmniBindConfig.isSliderEnabled(sliderId)) continue
                    val slider = getSliderSetting(sliderId)
                    if (slider != null) {
                        try {
                            val newValue = slider.decrement()
                            if (!OmniBindConfig.isNotificationDisabled(sliderId)) {
                                NotificationManager.showSliderNotification(slider, newValue)
                            }
                            LOGGER.debug("Decremented ${slider.displayName} to $newValue")
                            triggered = true
                        } catch (e: Exception) {
                            LOGGER.error("Failed to decrement slider ${slider.id}", e)
                        }
                    }
                } else if (!sliderOnly) {
                    // Regular toggle setting (skip if sliderOnly mode)
                    val setting = getSetting(settingId)
                    if (setting != null) {
                        try {
                            val newValue = setting.toggle()
                            if (!OmniBindConfig.isNotificationDisabled(settingId)) {
                                NotificationManager.showToggleNotification(setting, newValue)
                            }
                            LOGGER.debug("Toggled ${setting.displayName} to $newValue")
                            triggered = true
                        } catch (e: Exception) {
                            LOGGER.error("Failed to toggle setting ${setting.id}", e)
                        }
                    }
                }
            }
        }

        return triggered
    }

    fun buildModifiers(): Int {
        val mc = Minecraft.getInstance()
        val windowHandle = mc.window.handle()
        var modifiers = 0

        if (GLFW.glfwGetKey(windowHandle, GLFW.GLFW_KEY_LEFT_SHIFT) == GLFW.GLFW_PRESS ||
            GLFW.glfwGetKey(windowHandle, GLFW.GLFW_KEY_RIGHT_SHIFT) == GLFW.GLFW_PRESS) {
            modifiers = modifiers or KeybindData.MODIFIER_SHIFT
        }

        if (GLFW.glfwGetKey(windowHandle, GLFW.GLFW_KEY_LEFT_CONTROL) == GLFW.GLFW_PRESS ||
            GLFW.glfwGetKey(windowHandle, GLFW.GLFW_KEY_RIGHT_CONTROL) == GLFW.GLFW_PRESS) {
            modifiers = modifiers or KeybindData.MODIFIER_CTRL
        }

        if (GLFW.glfwGetKey(windowHandle, GLFW.GLFW_KEY_LEFT_ALT) == GLFW.GLFW_PRESS ||
            GLFW.glfwGetKey(windowHandle, GLFW.GLFW_KEY_RIGHT_ALT) == GLFW.GLFW_PRESS) {
            modifiers = modifiers or KeybindData.MODIFIER_ALT
        }

        return modifiers
    }

    fun isModifierKey(keyCode: Int): Boolean {
        return keyCode in listOf(
            GLFW.GLFW_KEY_LEFT_SHIFT, GLFW.GLFW_KEY_RIGHT_SHIFT,
            GLFW.GLFW_KEY_LEFT_CONTROL, GLFW.GLFW_KEY_RIGHT_CONTROL,
            GLFW.GLFW_KEY_LEFT_ALT, GLFW.GLFW_KEY_RIGHT_ALT,
            GLFW.GLFW_KEY_LEFT_SUPER, GLFW.GLFW_KEY_RIGHT_SUPER
        )
    }
}
