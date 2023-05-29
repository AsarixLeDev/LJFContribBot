package ch.asarix.stats.types;

import ch.asarix.Util;
import ch.asarix.stats.CalculatedStat;
import ch.asarix.stats.StatType;
import ch.asarix.stats.Stats;
import ch.asarix.stats.calculated.CalculatedDungeon;

import java.text.NumberFormat;
import java.util.Locale;

public enum DungeonType implements StatType {
    CATACOMBS(0.0002149604615),
    HEALER(0.0000045254834),
    MAGE(0.0000045254834),
    BERSERK(0.0000045254834),
    ARCHER(0.0000045254834),
    TANK(0.0000045254834);

    final double modifier;

    DungeonType(double modifier) {
        this.modifier = modifier;
    }

    public CalculatedStat calculate(long xp) {
        return new CalculatedDungeon(xp, modifier);
    }

    @Override
    public String niceName() {
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
