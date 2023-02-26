package ch.asarix.stats.types;

import ch.asarix.stats.CalculatedStat;
import ch.asarix.stats.Stat;
import ch.asarix.stats.calculated.CalculatedSlayer;

public enum Slayer implements Stat {
    WOLF(1962, 0.015),
    SPIDER(2118, 0.08),
    ZOMBIE(2208, 0.15),
    ENDERMAN(1430, 0.017),
    BLAZE(0, 0);

    final int div;
    final double modifier;

    Slayer(int div, double modifier) {
        this.modifier = modifier;
        this.div = div;
    }

    public CalculatedStat calculate(long xp) {
        return new CalculatedSlayer(xp, div, modifier, this);
    }

    @Override
    public String niceName() {
        return switch (this) {
            case WOLF -> "Sven slayer";
            case SPIDER -> "Tarantula slayer";
            case ZOMBIE -> "Revenant slayer";
            case ENDERMAN -> "Enderman slayer";
            case BLAZE -> "Blaze slayer";
        };
    }
}