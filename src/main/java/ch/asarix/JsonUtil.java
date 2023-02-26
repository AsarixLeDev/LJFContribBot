package ch.asarix;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.MissingNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.File;
import java.io.IOException;

public class JsonUtil {
    public static ObjectNode getObjectNode(File file) {
        JsonNode node = readFile(file);
        if (node instanceof MissingNode) {
            return JsonNodeFactory.instance.objectNode();
        } else if (node instanceof ObjectNode) {
            return (ObjectNode) node;
        } else {
            throw new RuntimeException("Could not read node properly");
        }
    }

    public static JsonNode readFile(File file) {
        try {
            return new ObjectMapper().readTree(file);
        } catch (IOException e) {
            e.printStackTrace();
            return JsonNodeFactory.instance.objectNode();
        }
    }

    public static void write(File file, JsonNode node) {
        try {
            writeTrow(file, node);
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
    }

    public static void writeTrow(File file, JsonNode node) throws RuntimeException {
        try {
            new ObjectMapper().writerWithDefaultPrettyPrinter().writeValue(file, node);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
