package net.naxx.cheatmod.gui.screen;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.DirectionalLayoutWidget;
import net.minecraft.client.gui.widget.GridWidget;
import net.minecraft.client.gui.widget.TextIconButtonWidget;
import net.minecraft.text.Text;
import net.naxx.cheatmod.modules.ClickTP;
import net.naxx.cheatmod.modules.Fly;
import net.naxx.cheatmod.utils.chat.ChatUtils;

import java.util.concurrent.atomic.AtomicInteger;


public class ModuleScreen extends Screen {
    public static final MinecraftClient INSTANCE = MinecraftClient.getInstance();

    private final int STD_WIDTH = 95;
    private final int STD_HEIGHT = 19;

    public ModuleScreen(Text title) {
        super(title);
    }

    @Override
    protected void init() {
        GridWidget gridWidget = new GridWidget().setColumnSpacing(10);

        ClickTPWidgetBuilder(gridWidget);
        FlyWidgetBuilder(gridWidget);

        gridWidget.refreshPositions();
        gridWidget.setPosition(width / 2 - gridWidget.getWidth() / 2, height / 2 - gridWidget.getHeight() / 2);
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
        }).size(STD_WIDTH, STD_HEIGHT).tooltip(Tooltip.of(clickTP.getDESC())).build());

        //+ button
        clickTPWidgets.add(TextIconButtonWidget.WithText.builder(Text.of("+"), positioner -> {
            if (reach.get() == 30) return;
            clickTP.setReach(reach.incrementAndGet());

            ChatUtils.sendMessage("§lReach§r : " + reach + (reach.get() > 1 ? " blocks" : " block"));
        }).size(20, 20).build());

        //- button
        clickTPWidgets.add(TextIconButtonWidget.WithText.builder(Text.of("-"), positioner -> {
            if (reach.get() == 1) return;
            clickTP.setReach(reach.decrementAndGet());

            ChatUtils.sendMessage("§lReach§r : " + reach + (reach.get() > 1 ? " blocks" : " block"));
        }).size(20, 20).build());

        clickTPWidgets.refreshPositions();
        gridWidget.add(clickTPWidgets, 1, 1);
    }

    private void FlyWidgetBuilder(GridWidget gridWidget) {
        Fly fly = Fly.getINSTANCE();

        gridWidget.add(ButtonWidget.builder(Text.of(String.format("%s : %s", fly.getName(), fly.isModuleEnabled() ? "§aEnabled" : "§cDisabled")), button -> {
            fly.toogleModule();
            button.setMessage(Text.of(String.format("%s : %s", fly.getName(), fly.isModuleEnabled() ? "§aEnabled" : "§cDisabled")));
        }).size(95, this.textRenderer.fontHeight + 10).tooltip(Tooltip.of(fly.getDESC())).build(), 1, 2);
    }
}
