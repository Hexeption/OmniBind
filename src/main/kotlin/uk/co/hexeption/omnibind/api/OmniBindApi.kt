package uk.co.hexeption.omnibind.api

/**
 * API for other mods to register toggleable settings with OmniBind.
 */
object OmniBindApi {

    private val pendingRegistrations = mutableListOf<ToggleableSetting>()
    private var registrationCallback: ((ToggleableSetting) -> Unit)? = null

    fun registerSetting(setting: ToggleableSetting): Boolean {
        return try {
            registrationCallback?.invoke(setting) ?: run {
                pendingRegistrations.add(setting)
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    fun registerSetting(id: String, builder: SettingBuilder.() -> Unit): Boolean {
        val settingBuilder = SettingBuilder(id)
        builder(settingBuilder)
        return registerSetting(settingBuilder.build())
    }

    fun registerSettings(vararg settings: ToggleableSetting): Int {
        return settings.count { registerSetting(it) }
    }

    fun unregisterSetting(settingId: String): Boolean {
        pendingRegistrations.removeIf { it.id == settingId }
        return true
    }

    internal fun setRegistrationCallback(callback: (ToggleableSetting) -> Unit) {
        registrationCallback = callback
        pendingRegistrations.forEach(callback)
        pendingRegistrations.clear()
    }

    internal fun getPendingRegistrations(): List<ToggleableSetting> = pendingRegistrations.toList()

    class SettingBuilder(private val id: String) {
        var displayName: String = id
        var category: String = "Misc"
        var getter: (() -> Boolean)? = null
        var setter: ((Boolean) -> Unit)? = null
        var description: String = ""
        var modId: String? = null

        internal fun build(): ToggleableSetting {
            requireNotNull(getter) { "getter must be set for setting '$id'" }
            requireNotNull(setter) { "setter must be set for setting '$id'" }

            return ToggleableSetting(
                id = id,
                displayName = displayName,
                category = category,
                getter = getter!!,
                setter = setter!!,
                description = description,
                modId = modId
            )
        }
    }
}

