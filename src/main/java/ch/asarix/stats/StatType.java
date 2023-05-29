package ch.asarix.stats;

public interface StatType {
    String name();

    String niceName();

    CalculatedStat calculate(long xp);

    String formatLine(Stats stats);
}
