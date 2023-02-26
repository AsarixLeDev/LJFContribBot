package ch.asarix.stats;

public interface Stat {
    String name();

    String niceName();

    CalculatedStat calculate(long xp);
}
