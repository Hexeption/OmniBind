package uk.co.hexeption.omnibind.integration

import com.terraformersmc.modmenu.api.ConfigScreenFactory
import com.terraformersmc.modmenu.api.ModMenuApi
import net.minecraft.client.gui.screens.Screen
import uk.co.hexeption.omnibind.gui.ClothOmniBindScreen

/**
 * Mod Menu integration for OmniBind config screen.
 */
class ModMenuIntegration : ModMenuApi {
    override fun getModConfigScreenFactory(): ConfigScreenFactory<*> {
        return ConfigScreenFactory { parent: Screen? -> ClothOmniBindScreen.create(parent) }
    }
}
