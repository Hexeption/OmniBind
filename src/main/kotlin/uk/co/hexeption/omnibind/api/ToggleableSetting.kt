package uk.co.hexeption.omnibind.api

/**
 * Represents a toggleable boolean setting that can be controlled via keybind.
 */
data class ToggleableSetting(
    val id: String,
    val displayName: String,
    val category: String,
    val getter: () -> Boolean,
    val setter: (Boolean) -> Unit,
    val description: String = "",
    val modId: String? = null,
    val translationKey: String? = null
) {
    fun getValue(): Boolean = getter()
    fun setValue(value: Boolean) = setter(value)

    fun toggle(): Boolean {
        val newValue = !getValue()
        setValue(newValue)
        return newValue
    }

    fun getSourceName(): String = modId ?: "Minecraft"
}
