package net.naxx.cheatmod.gui.screen;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.GridWidget;
import net.minecraft.client.gui.widget.SimplePositioningWidget;
import net.minecraft.text.Text;

public class ModuleScreen extends Screen {
    public ModuleScreen(Text title) {
        super(title);
    }

    @Override
    protected void init() {
        ModulesCategoriesMenu modulesCategoriesMenu = new ModulesCategoriesMenu();
        GridWidget gridWidget = modulesCategoriesMenu.getGridWidget();

        gridWidget.setPosition(this.width / 2, this.height / 2);
        SimplePositioningWidget.setPos(gridWidget, 0, 0, this.width, this.height, 0.5F, 0.25F);

        gridWidget.forEachChild(this::addDrawableChild);
//        this.applyBlur(0.3f);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawText(this.textRenderer, "Modules", width / 2 - this.textRenderer.getWidth("Modules") / 2 - 1, 30 - this.textRenderer.fontHeight - 10, 0xFFFFFFFF, true);
    }
}
