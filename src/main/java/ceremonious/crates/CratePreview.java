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

            lore.add("§7Probabilidad: §a" +
                    String.format(Locale.US, "%.2f", chance) + "%");

            // Aplicar información adicional sin alterar el objeto original.
            previewItem.setLore(lore.toArray(new String[0]));

            inventory.setItem(slot, previewItem);
            slot++;
        }

        player.addWindow(inventory);
    }
}

