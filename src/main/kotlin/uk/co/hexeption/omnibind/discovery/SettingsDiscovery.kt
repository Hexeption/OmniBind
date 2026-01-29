package uk.co.hexeption.omnibind.discovery

import net.minecraft.client.Minecraft
import net.minecraft.client.OptionInstance
import net.minecraft.client.Options
import org.slf4j.LoggerFactory
import uk.co.hexeption.omnibind.api.SliderSetting
import uk.co.hexeption.omnibind.api.ToggleableSetting
import uk.co.hexeption.omnibind.mixins.OptionInstanceAccessor
import java.lang.reflect.Field

/**
 * Discovers toggleable and slider settings from Minecraft's Options via reflection.
 */
object SettingsDiscovery {

    private val LOGGER = LoggerFactory.getLogger("OmniBind/Discovery")

    private val CATEGORY_MAPPINGS = mapOf(
        "music" to "Sound",
        "sound" to "Sound",
        "record" to "Sound",
        "volume" to "Sound",
        "render" to "Video",
        "graphics" to "Video",
        "cloud" to "Video",
        "fullscreen" to "Video",
        "vsync" to "Video",
        "bob" to "Video",
        "particles" to "Video",
        "gui" to "Video",
        "fov" to "Video",
        "gamma" to "Video",
        "brightness" to "Video",
        "screen" to "Video",
        "chat" to "Chat",
        "narrator" to "Accessibility",
        "accessibility" to "Accessibility",
        "contrast" to "Accessibility",
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
        "telemetry" to "Privacy",
        "secure" to "Chat"
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
        "commandSuggestions" to "Command Suggestions",
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

    fun discoverVanillaSettings(): List<ToggleableSetting> {
        LOGGER.info("Starting vanilla settings discovery...")
        val options = Minecraft.getInstance().options
        val settings = Options::class.java.declaredFields.mapNotNull { field ->
            try {
                field.isAccessible = true
                tryExtractBooleanSetting(field, options)?.also {
                    LOGGER.debug("Discovered setting: ${it.id} (${it.displayName})")
                }
            } catch (e: Exception) {
                LOGGER.debug("Failed to process field ${field.name}: ${e.message}")
                null
            }
        }
        LOGGER.info("Discovered ${settings.size} vanilla toggleable settings")
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
        val displayName =
            extractDisplayNameFromOption(optionInstance) ?: DISPLAY_NAME_OVERRIDES[fieldName] ?: fieldNameToDisplayName(
                fieldName
            )
        val category = determineCategory(fieldName, displayName)

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
        return fieldName.replace(Regex("([A-Z])"), " $1").replaceFirstChar { it.uppercaseChar() }
    }

    private fun determineCategory(fieldName: String, displayName: String): String {
        val lowerName = fieldName.lowercase()
        val lowerDisplay = displayName.lowercase()
        return CATEGORY_MAPPINGS.entries.firstOrNull {
            lowerName.contains(it.key) || lowerDisplay.contains(it.key)
        }?.value ?: "General"
    }

    fun getCategories(settings: List<ToggleableSetting>): List<String> {
        return settings.map { it.category }.distinct().sorted()
    }

    fun discoverVanillaSliderSettings(): List<SliderSetting> {
        LOGGER.info("Starting vanilla slider settings discovery...")
        val options = Minecraft.getInstance().options
        val settings = Options::class.java.declaredFields.mapNotNull { field ->
            try {
                field.isAccessible = true
                tryExtractSliderSetting(field, options)?.also {
                    LOGGER.debug("Discovered slider: ${it.id} (${it.displayName})")
                }
            } catch (e: Exception) {
                LOGGER.debug("Failed to process slider field ${field.name}: ${e.message}")
                null
            }
        }
        LOGGER.info("Discovered ${settings.size} vanilla slider settings")
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
        val displayName =
            extractDisplayNameFromOption(optionInstance) ?: DISPLAY_NAME_OVERRIDES[fieldName] ?: return null
        val category = determineCategory(fieldName, displayName)

        val range = extractRangeFromOptionInstance(optionInstance)

        val (min, max, step) = range ?: run {
            val rawCurrent = when (currentValue) {
                is Number -> currentValue.toDouble()
                else -> 0.0
            }
            val fallbackMax = if (rawCurrent > 1.0) rawCurrent else 1.0
            Triple(0.0, fallbackMax, 0.1)
        }

        val rawCurrent = when (currentValue) {
            is Number -> currentValue.toDouble()
            else -> 0.0
        }
        var finalMax = max
        if (rawCurrent > finalMax) finalMax = rawCurrent
        if (finalMax <= min) finalMax = min + maxOf(1.0, kotlin.math.abs(min))
        var finalStep = step
        val rangeSpan = finalMax - min
        if (finalStep <= 0.0) finalStep = if (rangeSpan >= 1.0) 1.0 else 0.01
        if (finalStep > rangeSpan) finalStep = if (rangeSpan >= 1.0) 1.0 else rangeSpan / 10.0

        val isIntegerBacked = currentValue is Int
        val adjustedMax = if (isIntegerBacked) kotlin.math.ceil(finalMax).toDouble() else finalMax
        val adjustedStep =
            if (isIntegerBacked) kotlin.math.max(1.0, kotlin.math.round(finalStep).toDouble()) else finalStep

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
            max = adjustedMax,
            defaultStep = adjustedStep,
            description = "Vanilla Minecraft setting: $displayName",
            modId = null
        )
    }

    private fun extractRangeFromOptionInstance(optionInstance: OptionInstance<*>): Triple<Double, Double, Double>? {
        try {
            if (optionInstance is OptionInstanceAccessor) {
                val valuesObj = (optionInstance as OptionInstanceAccessor).values
                if (valuesObj != null) {
                    return probeObjectForRange(valuesObj)
                }
            }
        } catch (e: Exception) {
            LOGGER.debug("Failed to extract range from OptionInstance: ${e.message}")
        }
        return null
    }

    private fun probeObjectForRange(obj: Any): Triple<Double, Double, Double>? {
        try {
            var min: Double? = null
            var max: Double? = null
            var step: Double? = null
            val cls = obj.javaClass

            for (f in cls.declaredFields) {
                try {
                    f.isAccessible = true
                    val name = f.name.lowercase()
                    val v = when (val value = f.get(obj)) {
                        is Number -> value.toDouble()
                        else -> null
                    }
                    if (v != null) {
                        when {
                            name.contains("min") || name.contains("lower") -> min = v
                            name.contains("max") || name.contains("upper") -> max = v
                            name.contains("step") || name.contains("increment") -> step = v
                        }
                    }
                } catch (_: Throwable) {
                }
            }

            if (min == null || max == null) {
                for (m in cls.methods) {
                    try {
                        if (m.parameterCount != 0) continue
                        val name = m.name.lowercase()
                        val v = when (val value = m.invoke(obj)) {
                            is Number -> value.toDouble()
                            else -> null
                        }
                        if (v != null) {
                            when {
                                name.contains("min") || name.contains("lower") -> min = v
                                name.contains("max") || name.contains("upper") -> max = v
                                name.contains("step") || name.contains("increment") -> step = v
                            }
                        }
                    } catch (_: Throwable) {
                    }
                }
            }

            if (min != null && max != null) {
                if (step == null) step = if ((max - min) >= 1.0) 1.0 else 0.01
                return Triple(min, max, step)
            }
        } catch (_: Throwable) {
        }
        return null
    }


    private fun extractDisplayNameFromOption(optionInstance: OptionInstance<*>?): String? {
        if (optionInstance == null) return null

        try {
            if (optionInstance is OptionInstanceAccessor) {
                return (optionInstance as OptionInstanceAccessor).caption.string
            }
        } catch (e: Exception) {
            LOGGER.debug("Failed to extract display name from OptionInstance: ${e.message}")
        }

        return null
    }
}
