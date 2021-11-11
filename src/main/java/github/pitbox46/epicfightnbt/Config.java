package github.pitbox46.epicfightnbt;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.item.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.JSONUtils;
import net.minecraftforge.fml.loading.FMLConfig;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.fml.loading.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import yesman.epicfight.capabilities.item.CapabilityItem;
import yesman.epicfight.config.CapabilityConfig;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class Config {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static final Map<String, Function<ItemStack, CapabilityItem>> DICTIONARY = new HashMap<>();
    static {
        for(CapabilityConfig.WeaponType type : CapabilityConfig.WeaponType.values()) {
            DICTIONARY.put(type.name().toLowerCase(), (stack) -> type.get(stack.getItem()));
        }
    }

    public static File jsonFile;
    public static final Map<String, Map<String, String>> JSON_MAP = new HashMap<>();

    public static void init(Path folder) {
        File file = new File(FileUtils.getOrCreateDirectory(folder, "serverconfig").toFile(), "epicfightnbt.json");
        try {
            if (file.createNewFile()) {
                Path defaultConfigPath = FMLPaths.GAMEDIR.get().resolve(FMLConfig.defaultConfigPath()).resolve("epicfightnbt.json");
                if (Files.exists(defaultConfigPath)) {
                    Files.copy(defaultConfigPath, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
                } else {
                    FileWriter writer = new FileWriter(file);

                    StringBuilder values = new StringBuilder();
                    for (String value : DICTIONARY.keySet()) {
                        values.append(value).append(", ");
                    }
                    String json = "{\n" +
                            "  \"__comment\": \"Values: " + values + "\",\n" +
                            "  \"double/head_left\": {\n" +
                            "    \"double/basic_hammer_left\": \"axe\",\n" +
                            "    \"double/basic_axe_left\": \"axe\",\n" +
                            "    \"double/basic_pickaxe_left\": \"pickaxe\",\n" +
                            "    \"double/hoe_left\": \"hoe\",\n" +
                            "    \"double/claw_left\": \"axe\",\n" +
                            "    \"double/adze_left\": \"axe\",\n" +
                            "    \"double/sickle_left\": \"axe\"\n" +
                            "  },\n" +
                            "  \"sword/blade\": {\n" +
                            "    \"sword/basic_blade\": \"sword\",\n" +
                            "    \"sword/heavy_blade\": \"greatsword\",\n" +
                            "    \"sword/machete\": \"tachi\",\n" +
                            "    \"sword/short_blade\": \"dagger\"\n" +
                            "  },\n" +
                            "  \"single/head\": {\n" +
                            "    \"single/spearhead\": \"spear\",\n" +
                            "    \"single/basic_shovel\": \"shovel\"\n" +
                            "  },\n" +
                            "  \"crossbow/stock\": {\n" +
                            "    \"crossbow/basic_stock\": \"crossbow\"\n" +
                            "  },\n" +
                            "  \"bow/string\": {\n" +
                            "    \"bow/basic_string\": \"bow\"\n" +
                            "  }\n" +
                            "}";
                    writer.write(json);
                    writer.close();
                }
            }
        } catch (IOException e) {
            LOGGER.warn(e.getMessage());
        }
        jsonFile = file;
        readJson(readFile());
    }

    public static JsonObject readFile() {
        try (Reader reader = new FileReader(jsonFile)) {
            return JSONUtils.fromJson(GSON, reader, JsonObject.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void readJson(JsonObject jsonObject) {
        if(jsonObject != null) {
            JSON_MAP.clear();
            for(Map.Entry<String, JsonElement> itemType: jsonObject.entrySet()) {
                if(itemType.getKey().equals("__comment")) continue;
                JSON_MAP.put(itemType.getKey(), itemType.getValue().getAsJsonObject().entrySet().stream().collect(HashMap::new, (map, entry) -> map.put(entry.getKey(), entry.getValue().getAsString()), HashMap::putAll));
            }
        }
    }

    public static CapabilityItem findWeaponByNBT(ItemStack stack) {
        if(stack.hasTag()) {
            CompoundNBT tag = stack.getTag();
            for (String key : tag.keySet()) {
                if (Config.JSON_MAP.containsKey(key)) {
                    String value = tag.getString(key);
                    if (Config.JSON_MAP.get(key).containsKey(value)) {
                        return Config.DICTIONARY.get(Config.JSON_MAP.get(key).get(value)).apply(stack);
                    }
                }
            }
        }
        return CapabilityItem.EMPTY;
    }
}
