package ceremonious.crates;

public enum RewardType {
   Item(0),
   ConsoleCommand(1),
   PlayerCommand(2);

   private int id;

   private RewardType(int id) {
      this.id = id;
   }

   public int toInt() {
      return this.id;
   }
}