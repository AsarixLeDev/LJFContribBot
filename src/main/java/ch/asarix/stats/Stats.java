package ch.asarix.stats;

import ch.asarix.stats.types.DungeonType;
import ch.asarix.stats.types.Misc;
import ch.asarix.stats.types.Skill;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Stats {
    public final double fetchedAtMillis;
    private final Map<StatType, CalculatedStat> stats = new HashMap<>();
    private final UUID uuid;
    public double totalWeight = 0;
    public double weightWithoutOverflow = 0;

    public Stats(UUID uuid) {
        this.uuid = uuid;
        this.fetchedAtMillis = System.currentTimeMillis();
    }

    public void addStat(StatType statType, long xp) {
        CalculatedStat calculatedStat = statType.calculate(xp);
        stats.put(statType, calculatedStat);
        Weight weight = calculatedStat.getWeight();
        if (Double.isFinite(weight.total())) {
            totalWeight += weight.total();
        }
        if (Double.isFinite(weight.base())) {
            weightWithoutOverflow += weight.base();
        }
    }

    public UUID getUuid() {
        return uuid;
    }

    public long getTotalXp(StatType statType) {
        if (statType == Misc.AVERAGE) {
            return (int) (getLevelWithProgress(Misc.AVERAGE) * 100L);
        }
        if (statType == Misc.WEIGHT) {
//            System.err.println("---------");
//            System.err.println(totalWeight);
//            System.err.println(getWeight(Misc.WEIGHT).total());
//            System.err.println((int) (totalWeight * 100));
//            System.err.println("---------");
            return (int) (totalWeight * 100);
        }
        if (statType == Misc.DJ_CLASS) {
            return getTotalXp(getBestClass());
        }
        if (!stats.containsKey(statType))
            return 0;
        return stats.get(statType).getTotalXp();
    }

    public int getLevel(StatType statType) {
        if (statType == Misc.AVERAGE) {
            long total = 0;
            double totalSkills = 0;
            for (StatType statType1 : stats.keySet()) {
                if (!(statType1 instanceof Skill s)) continue;
                if (s.isCosmetic()) continue;
                total += stats.get(statType1).getLevelWithProgress();
                totalSkills++;
            }
            return (int) Math.round(total / totalSkills);
        }
        if (statType == Misc.WEIGHT) {
            return (int) getWeight(Misc.WEIGHT).total();
        }
        if (statType == Misc.DJ_CLASS) {
            return getLevel(getBestClass());
        }
        if (!stats.containsKey(statType))
            return 0;
        return stats.get(statType).getLevel();
    }

    public double getLevelWithProgress(StatType statType) {
        if (statType == Misc.AVERAGE) {
            long total = 0;
            double totalSkills = 0;
            for (StatType statType1 : stats.keySet()) {
                if (!(statType1 instanceof Skill s)) continue;
                if (s.isCosmetic()) continue;
                total += stats.get(statType1).getLevelWithProgress();
                totalSkills++;
            }
            return total / totalSkills;
        }
        if (statType == Misc.WEIGHT) {
            return getWeight(Misc.WEIGHT).total();
        }
        if (statType == Misc.DJ_CLASS) {
            return getLevelWithProgress(getBestClass());
        }
        if (!stats.containsKey(statType))
            return 0;
        return stats.get(statType).getLevelWithProgress();
    }

    public Weight getWeight(StatType statType) {
        if (statType == Misc.WEIGHT) {
            return new Weight(weightWithoutOverflow, totalWeight - weightWithoutOverflow);
        }
        if (statType == Misc.DJ_CLASS) {
            return getWeight(getBestClass());
        }
        if (!stats.containsKey(statType))
            return new Weight(0, 0);
        return stats.get(statType).getWeight();
    }

    public Map<StatType, CalculatedStat> getStats() {
        return stats;
    }

    public StatType getBestClass() {
        StatType bestClass = null;
        long bestXp = 0;
        for (StatType statType : stats.keySet()) {
            if (!(statType instanceof DungeonType)) continue;
            if (statType == DungeonType.CATACOMBS) continue;
            long classXp = getTotalXp(statType);
            if (classXp > bestXp) {
                bestClass = statType;
                bestXp = classXp;
            }
        }
        return bestClass;
    }
}
