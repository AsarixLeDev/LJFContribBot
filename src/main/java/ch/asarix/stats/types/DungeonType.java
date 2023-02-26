package ch.asarix.stats.types;

import ch.asarix.Util;
import ch.asarix.stats.CalculatedStat;
import ch.asarix.stats.Stat;
import ch.asarix.stats.calculated.CalculatedDungeon;

public enum DungeonType implements Stat {
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
}
