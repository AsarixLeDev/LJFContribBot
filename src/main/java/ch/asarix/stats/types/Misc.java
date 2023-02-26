package ch.asarix.stats.types;

import ch.asarix.stats.CalculatedStat;
import ch.asarix.stats.Stat;
import ch.asarix.stats.calculated.CalculatedSbLevel;
import ch.asarix.stats.calculated.CalculatedVoid;

public enum Misc implements Stat {
    SB_LEVEL,
    AVERAGE,
    WEIGHT;

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
        };
    }
}
