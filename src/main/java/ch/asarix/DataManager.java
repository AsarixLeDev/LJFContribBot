package ch.asarix;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public abstract class DataManager {
    public boolean finishedLoading = false;
    public double loadPercentage = 0;
    public File file;

    public DataManager(String fileName) {
        this.file = Main.getFile(fileName);
    }

    public void init() {
        loadPercentage = 0;
        finishedLoading = false;
        try {
            JsonNode node1 = new ObjectMapper().readTree(file);
            Map<String, JsonNode> fields = new HashMap<>();

            Iterator<Map.Entry<String, JsonNode>> it = node1.fields();
            while (it.hasNext()) {
                Map.Entry<String, JsonNode> entry = it.next();
                fields.put(entry.getKey(), entry.getValue());
            }
            int totalSize = fields.size();
            double fetched = 0;
            for (String key : fields.keySet()) {
                try {
                    store(key, fields.get(key));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                fetched += 1;
                loadPercentage = fetched / totalSize;
            }
            loadPercentage = 1;
            finishedLoading = true;
            System.out.println("Finished loading " + file.getName() + " !");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public abstract void store(String key, JsonNode node);

    public abstract void clearData();
}
