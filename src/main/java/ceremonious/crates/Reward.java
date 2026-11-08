package ceremonious.crates;

import cn.nukkit.item.Item;

public class Reward {
   RewardType rewardtype;
   private int itemNum = 0;
   private final int MAX_ITEMS = 54;
   Item itemreward;
   String rewardcommand;

   public Reward(RewardType rewardtype) {
      this.rewardtype = rewardtype;
   }

   public RewardType getType() {
      return this.rewardtype;
   }

   public void setType(RewardType rewardtype) {
      this.rewardtype = rewardtype;
   }

   public Item getItemReward() {
      return this.itemreward;
   }

   public void setItemReward(Item item) {
      if (item.count > 64) {
         item.setCount(64);
         this.itemreward = item;
      }

      if (this.itemNum < 54) {
         this.itemreward = item;
         ++this.itemNum;
      }

   }

   public int getItemNum() {
      return this.itemNum;
   }

   public void setItemNum(int itemNum) {
      this.itemNum = itemNum;
   }

   public int getMaxItems() {
      return 54;
   }

   public String getRewardCommand() {
      return this.rewardcommand;
   }

   public void setRewardCommand(String rewardcommand) {
      this.rewardcommand = rewardcommand;
   }
}