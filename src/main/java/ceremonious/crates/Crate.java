package ceremonious.crates;

import cn.nukkit.item.Item;
import cn.nukkit.level.Location;
import java.util.ArrayList;
import java.util.HashMap;

public class Crate {
   private String name;
   private String displayName;
   private ArrayList<Reward> commands = new ArrayList();
   private HashMap<Reward, Integer> rewards = new HashMap();
   private Item key;
   private Location location;
   private boolean hasLocation = false;
   private boolean virtual = false;
   private int rounds = 1;
   private boolean particleEnabled = true;
   private boolean particleEnabledConfigured = false;
   private String particleName;
   private double particleRadius = -1.0;
   private int particlePoints = -1;

   public Crate(String name) {
      this.name = name;
      this.displayName = name;
      Main.crates.add(this);
   }

   public String getName() {
      return this.name;
   }

   public void setName(String name) {
      this.name = name;
   }

   public String getDisplayName() {
      return this.displayName;
   }

   public void setDisplayName(String name) {
      this.displayName = name;
   }

   public ArrayList<Reward> getCommands() {
      return this.commands;
   }

   public void addCommand(Reward reward) {
      this.commands.add(reward);
   }

   public void removeCommand(Reward reward) {
      this.commands.remove(reward);
   }

   public Item getKey() {
      return this.key;
   }

   public void setKey(Item key) {
      this.key = key;
   }

   public Location getLocation() {
      return this.location;
   }

   public void setLocation(Location location) {
      this.location = location;
      this.hasLocation = true;
   }

   public boolean hasLocation() {
      return this.hasLocation;
   }

   public void setVirtual(boolean virtual) {
      this.virtual = virtual;
   }

   public boolean isVirtual() {
      return this.virtual;
   }

   public void setRounds(int rounds) {
      this.rounds = rounds;
   }

   public int getRounds() {
      return this.rounds;
   }


   public boolean isParticleEnabled() {
      return this.particleEnabled;
   }

   public void setParticleEnabled(boolean enabled) {
      this.particleEnabled = enabled;
      this.particleEnabledConfigured = true;
   }

   public boolean hasParticleEnabledSetting() {
      return this.particleEnabledConfigured;
   }

   public String getParticleName() {
      return this.particleName;
   }

   public void setParticleName(String particleName) {
      this.particleName = particleName;
   }

   public double getParticleRadius() {
      return this.particleRadius;
   }

   public void setParticleRadius(double radius) {
      this.particleRadius = radius;
   }

   public int getParticlePoints() {
      return this.particlePoints;
   }

   public void setParticlePoints(int points) {
      this.particlePoints = points;
   }

   public HashMap<Reward, Integer> getRewards() {
      return this.rewards;
   }

   public void setRewards(HashMap<Reward, Integer> rewards) {
      this.rewards = rewards;
   }

   public void addReward(Reward reward) {
      this.rewards.put(reward, 20);
   }

   public void addReward(Reward reward, int chance) {
      this.rewards.put(reward, chance);
   }
}
