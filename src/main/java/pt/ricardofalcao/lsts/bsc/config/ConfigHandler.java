package pt.ricardofalcao.lsts.bsc.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import pt.ricardofalcao.lsts.bsc.config.impl.MainConfig;

@RequiredArgsConstructor
public class ConfigHandler {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    /*

     */

    private final File configFolder;

    /*

     */

    public MainConfig main;

    /*

     */

    public void load() throws ConfigLoadException {
        this.main = load(new File(configFolder, "main.json"), MainConfig.class);
    }

    private <T extends Config> T load(File file, Class<T> configClass) throws ConfigLoadException {
        try {
            if (!file.exists()) {
                file.createNewFile();
            }

            try (JsonReader reader = new JsonReader(new FileReader(file))) {
                T value = GSON.fromJson(reader, configClass);

                if (value == null) {
                    try {
                        return configClass.getDeclaredConstructor().newInstance();
                    } catch (Exception ex) {
                        throw new ConfigLoadException(ex);
                    }
                }

                return value;
            }

        } catch(IOException ex) {
            throw new ConfigLoadException(ex);
        }
    }

    /*

     */

    public void save() throws ConfigSaveException {
        save(new File(configFolder, "main.json"), this.main);
    }

    private <T extends Config> void save(File file, T config) throws ConfigSaveException {
        try (FileWriter writer = new FileWriter(file)) {
            GSON.toJson(config, writer);
        } catch(IOException ex) {
            throw new ConfigSaveException(ex);
        }
    }

}
