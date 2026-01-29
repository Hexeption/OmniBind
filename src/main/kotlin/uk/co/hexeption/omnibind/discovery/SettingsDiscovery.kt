package uk.co.hexeption.omnibind.discovery

import net.minecraft.client.Minecraft
import net.minecraft.client.OptionInstance
import net.minecraft.client.Options
import org.slf4j.LoggerFactory
import uk.co.hexeption.omnibind.api.SliderSetting
import uk.co.hexeption.omnibind.api.ToggleableSetting
import java.lang.reflect.Field

/**
 * Discovers toggleable and slider settings from Minecraft's Options via reflection.
 */
object SettingsDiscovery {

    private val LOGGER = LoggerFactory.getLogger("OmniBind/Discovery")

    private val CATEGORY_PATTERNS = linkedMapOf(
        "soundMusic" to "Music",
        "soundRecord" to "Music",
        "sound" to "Sound",
        "volume" to "Sound",
        "render" to "Video",
        "graphics" to "Video",
        "cloud" to "Video",
        "fullscreen" to "Video",
        "vsync" to "Video",
        "bobView" to "Video",
        "particles" to "Video",
        "gui" to "Video",
        "fov" to "Video",
        "gamma" to "Video",
        "screen" to "Video",
        "chat" to "Chat",
        "narrator" to "Accessibility",
        "accessibility" to "Accessibility",
        "touch" to "Controls",
        "auto" to "Controls",
        "key" to "Controls",
        "sneak" to "Controls",
        "sprint" to "Controls",
        "mouse" to "Controls",
        "sensitivity" to "Controls",
        "invert" to "Controls",
        "operator" to "Multiplayer",
        "server" to "Multiplayer",
        "realms" to "Multiplayer",
        "snooper" to "Privacy",
        "telemetry" to "Privacy"
    )

    private val DISPLAY_NAME_OVERRIDES = mapOf(
        "hideGui" to "Hide GUI",
        "bobView" to "View Bobbing",
        "toggleCrouch" to "Toggle Sneak",
        "toggleSprint" to "Toggle Sprint",
        "autoJump" to "Auto Jump",
        "fullscreen" to "Fullscreen",
        "vsync" to "VSync",
        "touchscreen" to "Touchscreen Mode",
        "invertYMouse" to "Invert Mouse",
        "discreteMouseScroll" to "Discrete Scrolling",
        "realmsNotifications" to "Realms Notifications",
        "reducedDebugInfo" to "Reduced Debug Info",
        "showSubtitles" to "Show Subtitles",
        "directionalAudio" to "Directional Audio",
        "backgroundForChatOnly" to "Chat Background Only",
        "chatColors" to "Chat Colors",
        "chatLinks" to "Chat Links",
        "chatLinksPrompt" to "Chat Links Prompt",
        "autoSuggestions" to "Auto Suggestions",
        "onlyShowSecureChat" to "Only Show Secure Chat",
        "useNativeTransport" to "Use Native Transport",
        "pauseOnLostFocus" to "Pause on Lost Focus",
        "advancedItemTooltips" to "Advanced Tooltips",
        "heldItemTooltips" to "Held Item Tooltips",
        "highContrast" to "High Contrast",
        "rawMouseInput" to "Raw Mouse Input",
        "darkMojangStudiosBackgroundColor" to "Dark Loading Screen",
        "hideLightningFlash" to "Hide Lightning Flash",
        "syncChunkWrites" to "Sync Chunk Writes",
        "entityShadows" to "Entity Shadows",
        "forceUnicodeFont" to "Force Unicode Font",
        "hideMatchedNames" to "Hide Matched Names",
        "operatorItemsTab" to "Operator Items Tab",
        "commandSuggestions" to "Command Suggestions"
    )

    private val SLIDER_DISPLAY_NAME_OVERRIDES = mapOf(
        "gamma" to "Brightness",
        "fov" to "FOV",
        "sensitivity" to "Mouse Sensitivity",
        "renderDistance" to "Render Distance",
        "simulationDistance" to "Simulation Distance",
        "entityDistanceScaling" to "Entity Distance",
        "guiScale" to "GUI Scale",
        "chatScale" to "Chat Scale",
        "chatWidth" to "Chat Width",
        "chatHeightFocused" to "Chat Height Focused",
        "chatHeightUnfocused" to "Chat Height Unfocused",
        "chatLineSpacing" to "Chat Line Spacing",
        "textBackgroundOpacity" to "Text Background Opacity",
        "chatOpacity" to "Chat Opacity",
        "chatDelay" to "Chat Delay",
        "soundMasterVolume" to "Master Volume",
        "soundMusicVolume" to "Music Volume",
        "soundRecordVolume" to "Jukebox Volume",
        "soundWeatherVolume" to "Weather Volume",
        "soundBlockVolume" to "Blocks Volume",
        "soundHostileVolume" to "Hostile Volume",
        "soundNeutralVolume" to "Neutral Volume",
        "soundPlayerVolume" to "Player Volume",
        "soundAmbientVolume" to "Ambient Volume",
        "soundVoiceVolume" to "Voice Volume"
    )

    private val SLIDER_RANGES = mapOf(
        "gamma" to Triple(0.0, 5.0, 0.1),
        "fov" to Triple(30.0, 110.0, 1.0),
        "sensitivity" to Triple(0.0, 1.0, 0.01),
        "renderDistance" to Triple(2.0, 32.0, 1.0),
        "simulationDistance" to Triple(5.0, 32.0, 1.0),
        "entityDistanceScaling" to Triple(0.5, 5.0, 0.25),
        "guiScale" to Triple(0.0, 4.0, 1.0),
        "chatScale" to Triple(0.0, 1.0, 0.01),
        "chatWidth" to Triple(0.0, 1.0, 0.01),
        "chatHeightFocused" to Triple(0.0, 1.0, 0.01),
        "chatHeightUnfocused" to Triple(0.0, 1.0, 0.01),
        "chatLineSpacing" to Triple(0.0, 1.0, 0.01),
        "textBackgroundOpacity" to Triple(0.0, 1.0, 0.01),
        "chatOpacity" to Triple(0.0, 1.0, 0.01),
        "chatDelay" to Triple(0.0, 6.0, 0.1),
        // Sound volumes
        "soundMasterVolume" to Triple(0.0, 1.0, 0.05),
        "soundMusicVolume" to Triple(0.0, 1.0, 0.05),
        "soundRecordVolume" to Triple(0.0, 1.0, 0.05),
        "soundWeatherVolume" to Triple(0.0, 1.0, 0.05),
        "soundBlockVolume" to Triple(0.0, 1.0, 0.05),
        "soundHostileVolume" to Triple(0.0, 1.0, 0.05),
        "soundNeutralVolume" to Triple(0.0, 1.0, 0.05),
        "soundPlayerVolume" to Triple(0.0, 1.0, 0.05),
        "soundAmbientVolume" to Triple(0.0, 1.0, 0.05),
        "soundVoiceVolume" to Triple(0.0, 1.0, 0.05)
    )

    fun discoverVanillaSettings(): List<ToggleableSetting> {
        val settings = mutableListOf<ToggleableSetting>()
        val options = Minecraft.getInstance().options

        LOGGER.info("Starting vanilla settings discovery...")

        try {
            val optionsClass = Options::class.java
            val fields = optionsClass.declaredFields

            for (field in fields) {
                try {
                    field.isAccessible = true
                    val setting = tryExtractBooleanSetting(field, options)
                    if (setting != null) {
                        settings.add(setting)
                        LOGGER.debug("Discovered setting: ${setting.id} (${setting.displayName})")
                    }
                } catch (e: Exception) {
                    LOGGER.debug("Failed to process field ${field.name}: ${e.message}")
                }
            }

            LOGGER.info("Discovered ${settings.size} vanilla toggleable settings")
        } catch (e: Exception) {
            LOGGER.error("Failed during settings discovery", e)
        }

        return settings
    }

    @Suppress("UNCHECKED_CAST")
    private fun tryExtractBooleanSetting(field: Field, options: Options): ToggleableSetting? {
        if (!OptionInstance::class.java.isAssignableFrom(field.type)) return null

        val optionInstance = field.get(options) as? OptionInstance<*> ?: return null
        val currentValue = try {
            optionInstance.get()
        } catch (e: Exception) {
            return null
        }
        if (currentValue !is Boolean) return null

        val booleanOption = optionInstance as OptionInstance<Boolean>
        val fieldName = field.name
        val displayName = DISPLAY_NAME_OVERRIDES[fieldName] ?: fieldNameToDisplayName(fieldName)
        val category = determineCategory(fieldName)

        return ToggleableSetting(
            id = "vanilla.$fieldName",
            displayName = displayName,
            category = category,
            getter = { booleanOption.get() },
            setter = { value -> booleanOption.set(value) },
            description = "Vanilla Minecraft setting: $displayName",
            modId = null
        )
    }

    private fun fieldNameToDisplayName(fieldName: String): String {
        val result = StringBuilder()
        for ((index, char) in fieldName.withIndex()) {
            if (index == 0) {
                result.append(char.uppercaseChar())
            } else if (char.isUpperCase()) {
                result.append(' ')
                result.append(char)
            } else {
                result.append(char)
            }
        }
        return result.toString()
    }

    private fun determineCategory(fieldName: String): String {
        val lowerName = fieldName.lowercase()
        for ((pattern, category) in CATEGORY_PATTERNS) {
            if (lowerName.contains(pattern.lowercase())) return category
        }
        return "General"
    }

    fun getCategories(settings: List<ToggleableSetting>): List<String> {
        return settings.map { it.category }.distinct().sorted()
    }

    fun discoverVanillaSliderSettings(): List<SliderSetting> {
        val settings = mutableListOf<SliderSetting>()
        val options = Minecraft.getInstance().options

        LOGGER.info("Starting vanilla slider settings discovery...")

        try {
            val optionsClass = Options::class.java
            val fields = optionsClass.declaredFields

            for (field in fields) {
                try {
                    field.isAccessible = true
                    val setting = tryExtractSliderSetting(field, options)
                    if (setting != null) {
                        settings.add(setting)
                        LOGGER.debug("Discovered slider: ${setting.id} (${setting.displayName})")
                    }
                } catch (e: Exception) {
                    LOGGER.debug("Failed to process slider field ${field.name}: ${e.message}")
                }
            }

            LOGGER.info("Discovered ${settings.size} vanilla slider settings")
        } catch (e: Exception) {
            LOGGER.error("Failed during slider settings discovery", e)
        }

        return settings
    }

    @Suppress("UNCHECKED_CAST")
    private fun tryExtractSliderSetting(field: Field, options: Options): SliderSetting? {
        if (!OptionInstance::class.java.isAssignableFrom(field.type)) return null

        val optionInstance = field.get(options) as? OptionInstance<*> ?: return null
        val currentValue = try {
            optionInstance.get()
        } catch (e: Exception) {
            return null
        }

        val fieldName = field.name
        val displayName = SLIDER_DISPLAY_NAME_OVERRIDES[fieldName] ?: return null
        val category = determineCategory(fieldName)
        val (min, max, step) = SLIDER_RANGES[fieldName] ?: Triple(0.0, 1.0, 0.1)

        // Create getter/setter based on actual value type
        val (getter, setter) = when (currentValue) {
            is Int -> {
                val intOption = optionInstance as OptionInstance<Int>
                Pair({ intOption.get().toDouble() }, { value: Double -> intOption.set(value.toInt()) })
            }

            is Double -> {
                val doubleOption = optionInstance as OptionInstance<Double>
                Pair({ doubleOption.get() }, { value: Double -> doubleOption.set(value) })
            }

            is Float -> {
                val floatOption = optionInstance as OptionInstance<Float>
                Pair({ floatOption.get().toDouble() }, { value: Double -> floatOption.set(value.toFloat()) })
            }

            else -> return null
        }

        return SliderSetting(
            id = "vanilla.$fieldName",
            displayName = displayName,
            category = category,
            getter = getter,
            setter = setter,
            min = min,
            max = max,
            defaultStep = step,
            description = "Vanilla Minecraft setting: $displayName",
            modId = null
        )
    }
}

