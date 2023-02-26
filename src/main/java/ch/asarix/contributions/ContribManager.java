package ch.asarix.contributions;

import ch.asarix.*;
import ch.asarix.seasons.Season;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.*;
import net.dv8tion.jda.api.entities.User;

import java.util.*;
import java.util.stream.Collectors;

public class ContribManager extends DataManager {

    private static final ContribManager instance = new ContribManager();
    public Map<Long, List<Contribution>> map = new HashMap<>();

    public ContribManager() {
        super("data.json");
    }

    public static ContribManager get() {
        return instance;
    }

    @Override
    public void store(String key, JsonNode node) {
        User user = UserManager.getUser(key);
        ArrayNode userNode = (ArrayNode) node;

        for (int i = 0; i < userNode.size(); i++) {
            JsonNode next = userNode.get(i);
            if (next == null || next instanceof MissingNode) continue;
            String name = next.get("name").asText();
            int amount = next.get("amount").asInt();
            Date date = new Date(next.get("dateMillis").asLong());
            String commentary = next.get("commentary").asText();
            long value = next.get("value").asLong();

            if (date.getTime() > System.currentTimeMillis())
                date = new Date();
            createContribution(user, name, amount, date, commentary, value);
        }
    }

    public void save(Contribution contribution) {
        User user = contribution.user();
        List<Contribution> contribs = map.get(user.getIdLong());
        if (contribs == null) {
            contribs = new ArrayList<>();
        }
        contribs.add(contribution);
        map.put(user.getIdLong(), contribs);
        saveAll(user, contribs);
    }

    public void saveAll(User user, List<Contribution> contribs) {

        ObjectNode node = JsonUtil.getObjectNode(file);
        ArrayNode userNode = new ArrayNode(JsonNodeFactory.instance);

        for (Contribution contribution : contribs) {

            ObjectNode contribNode = new ObjectNode(JsonNodeFactory.instance);
            contribNode.set("name", new TextNode(contribution.contribName()));
            contribNode.set("amount", new IntNode(contribution.amount()));
            contribNode.set("dateMillis", new LongNode(contribution.date().getTime()));
            contribNode.set("commentary", new TextNode(contribution.commentary()));
            contribNode.set("value", new LongNode(contribution.value()));
            userNode.add(contribNode);
        }
        node.set(user.getId(), userNode);

        JsonUtil.write(file, node);

    }

    public void replaceAll(User user, List<Contribution> contribs) {
        map.put(user.getIdLong(), contribs);
        saveAll(user, contribs);
    }

    public List<Contribution> getContribs(User user) {
        if (!map.containsKey(user.getIdLong())) return new ArrayList<>();
        return map.get(user.getIdLong());
    }

    public List<User> getContribers() {
        return new ArrayList<>(map.keySet()).stream().map(UserManager::getUser).collect(Collectors.toList());
    }

    public long getTotalContribValue(User user) {
        long total = 0;
        for (Contribution contribution : getContribs(user)) {
            total += contribution.value();
        }
        return total;
    }

    public long getTotalContribValue(User user, Season season) {
        long total = 0;
        for (Contribution contribution : getContribs(user)) {
            if (!season.contains(contribution)) continue;
            total += contribution.value();
        }
        return total;
    }

    public void remove(Contribution toRemove) {
        if (toRemove == null) return;
        User user = toRemove.user();
        List<Contribution> contribs = getContribs(user);
        for (Contribution contribution : contribs) {
            if (contribution.equals(toRemove)) {
                System.out.println("Found contrib to edit !");
                contribs.remove(contribution);
                replaceAll(user, contribs);
                return;
            }
        }
    }

    public void edit(User user, Contribution before, Contribution after) {
        List<Contribution> contribs = getContribs(user);
        for (Contribution contribution : contribs) {
            if (contribution.equals(before)) {
                System.out.println("Found contrib to edit !");
                contribs.remove(contribution);
                contribs.add(after);
                replaceAll(user, contribs);
                return;
            }
        }
    }

    public Contribution createContribution(User user, String contribName, int amount, Date date, String commentary, long value) {
        List<Contribution> contribs = getContribs(user);
        String[] newId = {""};
        do {
            newId[0] = new PasswordGenerator.PasswordGeneratorBuilder().useLower(true).useUpper(true).build().generate(8);
        }
        while (contribs.stream().anyMatch(contribution -> contribution.id().equals(newId[0])));
        Contribution contribution = new Contribution(user, contribName, amount, date, commentary, value, newId[0]);
        save(contribution);
        return contribution;
    }

    public Contribution getContrib(User user, String id) {
        return getContribs(user).stream().filter(contribution -> contribution.id().equals(id)).findFirst().orElse(null);
    }

    public void save(User user) {
        saveAll(user, map.get(user.getIdLong()));
    }

    public List<User> top() {
        return top(new Season("Universal", 0, System.currentTimeMillis()));
    }

    public List<User> top(Season season) {
        List<User> users = new ArrayList<>(getContribers());
        return users.stream()
                .filter(user -> getTotalContribValue(user, season) > 0)
                .sorted(Comparator.comparingLong(user -> getTotalContribValue((User) user, season)).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public void clearData() {
        map.clear();
    }
}
