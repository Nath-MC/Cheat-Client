package net.naxx.cheatmod.mixin.client;

import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.naxx.cheatmod.gui.screen.ModuleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameMenuScreen.class)
public class ModOptionsScreenMixin extends Screen {
    protected ButtonWidget buttonWidget = ButtonWidget.builder(Text.of("Modules"), button -> {
        this.client.setScreen(new ModuleScreen(Text.of("ModuleScreen")));
    }).dimensions(10, 10, 75, ButtonWidget.DEFAULT_HEIGHT).build();

    protected ModOptionsScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "initWidgets", at = @At("TAIL"))
    protected void addButton(CallbackInfo ci) {
        this.addDrawableChild(buttonWidget);
    }
}