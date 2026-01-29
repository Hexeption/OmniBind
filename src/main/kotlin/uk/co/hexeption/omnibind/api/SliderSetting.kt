package uk.co.hexeption.omnibind.api

import uk.co.hexeption.omnibind.config.OmniBindConfig

/**
 * Represents a numeric slider setting that can be controlled via keybind (increment/decrement).
 */
data class SliderSetting(
    val id: String,
    val displayName: String,
    val category: String,
    val getter: () -> Double,
    val setter: (Double) -> Unit,
    val min: Double = 0.0,
    val max: Double = 1.0,
    val defaultStep: Double = 0.1,
    val description: String = "",
    val modId: String? = null
) {
    fun getValue(): Double = getter()
    fun setValue(value: Double) = setter(value.coerceIn(min, max))

    fun getStep(): Double = OmniBindConfig.getSliderStep(id, defaultStep)

    fun increment(): Double {
        val newValue = (getValue() + getStep()).coerceIn(min, max)
        setValue(newValue)
        return newValue
    }

    fun decrement(): Double {
        val newValue = (getValue() - getStep()).coerceIn(min, max)
        setValue(newValue)
        return newValue
    }

    fun getSourceName(): String = modId ?: "Minecraft"
}

