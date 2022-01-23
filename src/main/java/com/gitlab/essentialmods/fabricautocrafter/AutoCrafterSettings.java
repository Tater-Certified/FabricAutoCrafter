package com.gitlab.essentialmods.fabricautocrafter;

import carpet.settings.Rule;
import static carpet.settings.RuleCategory.CREATIVE;

public class AutoCrafterSettings
{
    @Rule(desc = "Adds a separate autocrafter instead of overriding the vanilla crafting table. Very useful with GeyserMC players." +
            "SeparateAutoCrafter", category = {CREATIVE, "extras"})
    public static boolean AutoCrafter = false;
}
