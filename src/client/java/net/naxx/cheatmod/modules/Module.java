package net.naxx.cheatmod.modules;


import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.packet.Packet;
import net.minecraft.text.Text;
import net.naxx.cheatmod.Initializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.Objects;

public abstract class Module {

    private static final Map<Class<? extends Module>, Module> moduleMap = new Object2ObjectArrayMap<>();
    protected final Text name;
    protected final Text description;
    protected final MinecraftClient client;
    protected final Logger LOGGER = LoggerFactory.getLogger(String.format("%s / %s", Initializer.MOD_NAME, this.getClass().getSimpleName()));
    protected final RunCategory runCategory;
    protected ClientPlayerEntity clientPlayer;
    protected ClientWorld clientWorld;
    protected ClientPlayNetworkHandler network;
    protected boolean active = false;

    public Module(String description, RunCategory runCategory) {
        this.name = Text.of(this.getClass().getSimpleName());
        this.description = Text.of(description);
        this.client = Initializer.client;
        this.runCategory = runCategory;

        Modules.addModule(this);
        moduleMap.put(this.getClass(), this);
    }

    public static boolean isActive(Class<? extends Module> clazz) {
        return moduleMap.get(clazz).isActive();
    }

    public Text getName() {
        return name;
    }

    public Text getDescription() {
        return description;
    }

    public boolean isActive() {
        return active;
    }

    public void toggle() {
        active = !active;
        if (active) onActivate();
        else onDeactivate();
    }

    public Logger getLogger() {
        return LOGGER;
    }

    public RunCategory getRunCategory() {
        return runCategory;
    }

    public void onSendPacket(Packet<?> packet, CallbackInfo ci) {
    }

    public void onWorldJoin() {
        this.clientPlayer = client.player;
        this.clientWorld = client.world;
        this.network = client.getNetworkHandler();
    }

    public void onWorldLeave() {
        this.clientPlayer = null;
        this.clientWorld = null;
        this.network = null;
    }

    public void onActivate() {
    }

    public void onDeactivate() {
    }

    public abstract void run();

    public ButtonWidget buildWidget() {
        return ButtonWidget.builder(this.getButtonMessage(), button -> {
            this.toggle();
            button.setMessage(this.getButtonMessage());
        }).size(50, 15).tooltip(Tooltip.of(this.getDescription())).build();
    }

    public ButtonWidget getWidget() {
        return buildWidget();
    }

    public Text getButtonMessage() {
        return Text.of(String.format("%s", this.isActive() ? String.format("§a%s§r", this.getName().getString()) : String.format("§c%s§r", this.getName().getString())));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Module module = (Module) o;
        return Objects.equals(name, module.name);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name);
    }

    public enum RunCategory {
        onStartingTick, onEndingTick, onStartingClientTick, onEndingClientTick,
    }

}
