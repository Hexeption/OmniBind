package uk.co.hexeption.omnibind.gui

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements
import net.minecraft.client.DeltaTracker
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.resources.Identifier
import uk.co.hexeption.omnibind.notification.NotificationManager

/**
 * Renders HUD notifications when settings are toggled via keybind.
 */
object HudRenderer {

    private val HUD_LAYER_ID = Identifier.fromNamespaceAndPath("omnibind", "notification_hud")

    fun register() {
        HudElementRegistry.attachElementBefore(
            VanillaHudElements.CHAT, HUD_LAYER_ID, ::render
        )
    }

    private fun render(guiGraphics: GuiGraphics, tickCounter: DeltaTracker) {
        val notification = NotificationManager.getCurrentHudNotification() ?: return

        val minecraft = Minecraft.getInstance()
        val font = minecraft.font

        if (minecraft.screen != null) return

        val screenWidth = minecraft.window.guiScaledWidth
        val screenHeight = minecraft.window.guiScaledHeight

        val text = notification.message
        val textWidth = font.width(text)

        val x = (screenWidth - textWidth) / 2
        val y = screenHeight / 2 - 30

        val padding = 4
        val bgColor = if (notification.isEnabled) 0x80006600 else 0x80660000
        guiGraphics.fill(
            x - padding, y - padding, x + textWidth + padding, y + 9 + padding, bgColor.toInt()
        )

        val borderColor = if (notification.isEnabled) 0xFF00FF00.toInt() else 0xFFFF0000.toInt()
        val left = x - padding
        val top = y - padding
        val right = x + textWidth + padding
        val bottom = y + 9 + padding

        guiGraphics.fill(left, top, right, top + 1, borderColor)
        guiGraphics.fill(left, bottom - 1, right, bottom, borderColor)
        guiGraphics.fill(left, top, left + 1, bottom, borderColor)
        guiGraphics.fill(right - 1, top, right, bottom, borderColor)

        val textColor = if (notification.isEnabled) 0xFF55FF55.toInt() else 0xFFFF5555.toInt()
        guiGraphics.drawString(font, text, x, y, textColor)
    }
}
