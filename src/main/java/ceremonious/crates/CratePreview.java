package ceremonious.crates;

import cn.nukkit.Player;
import cn.nukkit.inventory.InventoryType;
import cn.nukkit.item.Item;
import me.iwareq.fakeinventories.FakeInventory;

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

        int slot = 0;

        // Mostrar únicamente recompensas de tipo Item.
        for (Reward reward : crate.getRewards().keySet()) {

            if (slot >= INVENTORY_SIZE) {
                break;
            }

            if (reward == null || reward.getType() != RewardType.Item) {
                continue;
            }

            Item item = reward.getItemReward();

            if (item == null || item.getId() == 0) {
                continue;
            }

            // Clonar para no modificar la recompensa original.
            inventory.setItem(slot, item.clone());
            slot++;
        }

        player.addWindow(inventory);
    }
}
