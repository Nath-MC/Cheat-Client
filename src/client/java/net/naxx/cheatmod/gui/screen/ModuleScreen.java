package net.naxx.cheatmod.gui.screen;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.GridWidget;
import net.minecraft.text.Text;
import net.naxx.cheatmod.modules.Module;
import net.naxx.cheatmod.modules.Modules;

import java.util.Comparator;
import java.util.TreeSet;


public class ModuleScreen extends Screen {

    private static final GridWidget gridWidget = new GridWidget();
    private static final int[] indexes = {0, 0};

    static {
        Comparator<Module> moduleComparator = Comparator.comparing(module -> module.getName().getString());
        TreeSet<Module> sortedModuleSet = new TreeSet<>(moduleComparator);

        for (Module module : new Modules()) sortedModuleSet.add(module);

        for (Module module : sortedModuleSet) {
            if (indexes[0] == 5) {
                indexes[0] = 1; // Columns
                indexes[1]++;   // Rows
            } else indexes[0]++;

            gridWidget.add(module.getWidget(), indexes[1], indexes[0]);
        }
        gridWidget.setRowSpacing(3);
        gridWidget.setColumnSpacing(8);
        gridWidget.refreshPositions();
    }

    public ModuleScreen(Text title) {
        super(title);
    }

    @Override
    protected void init() {
        gridWidget.setPosition(this.width / 2 - gridWidget.getWidth() / 2, this.height / 2 - gridWidget.getHeight() / 2);
        gridWidget.refreshPositions();
        gridWidget.forEachChild(this::addDrawableChild);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawText(this.textRenderer, "§d§lCheatMod§r", width / 2 - this.textRenderer.getWidth("CheatMod") / 2, 30 - this.textRenderer.fontHeight - 10, 0xFFFFFFFF, true);
    }

}
