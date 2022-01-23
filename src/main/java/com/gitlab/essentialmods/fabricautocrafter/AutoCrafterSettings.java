package com.gitlab.essentialmods.fabricautocrafter;

import carpet.settings.Rule;
import static carpet.settings.RuleCategory.CREATIVE;

public class AutoCrafterSettings
{
    @Rule(desc = "AutoCrafter that doesn't replace the vanilla one.", category = {CREATIVE, "extras"})
    public static boolean autoCrafter = false;
}
