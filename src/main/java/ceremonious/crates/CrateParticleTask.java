package ceremonious.crates;

import cn.nukkit.level.Level;
import cn.nukkit.level.Location;
import cn.nukkit.level.particle.GenericParticle;
import cn.nukkit.level.particle.Particle;
import cn.nukkit.math.Vector3;
import cn.nukkit.scheduler.PluginTask;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.ConfigSection;

import java.io.File;

public class CrateParticleTask extends PluginTask<Main> {

    private final Config config;
    private final boolean enabled;
    private final String defaultParticle;
    private final double defaultRadius;
    private final int defaultPoints;

    private CrateParticleTask(Main plugin) {
        super(plugin);

        ConfigSection defaults = new ConfigSection();
        defaults.set("enabled", true);
        defaults.set("particle", "FLAME");
        defaults.set("interval-ticks", 10);
        defaults.set("radius", 0.7);
        defaults.set("points", 8);
        defaults.set("crates", new ConfigSection());

        File file = new File(plugin.getDataFolder(), "effects.yml");
        this.config = new Config(file, Config.YAML, defaults);
        this.config.save();

        this.enabled = config.getBoolean("enabled", true);
        this.defaultParticle = normalizeParticle(config.getString("particle", "FLAME"));
        this.defaultRadius = Math.max(0.1, config.getDouble("radius", 0.7));
        this.defaultPoints = Math.max(1, config.getInt("points", 8));
    }

    public static void start(Main plugin) {
        CrateParticleTask task = new CrateParticleTask(plugin);

        if (!task.enabled) {
            return;
        }

        int interval = Math.max(1, task.config.getInt("interval-ticks", 10));
        plugin.getServer().getScheduler().scheduleRepeatingTask(task, interval);
    }

    @Override
    public void onRun(int currentTick) {
        for (Crate crate : Main.crates) {
            if (crate == null || !crate.hasLocation()) {
                continue;
            }

            String cratePath = "crates." + crate.getName();

            if (!config.getBoolean(cratePath + ".enabled", true)) {
                continue;
            }

            Location location = crate.getLocation();

            if (location == null || location.getLevel() == null) {
                continue;
            }

            String particleName = normalizeParticle(
                    config.getString(cratePath + ".particle", defaultParticle)
            );

            double radius = Math.max(
                    0.1,
                    config.getDouble(cratePath + ".radius", defaultRadius)
            );

            int points = Math.max(
                    1,
                    config.getInt(cratePath + ".points", defaultPoints)
            );

            Level level = location.getLevel();

            for (int i = 0; i < points; i++) {
                double angle = (2.0 * Math.PI * i) / points;

                double x = location.getX() + 0.5 + Math.cos(angle) * radius;
                double z = location.getZ() + 0.5 + Math.sin(angle) * radius;
                double y = location.getY() + 1.0;

                Vector3 position = new Vector3(x, y, z);
                Particle particle = createParticle(position, particleName);

                if (particle != null) {
                    level.addParticle(particle);
                }
            }
        }
    }

    private Particle createParticle(Vector3 position, String particleName) {
        Integer particleId = Particle.getParticleIdByName(particleName);

        if (particleId == null) {
            particleId = Particle.getParticleIdByName(defaultParticle);
        }

        if (particleId == null) {
            particleId = Particle.TYPE_FLAME;
        }

        return new GenericParticle(position, particleId);
    }

    private static String normalizeParticle(String particleName) {
        if (particleName == null || particleName.trim().isEmpty()) {
            return "FLAME";
        }

        String normalized = particleName.trim().toUpperCase();

        if (normalized.startsWith("TYPE_")) {
            normalized = normalized.substring(5);
        }

        return normalized;
    }
}
