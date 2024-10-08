package net.naxxsoftwares.mod.mixins;

import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.naxxsoftwares.mod.gui.screen.ModuleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameMenuScreen.class)
public abstract class GameMenuScreenMixin extends Screen {
    @Unique
    protected final ButtonWidget buttonWidget =
            ButtonWidget.builder(Text.of("§dCheatMod§r"), button -> this.client.setScreen(new ModuleScreen(Text.of("Cheatmod")))).dimensions(10, 10, 75, ButtonWidget.DEFAULT_HEIGHT).build();

    protected GameMenuScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "initWidgets", at = @At("TAIL"))
    protected void addButton(CallbackInfo ci) {
        this.addDrawableChild(buttonWidget);
    }
}