package ch.asarix.stats.calculated;

import ch.asarix.stats.CalculatedStat;
import ch.asarix.stats.Weight;

public class CalculatedVoid implements CalculatedStat {
    @Override
    public long getTotalXp() {
        return 0;
    }

    @Override
    public int getLevel() {
        return 0;
    }

    @Override
    public double getLevelWithProgress() {
        return 0;
    }

    @Override
    public Weight getWeight() {
        return new Weight(0, 0);
    }
}
