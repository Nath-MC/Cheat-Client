package net.naxx.cheatmod.gui.screen;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.*;
import net.minecraft.text.Text;
import net.naxx.cheatmod.modules.ClickTP;
import net.naxx.cheatmod.utils.chat.ChatUtils;

import java.util.concurrent.atomic.AtomicInteger;


public class ModuleScreen extends Screen {
    public static final MinecraftClient INSTANCE = MinecraftClient.getInstance();

    public ModuleScreen(Text title) {
        super(title);
    }

    @Override
    protected void init() {
        GridWidget gridWidget = new GridWidget();


        ClickTPWidgetBuilder(gridWidget);

        gridWidget.refreshPositions();
        gridWidget.setPosition(width/2 - gridWidget.getWidth()/2, height/2 - gridWidget.getHeight()/2);
        gridWidget.forEachChild(this::addDrawableChild);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawText(this.textRenderer, "Modules", width / 2 - this.textRenderer.getWidth("Modules") / 2 - 1, 30 - this.textRenderer.fontHeight - 10, 0xFFFFFFFF, true);
    }

    private void ClickTPWidgetBuilder(GridWidget gridWidget) {
        ClickTP clickTP = ClickTP.getInstance();
        AtomicInteger reach = new AtomicInteger(clickTP.getReach());

        DirectionalLayoutWidget clickTPWidgets = new DirectionalLayoutWidget(INSTANCE.getWindow().getWidth() / 2, INSTANCE.getWindow().getHeight() / 2, DirectionalLayoutWidget.DisplayAxis.HORIZONTAL).spacing(8);

        //Toggleable button
        clickTPWidgets.add(ButtonWidget.builder(Text.of(clickTP.getName() + " : " + (clickTP.isModuleEnabled() ? "§aEnabled" : "§cDisabled")), button -> {
            clickTP.toggleModule();
            button.setMessage(Text.of(clickTP.getName() + " : " + (clickTP.isModuleEnabled() ? "§aEnabled" : "§cDisabled")));
        }).size(95, this.textRenderer.fontHeight + 10).tooltip(Tooltip.of(Text.of("Teleport yourself toward a block"))).build());

        //+ button
        clickTPWidgets.add(TextIconButtonWidget.WithText.builder(Text.of("+"), positioner -> {
            if (reach.get() == 30) return;
            clickTP.setReach(reach.incrementAndGet());

            ChatUtils.sendMessage("§nReach§r -> " + reach + (reach.get() > 1 ? " blocks" : " block"));
        }).size(20,20).build());

        //- button
        clickTPWidgets.add(TextIconButtonWidget.WithText.builder(Text.of("-"), positioner -> {
            if (reach.get() == 1) return;
            clickTP.setReach(reach.decrementAndGet());

            ChatUtils.sendMessage("§nReach§r -> " + reach + (reach.get() > 1 ? " blocks" : " block"));
        }).size(20,20).build());

        clickTPWidgets.refreshPositions();
        gridWidget.add(clickTPWidgets, 1, 1);
    }
}
