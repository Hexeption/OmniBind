package uk.co.hexeption.omnibind.integration

import com.terraformersmc.modmenu.api.ConfigScreenFactory
import com.terraformersmc.modmenu.api.ModMenuApi
import uk.co.hexeption.omnibind.gui.ClothOmniBindScreen
import net.minecraft.client.gui.screens.Screen

/**
 * Mod Menu integration for OmniBind config screen.
 */
class ModMenuIntegration : ModMenuApi {
    override fun getModConfigScreenFactory(): ConfigScreenFactory<*> {
        return ConfigScreenFactory { parent: Screen? -> ClothOmniBindScreen.create(parent) }
    }
}
