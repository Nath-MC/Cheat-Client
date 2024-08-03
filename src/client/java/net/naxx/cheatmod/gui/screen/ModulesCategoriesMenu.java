package net.naxx.cheatmod.gui.screen;

import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.GridWidget;
import net.minecraft.text.Text;
import net.naxx.cheatmod.modules.ClickTP;


public class ModulesCategoriesMenu {
    public GridWidget gridWidget;
    public GridWidget.Adder adder;

    public ModulesCategoriesMenu() {

        gridWidget = new GridWidget().setColumnSpacing(20);
        adder = gridWidget.createAdder(1);

        //ClickTP
        ClickTP clickTP = ClickTP.getInstance();
        adder.add(ButtonWidget.builder(Text.of(clickTP.getName() + " : " + (clickTP.isModuleEnabled() ? "Enabled" : "Disabled")), button -> {
            clickTP.toggleModule();
            button.setMessage(Text.of(clickTP.getName() + " : " + (clickTP.isModuleEnabled() ? "Enabled" : "Disabled")));
        }).build());

        gridWidget.refreshPositions();
    }


    public GridWidget getGridWidget() {
        return gridWidget;
    }
}
