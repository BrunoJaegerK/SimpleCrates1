package ceremonious.crates;

import cn.nukkit.Player;
import cn.nukkit.item.Item;
import cn.nukkit.level.Location;
import cn.nukkit.level.Sound;
import cn.nukkit.scheduler.PluginTask;
import cn.nukkit.inventory.InventoryType;
import me.iwareq.fakeinventories.FakeInventory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

public final class CrateRouletteAnimation {

    private static final int CENTER_SLOT = 22;

    private static final int[] ROULETTE_SLOTS = {
            18, 19, 20, 21, 22, 23, 24, 25, 26
    };

    private static final int HOLD_TICKS = 10;

    private static final Random RANDOM = new Random();

    private static final Set<UUID> PLAYING = new HashSet<>();

    private CrateRouletteAnimation() {
    }

    public static boolean isPlaying(Player player) {
        return player != null && PLAYING.contains(player.getUniqueId());
    }

    public static boolean start(
            Main plugin,
            Player player,
            Location location,
            Crate crate,
            List<Reward> selectedRewards,
            Runnable onFinish
    ) {
        if (plugin == null || player == null ||
                location == null || crate == null ||
                selectedRewards == null || selectedRewards.isEmpty() ||
                onFinish == null || !player.isOnline()) {
            return false;
        }

        if (!CrateAnimationConfig.isLoaded()) {
            CrateAnimationConfig.load(plugin);
        }

        if (!CrateAnimationConfig.isRouletteEnabled()) {
            return false;
        }

        List<Reward> rewards = buildRewardList(crate);

        if (rewards.isEmpty()) {
            return false;
        }

        for (Reward selected : selectedRewards) {
            if (selected == null || !rewards.contains(selected)) {
                return false;
            }
        }

        UUID uuid = player.getUniqueId();

        if (PLAYING.contains(uuid)) {
            return false;
        }

        FakeInventory inventory = new FakeInventory(
                InventoryType.DOUBLE_CHEST,
                "§6§l" + crate.getDisplayName()
        );

        inventory.setDefaultItemHandler((item, event) ->
                event.setCancelled(true)
        );

        PLAYING.add(uuid);

        player.addWindow(inventory);

        new RouletteTask(
                plugin,
                player,
                location,
                inventory,
                rewards,
                new ArrayList<>(selectedRewards),
                onFinish
        ).start();

        return true;
    }

    // Compatibilidad con llamadas anteriores de una sola recompensa.
    public static boolean start(
            Main plugin,
            Player player,
            Location location,
            Crate crate,
            Reward selectedReward,
            Runnable onFinish
    ) {
        if (selectedReward == null) {
            return false;
        }

        return start(
                plugin,
                player,
                location,
                crate,
                Collections.singletonList(selectedReward),
                onFinish
        );
    }

    private static List<Reward> buildRewardList(Crate crate) {
        List<Reward> result = new ArrayList<>();

        for (Map.Entry<Reward, Integer> entry :
                crate.getRewards().entrySet()) {

            Reward reward = entry.getKey();
            int chance = entry.getValue();

            if (reward == null || chance <= 0) {
                continue;
            }

            for (int i = 0; i < chance; i++) {
                result.add(reward);
            }
        }

        return result;
    }

    private static Item createDisplayItem(Reward reward) {
        if (reward.getType() == RewardType.Item) {
            Item item = reward.getItemReward();

            if (item != null) {
                return item.clone();
            }

            return Item.get(1);
        }

        Item icon = Item.get(339);

        if (reward.getType() == RewardType.ConsoleCommand) {
            icon.setCustomName("§cComando de consola");
            icon.setLore(
                    "§7Recompensa de tipo comando",
                    "§7Ejecutado desde la consola"
            );
        } else if (reward.getType() == RewardType.PlayerCommand) {
            icon.setCustomName("§bComando de jugador");
            icon.setLore(
                    "§7Recompensa de tipo comando",
                    "§7Ejecutado por el jugador"
            );
        }

        return icon;
    }

    private static class RouletteTask extends PluginTask<Main> {

        private final Player player;
        private final Location location;
        private final FakeInventory inventory;
        private final List<Reward> rewards;
        private final List<Reward> selectedRewards;
        private final Runnable onFinish;

        private final int duration;

        private int roundIndex = 0;
        private int animationTick = 0;
        private int holdTicks = 0;
        private int position = RANDOM.nextInt(1000);

        private boolean holding = false;
        private boolean finished = false;

        RouletteTask(
                Main plugin,
                Player player,
                Location location,
                FakeInventory inventory,
                List<Reward> rewards,
                List<Reward> selectedRewards,
                Runnable onFinish
        ) {
            super(plugin);

            this.player = player;
            this.location = location;
            this.inventory = inventory;
            this.rewards = rewards;
            this.selectedRewards = selectedRewards;
            this.onFinish = onFinish;

            this.duration = Math.max(
                    1,
                    CrateAnimationConfig.getRouletteDuration()
            );
        }

        void start() {
            updateInventory(false);

            getOwner().getServer().getScheduler()
                    .scheduleRepeatingTask(this, 1);
        }

        @Override
        public void onRun(int currentTick) {

            if (finished) {
                return;
            }

            if (!player.isOnline()) {
                finish(false);
                return;
            }

            if (holding) {
                holdTicks++;

                if (holdTicks >= HOLD_TICKS) {
                    nextRound();
                }

                return;
            }

            animationTick++;

            double progress = Math.min(
                    1.0,
                    animationTick / (double) duration
            );

            int interval = Math.max(
                    1,
                    (int) (1 + progress * 5)
            );

            if (animationTick % interval == 0) {
                position++;

                updateInventory(false);
                playSpinSound(progress);
            }

            if (animationTick >= duration) {

                // Garantiza que el centro muestre el premio seleccionado.
                updateInventory(true);

                holding = true;
                holdTicks = 0;
            }
        }

        private void nextRound() {

            roundIndex++;

            if (roundIndex >= selectedRewards.size()) {
                finish(true);
                return;
            }

            animationTick = 0;
            holdTicks = 0;
            holding = false;
            position = RANDOM.nextInt(1000);

            updateInventory(false);
        }

        private void updateInventory(boolean finalSelection) {

            Reward selectedReward = selectedRewards.get(roundIndex);

            for (int i = 0; i < ROULETTE_SLOTS.length; i++) {

                Reward reward;

                if (finalSelection && i == 4) {
                    reward = selectedReward;
                } else {
                    int index = Math.floorMod(
                            position + i,
                            rewards.size()
                    );

                    reward = rewards.get(index);
                }

                inventory.setItem(
                        ROULETTE_SLOTS[i],
                        createDisplayItem(reward)
                );
            }

            inventory.setItem(
                    13,
                    Item.get(339).setCustomName("§e▼ PREMIO ▼")
            );

            inventory.setItem(
                    31,
                    Item.get(339).setCustomName("§e▲ PREMIO ▲")
            );
        }

        private void playSpinSound(double progress) {

            if (!CrateAnimationConfig.areRouletteSoundsEnabled()) {
                return;
            }

            if (animationTick % 5 != 0) {
                return;
            }

            playSound(
                    CrateAnimationConfig.getRouletteSpinSound(),
                    0.8f + (float) progress * 0.7f
            );
        }

        private void playSound(String soundName, float pitch) {

            try {
                Sound sound = Sound.valueOf(
                        soundName.toUpperCase()
                );

                location.getLevel().addSound(
                        player,
                        sound,
                        CrateAnimationConfig.getRouletteSoundVolume(),
                        pitch
                );

            } catch (IllegalArgumentException exception) {
                getOwner().getLogger().warning(
                        "Invalid roulette sound in animations.yml: "
                                + soundName
                );
            }
        }

        private void finish(boolean executeReward) {

            if (finished) {
                return;
            }

            finished = true;
            this.cancel();

            PLAYING.remove(player.getUniqueId());

            if (player.isOnline()) {
                player.removeWindow(inventory);

                if (executeReward) {
                    playSound(
                            CrateAnimationConfig.getRouletteFinishSound(),
                            1.0f
                    );

                    onFinish.run();
                }
            }
        }
    }
}


