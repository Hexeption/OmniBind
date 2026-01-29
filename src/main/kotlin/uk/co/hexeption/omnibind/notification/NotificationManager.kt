package uk.co.hexeption.omnibind.notification

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.components.toasts.SystemToast
import net.minecraft.network.chat.Component
import org.slf4j.LoggerFactory
import uk.co.hexeption.omnibind.api.SliderSetting
import uk.co.hexeption.omnibind.api.ToggleableSetting
import uk.co.hexeption.omnibind.config.OmniBindConfig

/**
 * Manages toast and HUD notifications when settings are changed.
 */
object NotificationManager {

    private val LOGGER = LoggerFactory.getLogger("OmniBind/Notifications")
    private var currentHudNotification: HudNotification? = null

    data class HudNotification(
        val message: String, val isEnabled: Boolean, val expiryTime: Long
    ) {
        fun isExpired(): Boolean = System.currentTimeMillis() >= expiryTime
    }

    fun showToggleNotification(setting: ToggleableSetting, newValue: Boolean) {
        if (OmniBindConfig.showToasts()) showToast(setting.displayName, if (newValue) "§aON" else "§cOFF")
        if (OmniBindConfig.showHudNotifications()) showHudNotification(
            "${setting.displayName}: ${if (newValue) "ON" else "OFF"}", newValue
        )
    }

    fun showSliderNotification(slider: SliderSetting, newValue: Double) {
        val formattedValue = String.format("%.2f", newValue)
        if (OmniBindConfig.showToasts()) showToast(slider.displayName, "§b$formattedValue")
        if (OmniBindConfig.showHudNotifications()) showHudNotification("${slider.displayName}: $formattedValue", true)
    }

    private fun showToast(name: String, value: String) {
        try {
            val client = Minecraft.getInstance()
            val toastManager = client.toastManager
            val title = Component.translatable("omnibind.notification.title")
            val description = Component.literal("$name: $value")

            toastManager.addToast(
                SystemToast.multiline(
                    client, SystemToast.SystemToastId.PERIODIC_NOTIFICATION, title, description
                )
            )

            LOGGER.debug("Showed toast for $name: $value")
        } catch (e: Exception) {
            LOGGER.error("Failed to show toast notification", e)
        }
    }

    private fun showHudNotification(message: String, isEnabled: Boolean) {
        currentHudNotification = HudNotification(
            message = message, isEnabled = isEnabled, expiryTime = System.currentTimeMillis() + 2000
        )
    }

    fun getCurrentHudNotification(): HudNotification? {
        val notification = currentHudNotification
        if (notification != null && notification.isExpired()) {
            currentHudNotification = null
            return null
        }
        return notification
    }

    fun clearHudNotification() {
        currentHudNotification = null
    }
}
