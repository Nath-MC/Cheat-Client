package net.naxxsoftwares.mod.gui.screen;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.GridWidget;
import net.minecraft.text.Text;
import net.naxxsoftwares.mod.modules.Module;
import net.naxxsoftwares.mod.modules.Modules;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.TreeSet;

public class ModuleScreen extends Screen {

    private static final int COLUMNS = 5;

    public ModuleScreen(Text title) {
        super(title);
    }

    @Override
    public void render(@NotNull DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawText(this.textRenderer, "§d§lCheatMod§r", width / 2 - this.textRenderer.getWidth("CheatMod") / 2, 30 - this.textRenderer.fontHeight - 10, 0xFFFFFFFF, true);
    }

    @Override
    protected void init() {
        GridWidget gridWidget = new GridWidget();

        // Sort modules alphabetically by name
        TreeSet<Module> sortedModules = new TreeSet<>(Comparator.comparing(Module::getStringName));
        sortedModules.addAll(Modules.getAllModules());

        int row = 0;
        int col = 0;

        for (Module module : sortedModules) {
            gridWidget.add(module.getWidget(), row, col);
            col++;
            if (col == COLUMNS) {
                col = 0;
                row++;
            }
        }

        gridWidget.setRowSpacing(3);
        gridWidget.setColumnSpacing(8);
        gridWidget.refreshPositions();

        // Center gridWidget on screen
        gridWidget.setPosition(this.width / 2 - gridWidget.getWidth() / 2, this.height / 2 - gridWidget.getHeight() / 2);
        gridWidget.forEachChild(this::addDrawableChild);
    }
}
