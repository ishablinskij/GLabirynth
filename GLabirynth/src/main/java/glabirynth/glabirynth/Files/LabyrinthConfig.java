package glabirynth.glabirynth.Files;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class LabyrinthConfig {

    private static final String locationOfStartSpawn = "locationOfStartSpawn";
    private static final String locationOfPlaceForLooser = "locationOfPlaceForLooser";
    private static final String locationOfStartSpawnInLabyrinth = "locationOfStartSpawnInLabyrinth";
    private static final String locationOfExitFromLabyrinth = "locationOfExitFromLabyrinth";


    private static HashMap<String, Double> locationOfStartSpawnHashMap = new HashMap<>();
    private static HashMap<String, Double> locationOfPlaceForLooserHashMap = new HashMap<>();
    private static HashMap<String, Double> locationOfStartSpawnInLabyrinthHashMap = new HashMap<>();
    private static HashMap<String, Double> locationOfExitFromLabyrinthHashMap = new HashMap<>();

    private static HashMap<String, Double> clockSpawning = new HashMap<>();

    private static HashMap<String, Boolean> wasCreated = new HashMap<>();

    private static Map<String, HashMap<String, Double>> locations = new HashMap<>();


    private static Yaml yaml = new Yaml();

    private static File config = Path.of("plugins/labyrinth/labyrinthConfig.yaml").toFile();

    public static void createConfigFile() {
        try {
            if (!config.exists()) {
                fillHashMaps();
                Files.createFile(config.toPath());
                wasCreated.put("wasCreated", false);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void fillHashMaps() {
        locationOfStartSpawnHashMap.put("x", 0.0);
        locationOfStartSpawnHashMap.put("y", 0.0);
        locationOfStartSpawnHashMap.put("z", 0.0);

        locationOfPlaceForLooserHashMap.put("x", 0.0);
        locationOfPlaceForLooserHashMap.put("y", 0.0);
        locationOfPlaceForLooserHashMap.put("z", 0.0);

        locationOfStartSpawnInLabyrinthHashMap.put("x", 0.0);
        locationOfStartSpawnInLabyrinthHashMap.put("y", 0.0);
        locationOfStartSpawnInLabyrinthHashMap.put("z", 0.0);

        locationOfExitFromLabyrinthHashMap.put("x", 0.0);
        locationOfExitFromLabyrinthHashMap.put("y", 0.0);
        locationOfExitFromLabyrinthHashMap.put("z", 0.0);

        clockSpawning.put("x", 0.0);
        clockSpawning.put("y", 0.0);
        clockSpawning.put("z", 0.0);

        locations.put(locationOfStartSpawn, locationOfStartSpawnHashMap);
        locations.put(locationOfPlaceForLooser, locationOfPlaceForLooserHashMap);
        locations.put(locationOfStartSpawnInLabyrinth, locationOfStartSpawnInLabyrinthHashMap);
        locations.put(locationOfExitFromLabyrinth, locationOfExitFromLabyrinthHashMap);
        locations.put("clockSpawning", clockSpawning);

    }

    public static void fillConfig() {
        if (wasCreated.get("wasCreated") != null && !wasCreated.get("wasCreated")) {
            try (Writer outputStream = new FileWriter(config)) {
                DumperOptions options = new DumperOptions();
                options.setIndent(2);
                options.setPrettyFlow(true);
                options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
                Yaml yamlForWriting = new Yaml(options);
                yamlForWriting.dump(locations, outputStream);
                wasCreated.remove("wasCreated");
                wasCreated.put("wasCreated", true);
                yamlForWriting.dump(wasCreated, outputStream);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }


    private static void readConfig() {
        try (Reader reader = new FileReader(config);) {
            locations = yaml.load(reader);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Object getProperty(String property) {
        readConfig();
        return locations.get(property);
    }

    public static Location getLocation(String nameOfLocation) {
        readConfig();
        Map<String, Double> locationsMap = locations.get(nameOfLocation);
        double x = locationsMap.get("x");
        double y = locationsMap.get("y");
        double z = locationsMap.get("z");
        String nameOfWorld = "maze";
        return new Location(Bukkit.getWorld(nameOfWorld), x, y, z);
    }
}
