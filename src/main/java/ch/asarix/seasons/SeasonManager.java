package ch.asarix.seasons;

import ch.asarix.DataManager;
import ch.asarix.JsonUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.LongNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.LinkedList;
import java.util.List;

public class SeasonManager extends DataManager {
    private static final SeasonManager instance = new SeasonManager();
    public final List<Season> seasons = new LinkedList<>();

    public SeasonManager() {
        super("seasons.json");
        seasons.add(new Season("Universal", 0, System.currentTimeMillis()));
    }

    public static SeasonManager get() {
        return instance;
    }

    public boolean exists(String name) {
        return getSeason(name) != null;
    }

    public Season getSeason(String name) {
        for (Season season : seasons) {
            if (season.name().equalsIgnoreCase(name))
                return season;
        }
        return null;
    }

    public Season createSeason(String name, long fromMillis, long toMillis) {
        String finalName = name == null ? "Saison n°" + seasons.size() : name;
        Season season = new Season(finalName, fromMillis, toMillis);
        seasons.add(season);
        ObjectNode dates = JsonNodeFactory.instance.objectNode();
        dates.set("fromMillis", new LongNode(fromMillis));
        dates.set("toMillis", new LongNode(toMillis));
        ObjectNode toWrite = JsonUtil.getObjectNode(file);
        toWrite.set(name, dates);
        JsonUtil.write(file, toWrite);
        return season;
    }

    @Override
    public void store(String key, JsonNode node) {
        long fromMillis = node.get("fromMillis").asLong();
        long toMillis = node.get("toMillis").asLong();
        createSeason(key, fromMillis, toMillis);
    }

    @Override
    public void clearData() {
        seasons.clear();
    }
}
