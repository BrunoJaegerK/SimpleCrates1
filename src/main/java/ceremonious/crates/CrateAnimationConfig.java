package ceremonious.crates;

import cn.nukkit.utils.Config;

import java.io.File;

public final class CrateAnimationConfig {

    private static Config config;

    private CrateAnimationConfig() {
    }

    public static void load(Main plugin) {
        File file = new File(plugin.getDataFolder(), "animations.yml");

        if (!file.exists()) {
            plugin.saveResource("animations.yml", false);
        }

        config = new Config(file, Config.YAML);
    }

    private static Config getConfig() {
        if (config == null) {
            throw new IllegalStateException(
                    "CrateAnimationConfig has not been loaded."
            );
        }

        return config;
    }

    public static boolean isLoaded() {
        return config != null;
    }

    // Animación clásica

    public static boolean isClassicEnabled() {
        return getConfig().getBoolean(
                "animations.classic.enabled", true
        );
    }

    public static int getClassicDuration() {
        return Math.max(1, getConfig().getInt(
                "animations.classic.duration", 60
        ));
    }

    public static boolean areClassicParticlesEnabled() {
        return getConfig().getBoolean(
                "animations.classic.particles.enabled", true
        );
    }

    public static String getClassicParticleType() {
        return getConfig().getString(
                "animations.classic.particles.type", "flame"
        );
    }

    public static double getClassicParticleRadius() {
        return getConfig().getDouble(
                "animations.classic.particles.radius", 0.8
        );
    }

    public static double getClassicParticleHeight() {
        return getConfig().getDouble(
                "animations.classic.particles.height", 1.0
        );
    }

    public static boolean areClassicSoundsEnabled() {
        return getConfig().getBoolean(
                "animations.classic.sounds.enabled", true
        );
    }

    public static String getClassicOpeningSound() {
        return getConfig().getString(
                "animations.classic.sounds.opening", "RANDOM_CHESTOPEN"
        );
    }

    public static float getClassicSoundVolume() {
        return (float) getConfig().getDouble(
                "animations.classic.sounds.volume", 1.0
        );
    }

    public static float getClassicPitchStart() {
        return (float) getConfig().getDouble(
                "animations.classic.sounds.pitch-start", 0.8
        );
    }

    public static float getClassicPitchEnd() {
        return (float) getConfig().getDouble(
                "animations.classic.sounds.pitch-end", 1.8
        );
    }

    // Selección del modo de animación

    public static String getAnimationMode() {
        String mode = getConfig().getString(
                "animations.mode", "classic"
        );

        if (!mode.equalsIgnoreCase("classic")
                && !mode.equalsIgnoreCase("roulette")) {
            return "classic";
        }

        return mode.toLowerCase();
    }

    // Animación de ruleta

    public static boolean isRouletteEnabled() {
        return getConfig().getBoolean(
                "animations.roulette.enabled", true
        );
    }

    public static int getRouletteDuration() {
        return Math.max(1, getConfig().getInt(
                "animations.roulette.duration", 100
        ));
    }

    public static boolean areRouletteSoundsEnabled() {
        return getConfig().getBoolean(
                "animations.roulette.sounds.enabled", true
        );
    }

    public static String getRouletteSpinSound() {
        return getConfig().getString(
                "animations.roulette.sounds.spin", "NOTE"
        );
    }

    public static String getRouletteFinishSound() {
        return getConfig().getString(
                "animations.roulette.sounds.finish", "LEVEL_UP"
        );
    }

    public static float getRouletteSoundVolume() {
        return (float) getConfig().getDouble(
                "animations.roulette.sounds.volume", 1.0
        );
    }
}


