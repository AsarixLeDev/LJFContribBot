package ch.asarix.stats.types;

import ch.asarix.Util;
import ch.asarix.stats.CalculatedStat;
import ch.asarix.stats.Stat;
import ch.asarix.stats.Stats;
import ch.asarix.stats.calculated.CalculatedSkill;

import java.text.NumberFormat;
import java.util.Locale;

public enum Skill implements Stat {
    MINING(1.18207448, 259634, 60, false),
    FORAGING(1.232826, 259634, 50, false),
    ENCHANTING(0.96976583, 882758, 60, false),
    FARMING(1.217848139, 220689, 60, false),
    COMBAT(1.15797687265, 275862, 60, false),
    FISHING(1.406418, 88274, 50, false),
    ALCHEMY(1.0, 1103448, 50, false),
    TAMING(1.14744, 441379, 50, false),
    CARPENTRY(0, 0, 50, false),
    SOCIAL2(0, 0, 50, true),
    RUNECRAFTING(0, 0, 25, true);

    final double exp;
    final int div;
    final int cap;
    final boolean cosmetic;

    Skill(double exp, int div, int cap, boolean cosmetic) {
        this.exp = exp;
        this.div = div;
        this.cap = cap;
        this.cosmetic = cosmetic;
    }

    public CalculatedStat calculate(long xp) {
        return new CalculatedSkill(xp, exp, div, cap, this);
    }

    public boolean isCosmetic() {
        return cosmetic;
    }

    @Override
    public String niceName() {
        if (this == SOCIAL2)
            return "Social";
        return Util.firstCap(name());
    }

    @Override
    public String formatLine(Stats stats) {
        long xp = stats.getTotalXp(this);
        double level = stats.getLevelWithProgress(this);
        NumberFormat nf = NumberFormat.getInstance(new Locale("en", "US"));
        return " [" + Util.round(level, 2) + "] **[" + nf.format(xp) + "]**";
    }
}
