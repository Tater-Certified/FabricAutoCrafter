package com.gitlab.essentialmods.fabricautocrafter;
import carpet.CarpetExtension;
import carpet.CarpetServer;
public class AutoCrafterTable implements CarpetExtension
{
    public static void noop() { }
    static
    {
        CarpetServer.manageExtension(new AutoCrafterTable());
    }
    @Override
    public void onGameStarted()
    {
        CraftingTableBlockEntity.init();
        // let's /carpet handle our few simple settings
        CarpetServer.settingsManager.parseSettingsClass(AutoCrafterSettings.class);
    }
}
