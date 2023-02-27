package ch.asarix.stats.types;

import ch.asarix.Util;
import ch.asarix.stats.CalculatedStat;
import ch.asarix.stats.Stat;
import ch.asarix.stats.Stats;
import ch.asarix.stats.calculated.CalculatedSbLevel;
import ch.asarix.stats.calculated.CalculatedVoid;

public enum Misc implements Stat {
    SB_LEVEL,
    AVERAGE,
    WEIGHT,
    DJ_CLASS;

    @Override
    public CalculatedStat calculate(long xp) {
        return switch (this) {
            case SB_LEVEL -> new CalculatedSbLevel(xp);
            default -> new CalculatedVoid();
        };
    }

    @Override
    public String niceName() {
        return switch (this) {
            case SB_LEVEL -> "Skyblock Level";
            case AVERAGE -> "Skill average";
            case WEIGHT -> "Senither weight";
            case DJ_CLASS -> "Best dungeon class";
        };
    }

    @Override
    public String formatLine(Stats stats) {
        double level = stats.getLevelWithProgress(this);
        if (this == DJ_CLASS) {
            String className = stats.getBestClass().niceName();
            return "**[" + Util.round(level, 2) + "] [" + className + "]**";
        }
        return "**[" + Util.round(level, 2) + "]**";
    }
}
