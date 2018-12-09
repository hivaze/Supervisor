package me.litefine.supervisor.utils;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class YamlUtils {

    private static final Supplier<Yaml> YAML_BUILDER = () -> {
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        return new Yaml(options);
    };

    public static HashMap loadFrom(File file) throws FileNotFoundException {
        return YAML_BUILDER.get().loadAs(new FileReader(file), HashMap.class);
    }

    public static void dump(HashMap map, File file) throws IOException {
        try (FileWriter writer = new FileWriter(file)) {
            YAML_BUILDER.get().dump(map, writer);
        }
    }

    public static boolean getBoolean(String path, Map source) {
        String retrieved = getString(path, source);
        return Boolean.valueOf(retrieved);
    }

    public static String getString(String path, Map source) {
        Object retrieved = getObject(path, source);
        return String.valueOf(retrieved);
    }

    public static int getInteger(String path, Map source) {
        String retrieved = getString(path, source);
        return Integer.parseInt(retrieved);
    }

    public static List<String> getList(String path, Map source) {
        Object retrieved = getObject(path, source);
        return (List<String>) retrieved;
    }

    public static Object getObject(String path, Map source) {
        String[] paths = path.split("[.]");
        for (int number = 0; number < paths.length; number++) {
            path = paths[number];
            if (number != paths.length-1 && source.get(path) instanceof Map)
                source = (Map) source.get(path);
        }
        return source.get(path);
    }

}