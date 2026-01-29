package uk.co.hexeption.omnibind.mixins;

import net.minecraft.client.OptionInstance;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(OptionInstance.class)
public interface OptionInstanceAccessor {

    @Accessor("caption")
    Component getCaption();

    @Accessor("value")
    Object getValues();
}
