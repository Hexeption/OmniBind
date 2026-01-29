package uk.co.hexeption.omnibind.gui

import com.mojang.blaze3d.platform.InputConstants
import me.shedaniel.clothconfig2.api.ConfigBuilder
import me.shedaniel.clothconfig2.api.Modifier
import me.shedaniel.clothconfig2.api.ModifierKeyCode
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component
import uk.co.hexeption.omnibind.keybind.SettingsRegistry
import uk.co.hexeption.omnibind.config.OmniBindConfig

/**
 * ClothConfig-based configuration screen for OmniBind settings and keybinds.
 */
object ClothOmniBindScreen {

    fun create(parent: Screen?): Screen {
        val builder = ConfigBuilder.create()
            .setParentScreen(parent)
            .setTitle(Component.literal("OmniBind Settings"))
            .setSavingRunnable {
                OmniBindConfig.save()
            }

        val entryBuilder = builder.entryBuilder()

        val generalCategory = builder.getOrCreateCategory(Component.literal("General"))

        generalCategory.addEntry(
            entryBuilder.startBooleanToggle(
                Component.literal("Show Toast Notifications"),
                OmniBindConfig.showToasts()
            )
                .setDefaultValue(true)
                .setTooltip(Component.literal("Display toast notifications when toggling settings"))
                .setSaveConsumer { OmniBindConfig.setShowToasts(it) }
                .build()
        )

        generalCategory.addEntry(
            entryBuilder.startBooleanToggle(
                Component.literal("Show HUD Notifications"),
                OmniBindConfig.showHudNotifications()
            )
                .setDefaultValue(false)
                .setTooltip(Component.literal("Display HUD overlay notifications when toggling settings"))
                .setSaveConsumer { OmniBindConfig.setShowHudNotifications(it) }
                .build()
        )

        // Toggleable Settings
        val settings = SettingsRegistry.getAllSettings()
            .sortedWith(compareBy({ it.category }, { it.displayName }))

        val byCategory = settings.groupBy { it.category }

        for ((categoryName, settingsList) in byCategory) {
            val category = builder.getOrCreateCategory(Component.literal(categoryName))

            for (setting in settingsList) {
                val subCategory = entryBuilder.startSubCategory(Component.literal(setting.displayName))

                if (setting.description.isNotEmpty()) {
                    subCategory.setTooltip(Component.literal(setting.description))
                }

                val currentValue = try { setting.getValue() } catch (_: Exception) { false }
                val keybindEnabled = OmniBindConfig.isToggleEnabled(setting.id)

                // Keybind enabled toggle
                subCategory.add(
                    entryBuilder.startBooleanToggle(Component.literal("Keybind Enabled"), keybindEnabled)
                        .setDefaultValue(false)
                        .setTooltip(Component.literal("Enable keybind for this setting"))
                        .setSaveConsumer { enabled ->
                            OmniBindConfig.setToggleEnabled(setting.id, enabled)
                        }
                        .build()
                )

                // Current value toggle
                subCategory.add(
                    entryBuilder.startBooleanToggle(Component.literal("Value"), currentValue)
                        .setDefaultValue(false)
                        .setTooltip(Component.literal("Current value of this setting"))
                        .setSaveConsumer { newValue ->
                            try { setting.setValue(newValue) } catch (_: Exception) { }
                        }
                        .build()
                )

                val keybind = OmniBindConfig.getKeybind(setting.id)
                val currentKeyCode = keybindToModifierKeyCode(keybind)

                subCategory.add(
                    entryBuilder.startModifierKeyCodeField(Component.literal("Keybind"), currentKeyCode)
                        .setDefaultValue(ModifierKeyCode.unknown())
                        .setTooltip(Component.literal("Click and press a key to set the keybind"))
                        .setModifierSaveConsumer { newKeyCode ->
                            saveKeybind(setting.id, newKeyCode)
                        }
                        .build()
                )

                val notifDisabled = OmniBindConfig.isNotificationDisabled(setting.id)
                subCategory.add(
                    entryBuilder.startBooleanToggle(Component.literal("Disable Notifications"), notifDisabled)
                        .setDefaultValue(false)
                        .setTooltip(Component.literal("Disable toast/HUD notifications for this setting"))
                        .setSaveConsumer { disabled ->
                            OmniBindConfig.setNotificationDisabled(setting.id, disabled)
                        }
                        .build()
                )

                category.addEntry(subCategory.build())
            }
        }

        // Slider Settings
        val sliderSettings = SettingsRegistry.getAllSliderSettings()
            .sortedWith(compareBy({ it.category }, { it.displayName }))

        val slidersByCategory = sliderSettings.groupBy { it.category }

        for ((categoryName, slidersList) in slidersByCategory) {
            val category = builder.getOrCreateCategory(Component.literal(categoryName))

            for (slider in slidersList) {
                val subCategory = entryBuilder.startSubCategory(Component.literal(slider.displayName))

                if (slider.description.isNotEmpty()) {
                    subCategory.setTooltip(Component.literal(slider.description))
                }

                val currentValue = try { slider.getValue() } catch (_: Exception) { slider.min }
                val currentStep = slider.getStep()
                val isEnabled = OmniBindConfig.isSliderEnabled(slider.id)

                // Enable toggle for keybinds
                subCategory.add(
                    entryBuilder.startBooleanToggle(Component.literal("Keybinds Enabled"), isEnabled)
                        .setDefaultValue(false)
                        .setTooltip(Component.literal("Enable keybind controls for this slider"))
                        .setSaveConsumer { enabled ->
                            OmniBindConfig.setSliderEnabled(slider.id, enabled)
                        }
                        .build()
                )

                // Slider control
                subCategory.add(
                    entryBuilder.startDoubleField(Component.literal("Value"), currentValue)
                        .setDefaultValue(slider.min)
                        .setMin(slider.min)
                        .setMax(slider.max)
                        .setTooltip(Component.literal("Current value: ${String.format("%.2f", currentValue)}"))
                        .setSaveConsumer { newValue ->
                            try { slider.setValue(newValue) } catch (_: Exception) { }
                        }
                        .build()
                )

                // Step size configuration
                subCategory.add(
                    entryBuilder.startDoubleField(Component.literal("Step Size"), currentStep)
                        .setDefaultValue(slider.defaultStep)
                        .setMin(0.01)
                        .setMax(slider.max - slider.min)
                        .setTooltip(Component.literal("Amount to change per keypress (default: ${slider.defaultStep})"))
                        .setSaveConsumer { newStep ->
                            OmniBindConfig.setSliderStep(slider.id, newStep)
                        }
                        .build()
                )

                // Increment keybind
                val incKeybind = OmniBindConfig.getKeybind("${slider.id}.increment")
                val incKeyCode = keybindToModifierKeyCode(incKeybind)

                subCategory.add(
                    entryBuilder.startModifierKeyCodeField(Component.literal("Increment (+)"), incKeyCode)
                        .setDefaultValue(ModifierKeyCode.unknown())
                        .setTooltip(Component.literal("Keybind to increase value"))
                        .setModifierSaveConsumer { newKeyCode ->
                            saveKeybind("${slider.id}.increment", newKeyCode)
                        }
                        .build()
                )

                // Decrement keybind
                val decKeybind = OmniBindConfig.getKeybind("${slider.id}.decrement")
                val decKeyCode = keybindToModifierKeyCode(decKeybind)

                subCategory.add(
                    entryBuilder.startModifierKeyCodeField(Component.literal("Decrement (-)"), decKeyCode)
                        .setDefaultValue(ModifierKeyCode.unknown())
                        .setTooltip(Component.literal("Keybind to decrease value"))
                        .setModifierSaveConsumer { newKeyCode ->
                            saveKeybind("${slider.id}.decrement", newKeyCode)
                        }
                        .build()
                )

                val notifDisabled = OmniBindConfig.isNotificationDisabled(slider.id)
                subCategory.add(
                    entryBuilder.startBooleanToggle(Component.literal("Disable Notifications"), notifDisabled)
                        .setDefaultValue(false)
                        .setTooltip(Component.literal("Disable toast/HUD notifications for this slider"))
                        .setSaveConsumer { disabled ->
                            OmniBindConfig.setNotificationDisabled(slider.id, disabled)
                        }
                        .build()
                )

                category.addEntry(subCategory.build())
            }
        }

        builder.setDefaultBackgroundTexture(
            net.minecraft.resources.ResourceLocation.withDefaultNamespace("textures/block/dark_oak_planks.png")
        )

        return builder.build()
    }


    private fun keybindToModifierKeyCode(keybind: OmniBindConfig.KeybindData?): ModifierKeyCode {
        return if (keybind != null && !keybind.isUnbound()) {
            val inputKey = InputConstants.Type.KEYSYM.getOrCreate(keybind.keyCode)
            val modifier = Modifier.of(keybind.hasAlt(), keybind.hasCtrl(), keybind.hasShift())
            ModifierKeyCode.of(inputKey, modifier)
        } else {
            ModifierKeyCode.unknown()
        }
    }

    private fun saveKeybind(settingId: String, newKeyCode: ModifierKeyCode) {
        if (newKeyCode.isUnknown) {
            OmniBindConfig.clearKeybind(settingId)
        } else {
            var modifiers = 0
            if (newKeyCode.modifier.hasShift()) modifiers = modifiers or OmniBindConfig.KeybindData.MODIFIER_SHIFT
            if (newKeyCode.modifier.hasControl()) modifiers = modifiers or OmniBindConfig.KeybindData.MODIFIER_CTRL
            if (newKeyCode.modifier.hasAlt()) modifiers = modifiers or OmniBindConfig.KeybindData.MODIFIER_ALT

            val keybindData = OmniBindConfig.KeybindData(
                keyCode = newKeyCode.keyCode.value,
                modifiers = modifiers
            )
            OmniBindConfig.setKeybind(settingId, keybindData)
        }
    }
}

