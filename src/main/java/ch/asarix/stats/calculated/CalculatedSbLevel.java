package ch.asarix.stats.calculated;

import ch.asarix.stats.CalculatedStat;
import ch.asarix.stats.Weight;

public class CalculatedSbLevel implements CalculatedStat {

    long xp;
    int level;
    double levelWithProgress;

    public CalculatedSbLevel(long xp) {
        this.xp = xp;
        levelWithProgress = xp / 100d;
        level = (int) Math.floor(levelWithProgress);
    }

    @Override
    public Weight getWeight() {
        return new Weight(0, 0);
    }

    @Override
    public long getTotalXp() {
        return xp;
    }

    @Override
    public int getLevel() {
        return level;
    }

    @Override
    public double getLevelWithProgress() {
        return levelWithProgress;
    }
}
