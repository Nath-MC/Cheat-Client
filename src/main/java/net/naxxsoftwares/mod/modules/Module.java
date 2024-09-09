package net.naxxsoftwares.mod.modules;


import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.naxxsoftwares.mod.Initializer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public abstract class Module {

    protected final @NotNull Text name;
    protected final Text description;
    protected final MinecraftClient client;
    protected final Logger LOGGER;
    protected boolean active;

    public Module(String description) {
        this.name = Text.of(this.getClass().getSimpleName());
        this.description = Text.of(description);
        this.client = Initializer.client;
        this.LOGGER = LoggerFactory.getLogger(String.format("%s / %s", Initializer.MOD_NAME, this.getName().getString()));
        this.active = false;
    }

    public @NotNull Text getName() {
        return name;
    }

    public static @Nullable String getStringName(@NotNull Module module) {
        return getStringName(module.getClass());
    }

    public static @Nullable String getStringName(Class<? extends Module> clazz) {
        Module module = Modules.getModuleByClass(clazz);
        if (Objects.isNull(module)) return null;
        return module.getName().getString();
    }

    public ButtonWidget getWidget() {
        return buildWidget();
    }

    public ButtonWidget buildWidget() {
        return ButtonWidget.builder(this.getButtonMessage(), button -> {
            this.toggle();
            button.setMessage(this.getButtonMessage());
        }).size(50, 15).tooltip(Tooltip.of(this.getDescription())).build();
    }

    public Text getDescription() {
        return description;
    }

    public void toggle() {
        active = !active;
        if (active) onActivate();
        else onDeactivate();
    }

    public void onActivate() {}

    public void onDeactivate() {}

    public @NotNull Text getButtonMessage() {
        String colorCode = isActive() ? "§a" : "§c";
        return Text.of(String.format("%s%s§r", colorCode, this.getName().getString()));
    }

    public boolean isActive() {
        return active;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name);
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Module module = (Module) o;
        return Objects.equals(name, module.name);
    }
}
