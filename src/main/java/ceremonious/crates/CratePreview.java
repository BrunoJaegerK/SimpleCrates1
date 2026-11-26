package ceremonious.crates;

import cn.nukkit.Player;
import cn.nukkit.inventory.InventoryType;
import cn.nukkit.item.Item;
import me.iwareq.fakeinventories.FakeInventory;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CratePreview {

    private static final int INVENTORY_SIZE = 54;

    /*
     * Minecraft legacy colors supported by Bedrock clients.
     * The palette follows a red -> yellow -> green probability gradient.
     */
    private static final ProbabilityColor[] PROBABILITY_COLORS = {
            new ProbabilityColor('4', 170, 0, 0),
            new ProbabilityColor('c', 255, 85, 85),
            new ProbabilityColor('6', 255, 170, 0),
            new ProbabilityColor('e', 255, 255, 85),
            new ProbabilityColor('a', 85, 255, 85),
            new ProbabilityColor('2', 0, 170, 0)
    };

    private CratePreview() {
    }

    public static void open(Player player, Crate crate) {
        if (player == null || crate == null) {
            return;
        }

        FakeInventory inventory = new FakeInventory(
                InventoryType.DOUBLE_CHEST,
                "Vista previa: " + crate.getName()
        );

        // Impide que el jugador retire o mueva objetos.
        inventory.setDefaultItemHandler((item, event) ->
                event.setCancelled(true)
        );

        // Calcular el peso total de todas las recompensas.
        // Incluye objetos y comandos.
        double totalWeight = 0;

        for (Integer weight : crate.getRewards().values()) {
            if (weight != null && weight > 0) {
                totalWeight += weight;
            }
        }

        int slot = 0;

        // Mostrar únicamente recompensas de tipo Item.
        for (Map.Entry<Reward, Integer> entry :
                crate.getRewards().entrySet()) {

            if (slot >= INVENTORY_SIZE) {
                break;
            }

            Reward reward = entry.getKey();

            if (reward == null ||
                    reward.getType() != RewardType.Item) {
                continue;
            }

            Item originalItem = reward.getItemReward();

            if (originalItem == null ||
                    originalItem.getId() == 0) {
                continue;
            }

            // Clonar para no modificar la recompensa original.
            Item previewItem = originalItem.clone();

            // Conservar el lore original.
            List<String> lore = new ArrayList<>();

            String[] originalLore = previewItem.getLore();

            if (originalLore != null) {
                for (String line : originalLore) {
                    lore.add(line);
                }
            }

            // Separador visual.
            lore.add(" ");

            // Nombre de la recompensa.
            String customName = previewItem.getCustomName();

            if (customName == null || customName.isEmpty()) {
                customName = previewItem.getName();
            }

            lore.add("§7Nombre: §f" + customName);

            // Cantidad que recibe el jugador.
            lore.add("§7Cantidad: §f" + originalItem.getCount());

            // Probabilidad real basada en todos los pesos.
            int weight = entry.getValue() != null
                    ? entry.getValue()
                    : 0;

            double chance = totalWeight > 0
                    ? (weight * 100.0) / totalWeight
                    : 0.0;

            String probabilityColor = getProbabilityColor(chance);
            lore.add("§7Probabilidad: §7→ " + probabilityColor
                    + String.format(Locale.US, "%.2f", chance) + "%");

            // Aplicar información adicional sin alterar el objeto original.
            previewItem.setLore(lore.toArray(new String[0]));

            inventory.setItem(slot, previewItem);
            slot++;
        }

        player.addWindow(inventory);
    }

    /**
     * Returns the closest supported Minecraft legacy color for a smooth
     * red (low chance) -> yellow (medium chance) -> green (high chance)
     * probability gradient.
     */
    private static String getProbabilityColor(double chance) {
        double percentage = Math.max(0.0, Math.min(100.0, chance));

        int red;
        int green;
        int blue = 85;

        if (percentage <= 50.0) {
            double progress = percentage / 50.0;
            red = 255;
            green = (int) Math.round(85 + (170 * progress));
        } else {
            double progress = (percentage - 50.0) / 50.0;
            red = (int) Math.round(255 - (170 * progress));
            green = 255;
        }

        ProbabilityColor closest = PROBABILITY_COLORS[0];
        double closestDistance = Double.MAX_VALUE;

        for (ProbabilityColor color : PROBABILITY_COLORS) {
            double redDifference = red - color.red;
            double greenDifference = green - color.green;
            double blueDifference = blue - color.blue;

            double distance = redDifference * redDifference
                    + greenDifference * greenDifference
                    + blueDifference * blueDifference;

            if (distance < closestDistance) {
                closestDistance = distance;
                closest = color;
            }
        }

        return "§" + closest.code;
    }

    private static final class ProbabilityColor {
        private final char code;
        private final int red;
        private final int green;
        private final int blue;

        private ProbabilityColor(char code, int red, int green, int blue) {
            this.code = code;
            this.red = red;
            this.green = green;
            this.blue = blue;
        }
    }
}
