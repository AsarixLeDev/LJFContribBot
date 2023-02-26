package ch.asarix.stats;

public interface CalculatedStat {
    long getTotalXp();

    int getLevel();

    double getLevelWithProgress();

    Weight getWeight();
}
