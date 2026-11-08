package ceremonious.crates;

import cn.nukkit.Player;
import cn.nukkit.level.Location;
import cn.nukkit.level.Sound;
import cn.nukkit.level.particle.FlameParticle;
import cn.nukkit.level.particle.Particle;
import cn.nukkit.math.Vector3;
import cn.nukkit.scheduler.PluginTask;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class CrateOpeningAnimation {

    private static final Set<UUID> PLAYING = new HashSet<>();

    private CrateOpeningAnimation() {
    }

    public static boolean isPlaying(Player player) {
        return PLAYING.contains(player.getUniqueId());
    }

    public static boolean start(
            Main plugin,
            Player player,
            Location location,
            Runnable onFinish
    ) {
        if (plugin == null || player == null ||
                location == null || location.getLevel() == null ||
                onFinish == null) {
            return false;
        }

        if (!CrateAnimationConfig.isLoaded()) {
            CrateAnimationConfig.load(plugin);
        }

        if (!CrateAnimationConfig.isClassicEnabled()) {
            return false;
        }

        UUID uuid = player.getUniqueId();

        if (!PLAYING.add(uuid)) {
            return false;
        }

        new OpeningTask(plugin, player, location, onFinish).start();

        return true;
    }

    private static class OpeningTask extends PluginTask<Main> {

        private final Player player;
        private final Location location;
        private final Runnable onFinish;

        private final int duration;
        private final double radius;
        private final double height;

        private int tick = 0;

        OpeningTask(
                Main plugin,
                Player player,
                Location location,
                Runnable onFinish
        ) {
            super(plugin);

            this.player = player;
            this.location = location;
            this.onFinish = onFinish;

            this.duration = CrateAnimationConfig.getClassicDuration();
            this.radius = CrateAnimationConfig.getClassicParticleRadius();
            this.height = CrateAnimationConfig.getClassicParticleHeight();
        }

        void start() {
            getOwner().getServer().getScheduler()
                    .scheduleRepeatingTask(this, 1);
        }

        @Override
        public void onRun(int currentTick) {

            if (!player.isOnline()) {
                finish(false);
                return;
            }

            tick++;

            double progress = Math.min(1.0, tick / (double) duration);
            double angle = tick * 0.35;
            double currentRadius = radius * progress;

            double x = location.getX() + 0.5 +
                    Math.cos(angle) * currentRadius;

            double z = location.getZ() + 0.5 +
                    Math.sin(angle) * currentRadius;

            double y = location.getY() + height +
                    Math.sin(angle * 2) * 0.25;

            if (CrateAnimationConfig.areClassicParticlesEnabled()) {
                String particleType =
                        CrateAnimationConfig.getClassicParticleType();

                if ("flame".equalsIgnoreCase(particleType)) {
                    Particle particle = new FlameParticle(
                            new Vector3(x, y, z)
                    );

                    location.getLevel().addParticle(particle);
                }
            }

            playOpeningSound(progress);

            if (tick >= duration) {
                finish(true);
            }
        }

        private void playOpeningSound(double progress) {

            if (!CrateAnimationConfig.areClassicSoundsEnabled()) {
                return;
            }

            int firstSound = Math.max(1, duration / 4);

            if (tick == 1 ||
                    tick == firstSound ||
                    tick == firstSound * 2 ||
                    tick == firstSound * 3) {

                String soundName =
                        CrateAnimationConfig.getClassicOpeningSound();

                try {
                    Sound sound = Sound.valueOf(soundName.toUpperCase());

                    float pitchStart =
                            CrateAnimationConfig.getClassicPitchStart();

                    float pitchEnd =
                            CrateAnimationConfig.getClassicPitchEnd();

                    float pitch = (float) (
                            pitchStart +
                                    (pitchEnd - pitchStart) * progress
                    );

                    float volume =
                            CrateAnimationConfig.getClassicSoundVolume();

                    location.getLevel().addSound(
                            player,
                            sound,
                            volume,
                            pitch
                    );

                } catch (IllegalArgumentException exception) {
                    getOwner().getLogger().warning(
                            "Invalid sound in animations.yml: " + soundName
                    );
                }
            }
        }

        private void finish(boolean executeReward) {
            PLAYING.remove(player.getUniqueId());
            this.cancel();

            if (executeReward && player.isOnline()) {
                onFinish.run();
            }
        }
    }
}

