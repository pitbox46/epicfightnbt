package github.pitbox46.epicfightnbt;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import github.pitbox46.epicfightnbt.JSONSchema.NBTCategorySchema;
import github.pitbox46.epicfightnbt.JSONSchema.NBTConditionSchema;
import github.pitbox46.epicfightnbt.JSONSchema.WeaponSchema;
import github.pitbox46.epicfightnbt.network.SSyncConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.item.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
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
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
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
    public static NBTCategorySchema JSON_MAP;

    public static void init(Path folder) {
        jsonFile = new File(FileUtils.getOrCreateDirectory(folder, "serverconfig").toFile(), "epicfightnbt.json");
        try {
            if (jsonFile.createNewFile()) {
                Path defaultConfigPath = FMLPaths.GAMEDIR.get().resolve(FMLConfig.defaultConfigPath()).resolve("epicfightnbt.json");
                InputStreamReader defaults = new InputStreamReader(Files.exists(defaultConfigPath)? Files.newInputStream(defaultConfigPath) :
                        Objects.requireNonNull(Thread.currentThread().getContextClassLoader().getResourceAsStream("assets/epicfightnbt/epicfightnbt.json")));
                FileOutputStream writer = new FileOutputStream(jsonFile, false);
                int read;
                while ((read = defaults.read()) != -1) {
                    writer.write(read);
                }
                writer.close();
                defaults.close();
            }
        } catch (IOException error) {
            LOGGER.warn(error.getMessage());
        }

        readConfig(jsonFile);
    }

    public static SSyncConfig configFileToSSyncConfig() {
        try {
            return new SSyncConfig(new String(Files.readAllBytes(jsonFile.toPath())));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void readConfig(String config) {
        JSON_MAP = new Gson().fromJson(config, NBTCategorySchema.class);
    }

    public static void readConfig(File path) {
        try (Reader reader = new FileReader(path)) {
            JSON_MAP = new Gson().fromJson(reader, NBTCategorySchema.class);
        } catch (IOException e) {
            e.printStackTrace();
            JSON_MAP = new NBTCategorySchema();
        }
    }

    public static CapabilityItem findWeaponByNBT(ItemStack stack) {
        if(stack.hasTag()) {
            CompoundNBT tag = stack.getTag();
            for (String key : tag.keySet()) {
                for (NBTConditionSchema condition : JSON_MAP.weapons) {
                    if (condition.type.equals(key)) {
                        String value = tag.getString(key);
                        for (WeaponSchema weapon : condition.subtypes) {
                            if (weapon.nbt_value.equals(value)) {
                                CapabilityItem toReturn = DICTIONARY.get(weapon.weapon_type).apply(stack);
                                toReturn.setConfigFileAttribute(
                                        weapon.armor_ignorance, weapon.impact, weapon.hit_at_once,
                                        weapon.armor_ignorance, weapon.impact, weapon.hit_at_once);
                                return toReturn;
                            }
                        }
                    }
                }
            }
        }
        return CapabilityItem.EMPTY;
    }
}
