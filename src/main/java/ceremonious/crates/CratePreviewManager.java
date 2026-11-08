package ceremonious.crates;

import cn.nukkit.Player;
import cn.nukkit.inventory.InventoryType;
import cn.nukkit.item.Item;
import me.iwareq.fakeinventories.FakeInventory;

import java.util.Map;

public class CratePreviewManager {

    public void openPreview(Player player, Crate crate) {
        FakeInventory inventory = new FakeInventory(
                InventoryType.DOUBLE_CHEST,
                "§8Vista previa: " + crate.getDisplayName()
        );

        // Impide retirar, mover o modificar los objetos.
        inventory.setDefaultItemHandler((item, event) ->
                event.setCancelled(true)
        );

        int slot = 0;

        for (Map.Entry<Reward, Integer> entry :
                crate.getRewards().entrySet()) {

            Reward reward = entry.getKey();

            if (reward.getType() != RewardType.Item) {
                continue;
            }

            if (reward.getItemReward() == null) {
                continue;
            }

            if (slot >= inventory.getSize()) {
                break;
            }

            Item item = reward.getItemReward().clone();

            inventory.setItem(slot, item);
            slot++;
        }

        player.addWindow(inventory);
    }
}