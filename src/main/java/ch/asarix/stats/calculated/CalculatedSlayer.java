package ch.asarix.stats.calculated;

import ch.asarix.stats.CalculatedStat;
import ch.asarix.stats.Weight;
import ch.asarix.stats.types.Slayer;

import java.util.LinkedHashMap;
import java.util.LinkedList;

public class CalculatedSlayer implements CalculatedStat {

    public Weight totalWeight;
    long xp;
    double div;
    double modifier;
    int level;
    long excess;
    double xpForNext;
    double progress;
    double levelWithProgress;
    double unlockableLevelWithProgress;
    int levelCap;
    int uncappedLevel;
    long xpCurrent;
    long remaining;
    double baseWeight;
    double excessWeight;
    Slayer slayerType;

    public CalculatedSlayer(long xp, double div, double modifier, Slayer slayerType) {
        this.xp = xp;
        this.div = div;
        this.modifier = modifier;
        this.slayerType = slayerType;

        this.levelCap = 9;
        calculateWeight(xp);
        calculate(xp);
    }

    public void calculateWeight(long xp) {
        if (xp <= 1000000) {
            totalWeight = new Weight((float) xp / this.div, 0);
            return;
        }
        double base = 1000000.0 / div;
        long remaining = xp - 1000000;

        double overflow = 0;
        double modifier = this.modifier;

        while (remaining > 0) {
            double left = Math.min(remaining, 1000000);

            overflow += Math.pow(left / (div * (1.5 + modifier)), 0.942);
            modifier += this.modifier;
            remaining -= left;
        }
        baseWeight = base;
        excessWeight = overflow;
        totalWeight = new Weight(baseWeight, excessWeight);
    }

    private void calculate(long xp) {
        /* the level ignoring the cap and using only the table */
        uncappedLevel = -1;

        /* the amount of xp over the amount required for the level (used for calculation progress to next level) */
        xpCurrent = xp;

        /* like xpCurrent but ignores cap */
        remaining = xp;

        for (int i = 0; i <= levelCap; i++) {
            int xpRequired = xpFromIndex(i);
            if (remaining > xpRequired) {
                uncappedLevel++;
                remaining -= xpRequired;
            } else {
                xpCurrent = remaining;
                break;
            }
        }

        // not sure why this is floored but I'm leaving it in for now
        xpCurrent = (int) Math.floor(xpCurrent);

        /* the level as displayed by in game UI */
        level = Math.min(levelCap, uncappedLevel);

        /* the amount amount of xp needed to reach the next level (used for calculation progress to next level) */
        xpForNext = level < levelCap ? Math.ceil(xpFromIndex(level + 1)) : Double.POSITIVE_INFINITY;

        /* the fraction of the way toward the next level */
        progress = Math.max(0, Math.min(xpCurrent / xpForNext, 1));

        /* a floating point value representing the current level for example if you are half way to level 5 it would be 4.5 */
        levelWithProgress = level + progress;

        /* a floating point value representing the current level ignoring the in-game unlockable caps for example if you are half way to level 5 it would be 4.5 */
        unlockableLevelWithProgress = Math.min(uncappedLevel + progress, levelCap);

        excess = level == levelCap ? remaining : 0;
    }

    private int xpFromIndex(int index) {
        LinkedHashMap<Integer, Integer> xpTable = xpTable();
        LinkedList<Integer> levels = new LinkedList<>(xpTable.keySet());
        return xpTable.get(levels.get(index));
    }

    private LinkedHashMap<Integer, Integer> xpTable() {
        LinkedHashMap<Integer, Integer> xpTable = new LinkedHashMap<>();
        xpTable.put(0, 0);
        switch (slayerType) {
            case WOLF, ENDERMAN, BLAZE -> xpTable.put(1, 10);
            case SPIDER, ZOMBIE -> xpTable.put(1, 5);
        }
        switch (slayerType) {
            case WOLF, ENDERMAN, BLAZE, SPIDER -> xpTable.put(2, 20);
            case ZOMBIE -> xpTable.put(2, 10);
        }
        switch (slayerType) {
            case WOLF, ENDERMAN, BLAZE -> xpTable.put(3, 220);
            case SPIDER -> xpTable.put(3, 175);
            case ZOMBIE -> xpTable.put(3, 185);
        }
        switch (slayerType) {
            case WOLF, ENDERMAN, BLAZE -> xpTable.put(4, 1250);
            case SPIDER, ZOMBIE -> xpTable.put(4, 800);
        }
        switch (slayerType) {
            case WOLF, ENDERMAN, BLAZE -> xpTable.put(5, 3500);
            case SPIDER, ZOMBIE -> xpTable.put(5, 4000);
        }
        xpTable.put(6, 15000);
        xpTable.put(7, 80000);
        xpTable.put(8, 300000);
        xpTable.put(9, 600000);
        return xpTable;
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

    @Override
    public Weight getWeight() {
        return totalWeight;
    }
}
