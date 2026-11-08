package ceremonious.crates;

import cn.nukkit.Player;
import cn.nukkit.level.Location;
import cn.nukkit.level.Sound;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.ConfigSection;

import java.io.File;

public final class CrateFailureEffect {

    private static Config config;

    private CrateFailureEffect() {
    }

    public static void load(Main plugin) {
        ConfigSection defaults = new ConfigSection();

        defaults.set("enabled", true);
        defaults.set("knockback.enabled", true);
        defaults.set("knockback.strength", 0.6);

        defaults.set("sound.enabled", true);
        defaults.set("sound.name", "BLOCK_FALSE_PERMISSIONS");
        defaults.set("sound.volume", 1.0);
        defaults.set("sound.pitch", 1.0);

        File file = new File(plugin.getDataFolder(), "failure-effects.yml");

        config = new Config(file, Config.YAML, defaults);
        config.save();
    }

    public static void play(Player player, Location crateLocation) {
        if (config == null || !config.getBoolean("enabled", true)) {
            return;
        }

        if (config.getBoolean("knockback.enabled", true)) {
            double strength = Math.max(
                    0.0,
                    config.getDouble("knockback.strength", 0.6)
            );

            double x = player.getX() - (crateLocation.getX() + 0.5);
            double z = player.getZ() - (crateLocation.getZ() + 0.5);

            player.knockBack(null, 0.0, x, z, strength);
        }

        if (config.getBoolean("sound.enabled", true)) {
            String soundName = config.getString(
                    "sound.name",
                    "BLOCK_FALSE_PERMISSIONS"
            );

            try {
                Sound sound = Sound.valueOf(soundName.toUpperCase());

                float volume = (float) config.getDouble("sound.volume", 1.0);
                float pitch = (float) config.getDouble("sound.pitch", 1.0);

                player.getLevel().addSound(player, sound, volume, pitch);

            } catch (IllegalArgumentException exception) {
                // Sonido inválido: se omite sin afectar la apertura de crates.
            }
        }
    }
}