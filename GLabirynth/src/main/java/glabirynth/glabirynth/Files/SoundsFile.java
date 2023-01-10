package glabirynth.glabirynth.Files;

import org.bukkit.Sound;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;

public class SoundsFile {

    private static HashMap<String, String> sound = new HashMap<>();

    private static File soundFile = Path.of("plugins/labyrinth/sounds.yaml").toFile();
    private static HashMap<String, Boolean> wasCreated = new HashMap<>();

    private static Yaml yaml = new Yaml();

    public static void createSoundsFile() {
        try {
            if (!soundFile.exists()) {
                wasCreated.put("wasCreated", false);
                fillHashMaps();
                Files.createFile(soundFile.toPath());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void fillHashMaps() {
        sound.put("soundOfAllGame", Sound.MUSIC_DISC_PIGSTEP.name());
    }

    public static void fillSoundsFile() {
        if (wasCreated.get("wasCreated") != null && !wasCreated.get("wasCreated")) {
            try (Writer writer = new FileWriter(soundFile)) {
                DumperOptions options = new DumperOptions();
                options.setIndent(2);
                options.setPrettyFlow(true);
                options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
                Yaml yamlForWriting = new Yaml(options);
                yamlForWriting.dump(sound, writer);
                wasCreated.remove("wasCreated");
                wasCreated.put("wasCreated", true);
                yamlForWriting.dump(wasCreated);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void readSoundFile() {
        try (Reader reader = new FileReader(soundFile)) {
            sound = yaml.load(reader);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public static String getSoundProperty(String property) {
        readSoundFile();
        return  sound.get(property);
    }

}
