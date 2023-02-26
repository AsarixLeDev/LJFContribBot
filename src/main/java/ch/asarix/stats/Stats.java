package ch.asarix.stats;

import ch.asarix.stats.types.Misc;
import ch.asarix.stats.types.Skill;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Stats {
    public final double fetchedAtMillis;
    private final Map<Stat, CalculatedStat> stats = new HashMap<>();
    private final UUID uuid;
    public double totalWeight = 0;
    public double weightWithoutOverflow = 0;

    public Stats(UUID uuid) {
        this.uuid = uuid;
        this.fetchedAtMillis = System.currentTimeMillis();
    }

    public void addStat(Stat stat, long xp) {
        CalculatedStat calculatedStat = stat.calculate(xp);
        stats.put(stat, calculatedStat);
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

    public long getTotalXp(Stat stat) {
        if (stat == Misc.AVERAGE) {
            return (int) (getLevelWithProgress(Misc.AVERAGE) * 100L);
        }
        if (stat == Misc.WEIGHT) {
//            System.err.println("---------");
//            System.err.println(totalWeight);
//            System.err.println(getWeight(Misc.WEIGHT).total());
//            System.err.println((int) (totalWeight * 100));
//            System.err.println("---------");
            return (int) (totalWeight * 100);
        }
        if (!stats.containsKey(stat))
            return 0;
        return stats.get(stat).getTotalXp();
    }

    public int getLevel(Stat stat) {
        if (stat == Misc.AVERAGE) {
            long total = 0;
            double totalSkills = 0;
            for (Stat stat1 : stats.keySet()) {
                if (!(stat1 instanceof Skill s)) continue;
                if (s.isCosmetic()) continue;
                total += stats.get(stat1).getLevelWithProgress();
                totalSkills++;
            }
            return (int) Math.round(total / totalSkills);
        }
        if (stat == Misc.WEIGHT) {
            return (int) getWeight(Misc.WEIGHT).total();
        }
        if (!stats.containsKey(stat))
            return 0;
        return stats.get(stat).getLevel();
    }

    public double getLevelWithProgress(Stat stat) {
        if (stat == Misc.AVERAGE) {
            long total = 0;
            double totalSkills = 0;
            for (Stat stat1 : stats.keySet()) {
                if (!(stat1 instanceof Skill s)) continue;
                if (s.isCosmetic()) continue;
                total += stats.get(stat1).getLevelWithProgress();
                totalSkills++;
            }
            return total / totalSkills;
        }
        if (stat == Misc.WEIGHT) {
            return getWeight(Misc.WEIGHT).total();
        }
        if (!stats.containsKey(stat))
            return 0;
        return stats.get(stat).getLevelWithProgress();
    }

    public Weight getWeight(Stat stat) {
        if (stat == Misc.WEIGHT) {
            return new Weight(weightWithoutOverflow, totalWeight - weightWithoutOverflow);
        }
        if (!stats.containsKey(stat))
            return new Weight(0, 0);
        return stats.get(stat).getWeight();
    }

    public Map<Stat, CalculatedStat> getStats() {
        return stats;
    }
}
