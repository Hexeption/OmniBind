package uk.co.hexeption.omnibind.mixins;

import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.input.KeyEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import uk.co.hexeption.omnibind.keybind.KeybindHandler;

/**
 * Mixin to intercept keyboard input for OmniBind's dynamic keybinds.
 *
 * This hooks into Minecraft's KeyboardHandler to capture key events
 * before they are processed by the game, allowing OmniBind to handle
 * custom keybind triggers.
 */
@Mixin(KeyboardHandler.class)
public class KeyboardHandlerMixin {

    /**
     * Intercepts key press events to process OmniBind keybinds.
     *
     * @param window The GLFW window handle
     * @param key The GLFW key code
     * @param scancode The platform-specific scancode
     * @param action GLFW action (PRESS, RELEASE, REPEAT)
     * @param modifiers Modifier key flags
     * @param ci Callback info
     */
    @Inject(
        method = "keyPress",
        at = @At("HEAD"),
        cancellable = true
    )
    private void omnibind$onKeyPress(long window, int action, KeyEvent event, CallbackInfo ci) {
        // Process the key event through OmniBind's handler
        // If it returns true, the event was consumed by OmniBind
        if (KeybindHandler.INSTANCE.onKeyEvent(event.key(), action)) {
            // Don't cancel here - we want the game to still process normal keybinds
            // This allows OmniBind keybinds to work alongside vanilla keybinds
        }
    }
}
