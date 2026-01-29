package uk.co.hexeption.omnibind.gui

import com.mojang.blaze3d.platform.InputConstants
import me.shedaniel.clothconfig2.api.ConfigBuilder
import me.shedaniel.clothconfig2.api.Modifier
import me.shedaniel.clothconfig2.api.ModifierKeyCode
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component
import net.minecraft.resources.Identifier
import uk.co.hexeption.omnibind.config.OmniBindConfig
import uk.co.hexeption.omnibind.keybind.SettingsRegistry

/**
 * ClothConfig-based configuration screen for OmniBind settings and keybinds.
 */
object ClothOmniBindScreen {

    fun create(parent: Screen?): Screen {
        val builder =
            ConfigBuilder.create().setParentScreen(parent).setTitle(Component.translatable("omnibind.gui.title"))
                .setSavingRunnable { OmniBindConfig.save() }

        val entryBuilder = builder.entryBuilder()

        val generalCategory = builder.getOrCreateCategory(Component.translatable("omnibind.gui.category.general"))

        generalCategory.addEntry(
            entryBuilder.startBooleanToggle(
                Component.translatable("omnibind.gui.show_toasts"), OmniBindConfig.showToasts()
            ).setDefaultValue(true).setTooltip(Component.translatable("omnibind.gui.show_toasts.tooltip"))
                .setSaveConsumer { OmniBindConfig.setShowToasts(it) }.build()
        )

        generalCategory.addEntry(
            entryBuilder.startBooleanToggle(
                Component.translatable("omnibind.gui.show_hud_notifications"), OmniBindConfig.showHudNotifications()
            ).setDefaultValue(false).setTooltip(Component.translatable("omnibind.gui.show_hud_notifications.tooltip"))
                .setSaveConsumer { OmniBindConfig.setShowHudNotifications(it) }.build()
        )

        // Toggleable Settings
        val settings = SettingsRegistry.getAllSettings().sortedWith(compareBy({ it.category }, { it.displayName }))
        val byCategory = settings.groupBy { it.category }

        for ((categoryName, settingsList) in byCategory) {
            val catComponent = Component.translatable("omnibind.category.$categoryName", categoryName)
            val category = builder.getOrCreateCategory(catComponent)

            for (setting in settingsList) {
                val subCategory =
                    entryBuilder.startSubCategory(Component.translatable("omnibind.setting.name", setting.displayName))

                if (setting.description.isNotEmpty()) {
                    subCategory.setTooltip(Component.translatable("omnibind.setting.desc", setting.description))
                }

                val currentValue = try {
                    setting.getValue()
                } catch (_: Exception) {
                    false
                }
                val keybindEnabled = OmniBindConfig.isToggleEnabled(setting.id)

                // Keybind enabled toggle
                subCategory.add(
                    entryBuilder.startBooleanToggle(
                        Component.translatable("omnibind.gui.keybind_enabled"), keybindEnabled
                    ).setDefaultValue(false).setTooltip(Component.translatable("omnibind.gui.keybind_enabled.tooltip"))
                        .setSaveConsumer { enabled -> OmniBindConfig.setToggleEnabled(setting.id, enabled) }.build()
                )

                // Current value toggle
                subCategory.add(
                    entryBuilder.startBooleanToggle(
                        Component.translatable("omnibind.gui.value"), currentValue
                    ).setDefaultValue(false).setTooltip(Component.translatable("omnibind.gui.value.tooltip"))
                        .setSaveConsumer { newValue ->
                            try {
                                setting.setValue(newValue)
                            } catch (_: Exception) {
                            }
                        }.build()
                )

                val keybind = OmniBindConfig.getKeybind(setting.id)
                val currentKeyCode = keybindToModifierKeyCode(keybind)

                subCategory.add(
                    entryBuilder.startModifierKeyCodeField(
                        Component.translatable("omnibind.gui.keybind"), currentKeyCode
                    ).setDefaultValue(ModifierKeyCode.unknown())
                        .setTooltip(Component.translatable("omnibind.gui.keybind.tooltip"))
                        .setModifierSaveConsumer { newKeyCode -> saveKeybind(setting.id, newKeyCode) }.build()
                )

                val notifDisabled = OmniBindConfig.isNotificationDisabled(setting.id)
                subCategory.add(
                    entryBuilder.startBooleanToggle(
                    Component.translatable("omnibind.gui.disable_notifications"), notifDisabled
                ).setDefaultValue(false)
                    .setTooltip(Component.translatable("omnibind.gui.disable_notifications.tooltip"))
                    .setSaveConsumer { disabled -> OmniBindConfig.setNotificationDisabled(setting.id, disabled) }
                    .build())

                category.addEntry(subCategory.build())
            }
        }

        // Slider Settings
        val sliderSettings =
            SettingsRegistry.getAllSliderSettings().sortedWith(compareBy({ it.category }, { it.displayName }))
        val slidersByCategory = sliderSettings.groupBy { it.category }

        for ((categoryName, slidersList) in slidersByCategory) {
            val catComponent = Component.translatable("omnibind.category.$categoryName", categoryName)
            val category = builder.getOrCreateCategory(catComponent)

            for (slider in slidersList) {
                val subCategory =
                    entryBuilder.startSubCategory(Component.translatable("omnibind.slider.name", slider.displayName))

                if (slider.description.isNotEmpty()) {
                    subCategory.setTooltip(Component.translatable("omnibind.slider.desc", slider.description))
                }

                val currentValue = try {
                    slider.getValue()
                } catch (_: Exception) {
                    slider.min
                }
                val currentStep = slider.getStep()
                val isEnabled = OmniBindConfig.isSliderEnabled(slider.id)

                // Enable toggle for slider keybind controls
                subCategory.add(
                    entryBuilder.startBooleanToggle(
                        Component.translatable("omnibind.gui.slider_keybinds_enabled"), isEnabled
                    ).setDefaultValue(false)
                        .setTooltip(Component.translatable("omnibind.gui.slider_keybinds_enabled.tooltip"))
                        .setSaveConsumer { enabled -> OmniBindConfig.setSliderEnabled(slider.id, enabled) }.build()
                )

                // Slider control (numeric field)
                subCategory.add(
                    entryBuilder.startDoubleField(
                        Component.translatable("omnibind.gui.value"), currentValue
                    ).setDefaultValue(slider.min).setMin(slider.min).setMax(slider.max).setTooltip(
                        Component.translatable(
                            "omnibind.gui.slider_value.tooltip", String.format("%.2f", currentValue)
                        )
                    ).setSaveConsumer { newValue ->
                        try {
                            slider.setValue(newValue)
                        } catch (_: Exception) {
                        }
                    }.build()
                )

                // Step size configuration
                subCategory.add(
                    entryBuilder.startDoubleField(
                        Component.translatable("omnibind.gui.step_size"), currentStep
                    ).setDefaultValue(slider.defaultStep).setMin(0.01).setMax(slider.max - slider.min).setTooltip(
                        Component.translatable(
                            "omnibind.gui.step_size.tooltip", String.format("%.2f", slider.defaultStep)
                        )
                    ).setSaveConsumer { newStep -> OmniBindConfig.setSliderStep(slider.id, newStep) }.build()
                )

                // Increment keybind
                val incKeybind = OmniBindConfig.getKeybind("${slider.id}.increment")
                val incKeyCode = keybindToModifierKeyCode(incKeybind)

                subCategory.add(
                    entryBuilder.startModifierKeyCodeField(
                    Component.translatable("omnibind.gui.increment"), incKeyCode
                ).setDefaultValue(ModifierKeyCode.unknown())
                    .setTooltip(Component.translatable("omnibind.gui.increment.tooltip"))
                    .setModifierSaveConsumer { newKeyCode -> saveKeybind("${slider.id}.increment", newKeyCode) }
                    .build())

                // Decrement keybind
                val decKeybind = OmniBindConfig.getKeybind("${slider.id}.decrement")
                val decKeyCode = keybindToModifierKeyCode(decKeybind)

                subCategory.add(
                    entryBuilder.startModifierKeyCodeField(
                    Component.translatable("omnibind.gui.decrement"), decKeyCode
                ).setDefaultValue(ModifierKeyCode.unknown())
                    .setTooltip(Component.translatable("omnibind.gui.decrement.tooltip"))
                    .setModifierSaveConsumer { newKeyCode -> saveKeybind("${slider.id}.decrement", newKeyCode) }
                    .build())

                val notifDisabled = OmniBindConfig.isNotificationDisabled(slider.id)
                subCategory.add(
                    entryBuilder.startBooleanToggle(
                    Component.translatable("omnibind.gui.disable_notifications"), notifDisabled
                ).setDefaultValue(false)
                    .setTooltip(Component.translatable("omnibind.gui.disable_notifications.tooltip"))
                    .setSaveConsumer { disabled -> OmniBindConfig.setNotificationDisabled(slider.id, disabled) }
                    .build())

                category.addEntry(subCategory.build())
            }
        }

        builder.setDefaultBackgroundTexture(Identifier.withDefaultNamespace("textures/block/dark_oak_planks.png"))

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
                keyCode = newKeyCode.keyCode.value, modifiers = modifiers
            )
            OmniBindConfig.setKeybind(settingId, keybindData)
        }
    }
}
