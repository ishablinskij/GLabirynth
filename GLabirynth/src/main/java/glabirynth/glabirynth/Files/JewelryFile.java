package glabirynth.glabirynth.Files;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class JewelryFile {

    private static Map<String, Integer> jewelryMap = new HashMap<>();

    private static File jewerlyFile = Path.of("plugins/labyrinth/jewelry.yaml").toFile();

    private static String[] startArrayOfJewelry = {"GOLD_BLOCK", "GOLD_INGOT", "DIAMOND_BLOCK", "DIAMOND", "GOLD_NUGGET"};

    private static HashMap<String, Boolean> wasCreated = new HashMap<>();



    private static Yaml yaml = new Yaml();

    public static void createJewelryFile() {
        try {
            if (!jewerlyFile.exists()) {
                wasCreated.put("wasCreated", false);
                fillJewelryMap();
                Files.createFile(jewerlyFile.toPath());
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void fillJewelryMap() {
        jewelryMap.put(startArrayOfJewelry[0], 0);
        jewelryMap.put(startArrayOfJewelry[1], 0);
        jewelryMap.put(startArrayOfJewelry[2], 0);
        jewelryMap.put(startArrayOfJewelry[3], 0);
        jewelryMap.put(startArrayOfJewelry[4], 0);
    }

    public static void fillJewelryFile() {
        if (wasCreated.get("wasCreated") != null && !wasCreated.get("wasCreated")) {
            try (Writer outputStream = new FileWriter(jewerlyFile)) {
                DumperOptions options = new DumperOptions();
                options.setIndent(2);
                options.setPrettyFlow(true);
                options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
                Yaml yamlForWriting = new Yaml(options);
                yamlForWriting.dump(jewelryMap, outputStream);
                wasCreated.remove("wasCreated");
                wasCreated.put("wasCreated", true);
                yamlForWriting.dump(wasCreated, outputStream);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static String[] getArrayOfJewelry() {
        readJewelryFile();
        return jewelryMap.keySet().toArray(new String[0]);
    }

    public static Integer getValueOfJewel(String jewel) {
        readJewelryFile();
        return jewelryMap.get(jewel);
    }

    private static void readJewelryFile() {
        try (Reader reader = new FileReader(jewerlyFile);) {
            jewelryMap = yaml.load(reader);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
