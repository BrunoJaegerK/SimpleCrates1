package ceremonious.crates;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.block.Block;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.block.BlockBreakEvent;
import cn.nukkit.event.player.PlayerInteractEvent;
import cn.nukkit.event.player.PlayerInteractEvent.Action;
import cn.nukkit.inventory.Inventory;
import cn.nukkit.item.Item;
import cn.nukkit.item.enchantment.Enchantment;
import cn.nukkit.level.Level;
import cn.nukkit.level.Location;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.plugin.PluginManager;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.ConfigSection;
import cn.nukkit.utils.TextFormat;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

public class Main extends PluginBase implements Listener {
   public static ArrayList<Crate> crates = new ArrayList();
   public static String prefix = "&f[&e&lSC&r&f]&f";
   Server server;
   Config config;
   Config keysConfig;
   HashMap<UUID, Long> timeout = new HashMap();
   boolean loading = false;

   public void onLoad() {
      this.getLogger().info(TextFormat.DARK_GREEN + "Loading crates...");
      this.loading = true;
   }

   public void onEnable() {
      String ver = "1.0.2";
      this.server = this.getServer();
      ArrayList<String> commandsCFG = new ArrayList();
      commandsCFG.add("give {user} diamond 1");
      commandsCFG.add("tell {user} Thanks for downloading SimpleCrates!");
      List<String> keylore = new ArrayList();
      keylore.add("&eUse on the {name} crate!");
      List<String> lore = new ArrayList();
      lore.add("Very special indeed!");
      ArrayList<String> commandsCFG2 = new ArrayList();
      commandsCFG2.add("me is awesome for running this plugin!");
      ConfigSection cs = new ConfigSection();
      cs.set("version", ver);
      cs.set("options.prefix", "&f[&e&lSC&r&f]&f");
      cs.set("crates.default.displayname", "&e&lSimple Crate");
      cs.set("crates.default.rounds", 1);
      cs.set("crates.default.key.virtual", false);
      cs.set("crates.default.key.id", 131);
      cs.set("crates.default.key.durability", 0);
      cs.set("crates.default.key.name", "{displayname} Key");
      cs.set("crates.default.key.lore", keylore);
      cs.set("crates.default.key.enchantments.17", 3);
      cs.set("crates.default.commands.console", commandsCFG);
      cs.set("crates.default.commands.player", commandsCFG2);
      cs.set("crates.default.rewards.items.enchantedglass.id", 20);
      cs.set("crates.default.rewards.items.enchantedglass.durability", 0);
      cs.set("crates.default.rewards.items.enchantedglass.chance", 100);
      cs.set("crates.default.rewards.items.enchantedglass.name", "&r&bEnchanted Glass");
      cs.set("crates.default.rewards.items.enchantedglass.lore", lore);
      cs.set("crates.default.rewards.items.enchantedglass.amount", 16);
      cs.set("crates.default.rewards.items.enchantedglass.enchantments.0", 0);
      cs.set("crates.default.rewards.items.enchantedglass.enchantments.5", 2);
      cs.set("crates.default.rewards.items.stone-1.id", 1);
      cs.set("crates.default.rewards.items.stone-1.durability", 0);
      cs.set("crates.default.rewards.items.stone-1.chance", 100);
      cs.set("crates.default.rewards.items.stone-1.name", "null");
      cs.set("crates.default.rewards.items.stone-1.amount", 16);
      cs.set("crates.default.rewards.commands.console.give {user} cookie 1.chance", 20);
      cs.set("crates.default.rewards.commands.console.give {user} cookie 1.chance", 20);
      cs.set("crates.default.rewards.commands.player.me I'm epic for using this plugin!.chance", 20);
      this.config = new Config(new File(this.getDataFolder(), "config.yml"), 2, cs);
      ConfigSection cs2 = new ConfigSection();
      cs2.set("keys.example-uuid.default", 0);
      this.keysConfig = new Config(new File(this.getDataFolder(), "keys.yml"), 2, cs2);
      this.saveDefaultConfig();
      this.getServer().getPluginManager().registerEvents(this, this);
      String version = this.getConfig().getString("version", ver);
      if (version.equals(ver)) {
         this.getLogger().info(TextFormat.DARK_GREEN + "SimpleCrates is up to date, with configuration version " + ver + "!");
         this.loadCrates();
         CrateParticleTask.start(this);
         CrateFailureEffect.load(this);
         this.getLogger().info(TextFormat.DARK_GREEN + "Plugin enabled!");
      } else {
         this.getLogger().info(TextFormat.RED + "Uh oh! The server configuration version isn't " + ver + ", this may cause errors, so we have closed the plugin. Delete config.yml and reload your server to update!");
         PluginManager pm = this.getServer().getPluginManager();
         pm.disablePlugin(this);
      }
   }

   public void onDisable() {
      this.getLogger().info(TextFormat.DARK_GREEN + "Plugin disabled!");
   }

   public void loadCrates() {
      String getprefix = this.config.getString("options.prefix");
      prefix = getprefix;
      ConfigSection section = this.config.getSection("crates");
      if (section == null) {
         this.getLogger().info(TextFormat.RED + "Error in config! There are no crates!");
      } else {
         for(String cratename : section.getKeys(false)) {
            String cratedname = this.config.getString("crates." + cratename + ".displayname");
            if (cratename.equals("")) {
               this.getLogger().info(TextFormat.RED + "Error in config! There is a Crate with no name!");
               return;
            }

            if (this.loading) {
               for(Crate listcrates : crates) {
                  if (listcrates.getName().equals(cratename)) {
                     this.getLogger().info(TextFormat.RED + "Error in config! Crate '" + listcrates.getName() + "' already exists!");
                     return;
                  }
               }
            }

            Crate crate = new Crate(cratename);
            int getrounds = this.config.getInt("crates." + cratename + ".rounds");
            if (getrounds <= 0) {
               crate.setRounds(1);
            } else {
               crate.setRounds(getrounds);
            }

            boolean keyv = this.config.getBoolean("crates." + cratename + ".key.virtual");
            crate.setVirtual(keyv);
            int keyid = this.config.getInt("crates." + cratename + ".key.id");
            Item key = Item.get(keyid);
            key.setDamage(this.config.getInt("crates." + cratename + ".key.durability"));
            if (!cratedname.equals((Object)null) && !cratedname.equals("")) {
               crate.setDisplayName(cratedname);
               key.setCustomName(TextFormat.colorize("&r" + this.config.getString("crates." + cratename + ".key.name").replace("{displayname}", crate.getDisplayName()).replace("{name}", crate.getName()).replace('§', '&')));
               ArrayList<String> itemlorelist = new ArrayList();
               if (!this.config.getStringList("crates." + cratename + ".key.lore").isEmpty()) {
                  for(String ll : this.config.getStringList("crates." + cratename + ".key.lore")) {
                     String nl = ll.replace("{displayname}", crate.getDisplayName()).replace("{name}", crate.getName()).replace('§', '&');
                     itemlorelist.add(TextFormat.colorize("&r" + nl));
                  }

                  String[] itemlore = (String[])itemlorelist.toArray(new String[0]);
                  if (!this.isEmptyStringArray(itemlore)) {
                     key.setLore(itemlore);
                  }
               }
            } else {
               crate.setDisplayName("&r" + cratename);
               key.setCustomName(TextFormat.colorize("&r" + this.config.getString("crates." + cratename + ".key.name").replace("{displayname}", crate.getDisplayName()).replace("{name}", crate.getName()).replace('§', '&')));
               ArrayList<String> itemlorelist = new ArrayList();
               if (!this.config.getStringList("crates." + cratename + ".key.lore").isEmpty()) {
                  for(String ll : this.config.getStringList("crates." + cratename + ".key.lore")) {
                     String nl = ll.replace("{displayname}", crate.getDisplayName()).replace("{name}", crate.getName()).replace('§', '&');
                     itemlorelist.add(TextFormat.colorize("&r" + nl));
                  }

                  String[] itemlore = (String[])itemlorelist.toArray(new String[0]);
                  if (!this.isEmptyStringArray(itemlore)) {
                     key.setLore(itemlore);
                  }
               }
            }

            for(String enchantID : this.config.getSection("crates." + cratename + ".key.enchantments").getKeys()) {
               int enchantIDi = Integer.parseInt(enchantID);
               int enchantLVL = this.config.getInt("crates." + cratename + ".key.enchantments." + enchantIDi);
               key.addEnchantment(new Enchantment[]{Enchantment.getEnchantment(enchantIDi).setLevel(enchantLVL)});
            }

            crate.setKey(key);
            if (section.exists(cratename + ".commands.console")) {
               for(String command : this.config.getStringList("crates." + cratename + ".commands.console")) {
                  Reward reward = new Reward(RewardType.ConsoleCommand);
                  reward.setRewardCommand(command);
                  crate.addCommand(reward);
               }
            }

            if (section.exists(cratename + ".commands.player")) {
               for(String command : this.config.getStringList("crates." + cratename + ".commands.player")) {
                  Reward reward = new Reward(RewardType.PlayerCommand);
                  reward.setRewardCommand(command);
                  crate.addCommand(reward);
               }
            }

            if (section.exists(cratename + ".location")) {
               Level level = this.server.getLevelByName(this.config.getString("crates." + cratename + ".location.world"));
               double x = this.config.getDouble("crates." + cratename + ".location.x");
               double y = this.config.getDouble("crates." + cratename + ".location.y");
               double z = this.config.getDouble("crates." + cratename + ".location.z");
               Location location = new Location(x, y, z, level);
               crate.setLocation(location);
            }

            if (section.exists(cratename + ".rewards.commands.console")) {
               for(String rcommands : this.config.getSection("crates." + cratename + ".rewards.commands.console").keySet()) {
                  if (rcommands != null && !rcommands.equals("null")) {
                     Reward reward = new Reward(RewardType.ConsoleCommand);
                     reward.setRewardCommand(rcommands);
                     int chance = this.config.getInt("crates." + cratename + ".rewards.commands.console." + rcommands + ".chance");
                     if (chance <= 0) {
                        chance = 1;
                     }

                     crate.addReward(reward, chance);
                  }
               }
            }

            if (section.exists(cratename + ".rewards.commands.player")) {
               for(String rcommands : this.config.getSection("crates." + cratename + ".rewards.commands.player").keySet()) {
                  if (rcommands != null && !rcommands.equals("null")) {
                     Reward reward = new Reward(RewardType.PlayerCommand);
                     reward.setRewardCommand(rcommands);
                     int chance = this.config.getInt("crates." + cratename + ".rewards.commands.player." + rcommands + ".chance");
                     if (chance <= 0) {
                        chance = 1;
                     }

                     crate.addReward(reward, chance);
                  }
               }
            }

            for(String itemnameid : this.config.getSection("crates." + cratename + ".rewards.items").getKeys(false)) {
               int itemid = this.config.getInt("crates." + cratename + ".rewards.items." + itemnameid + ".id");
               String itemname = this.config.getString("crates." + cratename + ".rewards.items." + itemnameid + ".name");
               ArrayList<String> itemlorelist = new ArrayList();
               Item item = Item.get(itemid);
               if (!this.config.getStringList("crates." + cratename + ".rewards.items." + itemnameid + ".lore").isEmpty()) {
                  for(String ll : this.config.getStringList("crates." + cratename + ".rewards.items." + itemnameid + ".lore")) {
                     itemlorelist.add(TextFormat.colorize(ll));
                  }

                  String[] itemlore = (String[])itemlorelist.toArray(new String[0]);
                  if (!this.isEmptyStringArray(itemlore)) {
                     item.setLore(itemlore);
                  }
               }

               int itemdur = this.config.getInt("crates." + cratename + ".rewards.items." + itemnameid + ".durability");
               int chance = this.config.getInt("crates." + cratename + ".rewards.items." + itemnameid + ".chance");
               int itemamt = this.config.getInt("crates." + cratename + ".rewards.items." + itemnameid + ".amount");
               item.setDamage(itemdur);
               if (!itemname.equals("null")) {
                  item.setCustomName(TextFormat.colorize("&r" + itemname));
               }

               Set<String> keys = this.config.getRootSection().getSection("crates." + cratename + ".rewards.items." + itemnameid + ".enchantments").getKeys(false);
               if (keys != null) {
                  for(String enchantID : this.config.getSection("crates." + cratename + ".rewards.items." + itemnameid + ".enchantments").getKeys(false)) {
                     int enchantIDi = Integer.parseInt(enchantID);
                     int enchantLVL = this.config.getInt("crates." + cratename + ".rewards.items." + itemnameid + ".enchantments." + enchantIDi);
                     item.addEnchantment(new Enchantment[]{Enchantment.getEnchantment(enchantIDi).setLevel(enchantLVL)});
                  }
               }

               item.setCount(itemamt);
               Reward newReward = new Reward(RewardType.Item);
               newReward.setItemReward(item);
               if (!crate.getRewards().containsKey(newReward)) {
                  crate.addReward(newReward, chance);
               }
            }
         }

         this.getLogger().info(TextFormat.DARK_GREEN + "Crates loaded!");
         this.loading = false;
      }
   }

   public void saveCrates() {
      if (!crates.isEmpty()) {
         for(Crate crate : crates) {
            String name = crate.getName();
            String dname = crate.getDisplayName();
            if (!dname.equals((Object)null) && !dname.equals("")) {
               this.config.set("crates." + name + ".displayname", dname);
            } else {
               this.config.set("crates." + name + ".displayname", name);
            }

            Item key = crate.getKey();
            this.config.set("crates." + name + ".rounds", crate.getRounds());
            this.config.set("crates." + name + ".key.virtual", crate.isVirtual());
            this.config.set("crates." + name + ".key.id", key.getId());
            this.config.set("crates." + name + ".key.durability", key.getDamage());
            this.config.set("crates." + name + ".key.name", key.getCustomName().replace('§', '&'));
            this.config.set("crates." + name + ".key.lore", key.getLore());
            HashMap<String, Integer> keyenchantments = new HashMap();
            if (key.hasEnchantments()) {
               Enchantment[] var10;
               for(Enchantment enchants : var10 = key.getEnchantments()) {
                  String eID = String.valueOf(enchants.getId());
                  int eLvl = enchants.getLevel();
                  keyenchantments.put(eID, eLvl);
               }

               this.config.set("crates." + name + ".key.enchantments", keyenchantments);
            }

            Location location = crate.getLocation();
            if (crate.hasLocation()) {
               this.config.set("crates." + name + ".location.world", location.getLevel().getName());
               this.config.set("crates." + name + ".location.x", location.getX());
               this.config.set("crates." + name + ".location.y", location.getY());
               this.config.set("crates." + name + ".location.z", location.getZ());
            }

            if (!crate.getCommands().isEmpty()) {
               for(Reward reward : crate.getCommands()) {
                  if (reward.getType().equals(RewardType.ConsoleCommand)) {
                     ArrayList<String> cc = (ArrayList)this.config.getStringList("crates." + name + ".commands.console");
                     if (!cc.contains(reward.getRewardCommand())) {
                        cc.add(reward.getRewardCommand());
                     }

                     this.config.set("crates." + name + ".commands.console", cc);
                  } else if (reward.getType().equals(RewardType.PlayerCommand)) {
                     ArrayList<String> pc = (ArrayList)this.config.getStringList("crates." + name + ".commands.player");
                     if (!pc.contains(reward.getRewardCommand())) {
                        pc.add(reward.getRewardCommand());
                     }

                     this.config.set("crates." + name + ".commands.player", pc);
                  }
               }
            }

            if (!crate.getRewards().isEmpty()) {
               for(Reward reward : crate.getRewards().keySet()) {
                  if (reward != null) {
                     if (reward.getType().equals(RewardType.ConsoleCommand)) {
                        if (!reward.getRewardCommand().equals("null")) {
                           this.config.set("crates." + name + ".rewards.commands.console." + reward.getRewardCommand() + ".chance", crate.getRewards().get(reward));
                        }
                     } else if (reward.getType().equals(RewardType.PlayerCommand) && !reward.getRewardCommand().equals("null")) {
                        this.config.set("crates." + name + ".rewards.commands.player." + reward.getRewardCommand() + ".chance", crate.getRewards().get(reward));
                     }
                  }
               }
            }
         }

         this.config.save();
         this.getLogger().info(TextFormat.DARK_GREEN + "Crates saved!");
      } else {
         this.config.set("crates", (Object)null);
         this.config.save();
         this.getLogger().info(TextFormat.DARK_GREEN + "No more crates!");
      }

   }

   public boolean isEmptyStringArray(String[] array) {
      for(int i = 0; i < array.length; ++i) {
         if (array[i] != null) {
            return false;
         }
      }

      return true;
   }


   @EventHandler
   public void onBlockClick(PlayerInteractEvent e) {
      Player p = e.getPlayer();

      if ((e.getAction() == Action.RIGHT_CLICK_BLOCK || e.getAction() == Action.LEFT_CLICK_BLOCK)
              && e.getBlock().getId() != 0) {

         Block block = e.getBlock();
         Location l = block.getLocation();

         for (Crate crate : crates) {
            Location c = crate.getLocation();

            if (l.equals(c)) {
               e.setCancelled(true);

               // Clic derecho: abrir únicamente la vista previa.
               if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
                  CratePreview.open(p, crate);
                  break;
               }

               // Evitar abrir otra crate mientras se reproduce una animación.
               if (CrateOpeningAnimation.isPlaying(p)) {
                  break;
               }

               // Desde aquí se conserva la lógica original de apertura.
               Long time = System.currentTimeMillis() - 5L;

               if (this.timeout.containsKey(e.getPlayer().getUniqueId())) {
                  time = (Long) this.timeout.get(e.getPlayer().getUniqueId());
               } else {
                  time = System.currentTimeMillis() - 5L;
               }

               if (System.currentTimeMillis() < time) {
                  return;
               }

               this.timeout.put(e.getPlayer().getUniqueId(), System.currentTimeMillis() + 500L);

               if (crate.isVirtual()) {
                  int value = this.keysConfig.getInt("keys." + p.getUniqueId() + "." + crate.getName());

                  if (value <= 0) {
                     CrateFailureEffect.play(p, c);
                     p.sendMessage(TextFormat.colorize(prefix + " You do not have a " + crate.getDisplayName() + " Key!"));
                     this.keysConfig.set("keys." + p.getUniqueId() + "." + crate.getName(), 0);
                  } else {
                     this.keysConfig.set("keys." + p.getUniqueId() + "." + crate.getName(), value - 1);
                     this.keysConfig.save();

                     if (crate.getRewards().isEmpty()) {
                        p.sendMessage(TextFormat.colorize(prefix + " This crate is empty!"));
                     } else {

                        Runnable rewardAction = () -> {
                           p.sendMessage(TextFormat.colorize(prefix + " You opened a crate!"));

                           if (crate.getRounds() > crate.getRewards().size()) {
                              crate.setRounds(crate.getRewards().size());
                           }

                           for (int i = 0; i < crate.getRounds(); ++i) {
                              if (!crate.getCommands().isEmpty()) {
                                 for (Reward command : crate.getCommands()) {
                                    if (command.getType().equals(RewardType.ConsoleCommand)) {
                                       this.server.dispatchCommand(
                                               this.server.getConsoleSender(),
                                               command.getRewardCommand().replace("{user}", p.getName())
                                       );
                                    } else if (command.getType().equals(RewardType.PlayerCommand)) {
                                       this.server.dispatchCommand(
                                               p,
                                               command.getRewardCommand().replace("{user}", p.getName())
                                       );
                                    }
                                 }
                              }

                              if (!crate.getRewards().isEmpty()) {
                                 Random rnd = new Random();
                                 List<Reward> rewardChances = new ArrayList();
                                 Map<Reward, Integer> map = crate.getRewards();

                                 for (Map.Entry<Reward, Integer> entry : map.entrySet()) {
                                    for (int x = 0; x < (Integer) entry.getValue(); ++x) {
                                       rewardChances.add((Reward) entry.getKey());
                                    }
                                 }

                                 Reward reward = (Reward) rewardChances.get(rnd.nextInt(rewardChances.size()));

                                 if (reward.getType().equals(RewardType.Item)) {
                                    p.getInventory().addItem(new Item[]{reward.getItemReward()});
                                 } else if (reward.getType().equals(RewardType.ConsoleCommand)) {
                                    this.server.dispatchCommand(
                                            this.server.getConsoleSender(),
                                            reward.getRewardCommand().replace("{user}", p.getName())
                                    );
                                 } else if (reward.getType().equals(RewardType.PlayerCommand)) {
                                    this.server.dispatchCommand(
                                            p,
                                            reward.getRewardCommand().replace("{user}", p.getName())
                                    );
                                 }
                              }
                           }
                        };

                        boolean animationStarted = CrateOpeningAnimation.start(
                                this,
                                p,
                                c,
                                rewardAction
                        );

                        if (!animationStarted) {
                           rewardAction.run();
                        }
                     }
                  }
               } else if (e.getItem().equals(crate.getKey())) {
                  if (crate.getRewards().isEmpty()) {
                     p.sendMessage(TextFormat.colorize(prefix + " This crate is empty!"));
                  } else {

                     // Consumir la llave física antes de iniciar la animación.
                     int itemcount = p.getInventory().getItemInHand().getCount();
                     Item newkeys = crate.getKey();
                     newkeys.setCount(itemcount - 1);

                     if (itemcount == 1) {
                        p.getInventory().remove(p.getInventory().getItemInHand());
                     } else {
                        p.getInventory().setItemInHand(newkeys);
                     }

                     Runnable rewardAction = () -> {
                        p.sendMessage(TextFormat.colorize(prefix + " You opened a crate!"));

                        if (crate.getRounds() > crate.getRewards().size()) {
                           crate.setRounds(crate.getRewards().size());
                        }

                        for (int i = 0; i < crate.getRounds(); ++i) {
                           if (!crate.getCommands().isEmpty()) {
                              for (Reward command : crate.getCommands()) {
                                 if (command.getType().equals(RewardType.ConsoleCommand)) {
                                    this.server.dispatchCommand(
                                            this.server.getConsoleSender(),
                                            command.getRewardCommand().replace("{user}", p.getName())
                                    );
                                 } else if (command.getType().equals(RewardType.PlayerCommand)) {
                                    this.server.dispatchCommand(
                                            p,
                                            command.getRewardCommand().replace("{user}", p.getName())
                                    );
                                 }
                              }
                           }

                           if (!crate.getRewards().isEmpty()) {
                              Random rnd = new Random();
                              List<Reward> rewardChances = new ArrayList();
                              Map<Reward, Integer> map = crate.getRewards();

                              for (Map.Entry<Reward, Integer> entry : map.entrySet()) {
                                 for (int x = 0; x < (Integer) entry.getValue(); ++x) {
                                    rewardChances.add((Reward) entry.getKey());
                                 }
                              }

                              Reward reward = (Reward) rewardChances.get(
                                      rnd.nextInt(rewardChances.size())
                              );

                              if (reward.getType().equals(RewardType.Item)) {
                                 p.getInventory().addItem(new Item[]{reward.getItemReward()});
                              } else if (reward.getType().equals(RewardType.ConsoleCommand)) {
                                 this.server.dispatchCommand(
                                         this.server.getConsoleSender(),
                                         reward.getRewardCommand().replace("{user}", p.getName())
                                 );
                              } else if (reward.getType().equals(RewardType.PlayerCommand)) {
                                 this.server.dispatchCommand(
                                         p,
                                         reward.getRewardCommand().replace("{user}", p.getName())
                                 );
                              }
                           }
                        }
                     };

                     boolean animationStarted = CrateOpeningAnimation.start(
                             this,
                             p,
                             c,
                             rewardAction
                     );

                     if (!animationStarted) {
                        rewardAction.run();
                     }
                  }
               } else {
                  CrateFailureEffect.play(p, c);
                  p.sendMessage(TextFormat.colorize(prefix + " You do not have a " + crate.getDisplayName() + " Key!"));
               }

               break;
            }
         }
      }
   }

   @EventHandler
   public void playerBlockPick(BlockBreakEvent e) {
      for(Crate crate : crates) {
         Location l = e.getBlock().getLocation();
         Location c = crate.getLocation();
         if (l.equals(c)) {
            e.setCancelled(true);
         }
      }

   }

   public static boolean canParse(String message) {
      try {
         Integer.parseInt(message);
         return true;
      } catch (Exception var2) {
         return false;
      }
   }

   public void sendHelp(Player sender) {
      sender.sendMessage(TextFormat.colorize("&e&lCrates Help:"));
      sender.sendMessage(TextFormat.colorize("- /keys"));
      sender.sendMessage(TextFormat.colorize("- /crates read"));
      sender.sendMessage(TextFormat.colorize("- /crates list"));
      sender.sendMessage(TextFormat.colorize("- /crates create <crate>"));
      sender.sendMessage(TextFormat.colorize("- /crates delete <crate>"));
      sender.sendMessage(TextFormat.colorize("- /crates setblock <crate>"));
      sender.sendMessage(TextFormat.colorize("- /crates additem <crate>"));
      sender.sendMessage(TextFormat.colorize("- /crates addcmd <crate> <command>"));
      sender.sendMessage(TextFormat.colorize("- /crates removecmd <crate> <command>"));
      sender.sendMessage(TextFormat.colorize("- /crates addplayercmd <crate> <command>"));
      sender.sendMessage(TextFormat.colorize("- /crates removeplayercmd <crate> <command>"));
      sender.sendMessage(TextFormat.colorize("- /crates givekey <player> <crate> <amount>"));
      sender.sendMessage(TextFormat.colorize("- /crates givekeyall <crate> <amount>"));
   }

   public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
      if (command.getName().equalsIgnoreCase("crates")) {
         Player p = null;
         if (sender instanceof Player) {
            p = (Player)sender;
         }

         if (args.length < 1) {
            this.sendHelp(p);
            return false;
         }

         if (args.length == 1) {
            if (args[0].equalsIgnoreCase("read")) {
               if (!(sender instanceof Player)) {
                  sender.sendMessage(TextFormat.colorize(prefix + " You must be ingame to run this command!"));
                  return false;
               }

               Item item = p.getInventory().getItemInHand();
               if (item.getName().toLowerCase().equals("air")) {
                  p.sendMessage(TextFormat.colorize(prefix + " You don't have an item in your hand!"));
                  return false;
               }

               p.sendMessage(TextFormat.colorize("ID: " + item.getId()));
               p.sendMessage(TextFormat.colorize("DURABILITY: " + item.getDamage()));
               if (item.hasCustomName()) {
                  p.sendMessage(TextFormat.colorize("NAME: " + item.getCustomName()));
               } else {
                  p.sendMessage(TextFormat.colorize("NAME: " + item.getName()));
               }

               if (item.hasEnchantments()) {
                  p.sendMessage(TextFormat.colorize("ENCHANTMENTS:"));
                  Enchantment[] enchantments = item.getEnchantments();

                  for(Enchantment enchant : enchantments) {
                     p.sendMessage(TextFormat.colorize("- '" + enchant.getId() + "': " + enchant.getLevel()));
                  }
               }

               return false;
            }

            if (!args[0].equalsIgnoreCase("list")) {
               this.sendHelp(p);
               return false;
            }

            if (crates.isEmpty()) {
               sender.sendMessage(TextFormat.colorize(prefix + " There are no crates!"));
               return false;
            }

            sender.sendMessage(TextFormat.colorize("&e&lList of Crates:"));

            for(Crate crate : crates) {
               sender.sendMessage(TextFormat.colorize("- " + crate.getName()));
            }

            return false;
         }

         if (args.length == 2) {
            if (args[0].equalsIgnoreCase("create")) {
               for(Crate crate : crates) {
                  if (crate.getName().toLowerCase().equals(args[1].toLowerCase())) {
                     sender.sendMessage(TextFormat.colorize(prefix + " Crate " + crate.getName() + " already exists!"));
                     return false;
                  }
               }

               Crate crate = new Crate(args[1]);
               Item key = Item.get(131);
               key.setCustomName(TextFormat.colorize("&r&e&l" + args[1] + " Key"));
               key.setLore(new String[]{TextFormat.colorize("&r&eUse on the " + args[1] + " crate!")});
               key.addEnchantment(new Enchantment[]{Enchantment.getEnchantment(17).setLevel(3)});
               crate.setKey(key);
               this.saveCrates();
               sender.sendMessage(TextFormat.colorize(prefix + " Crate " + crate.getName() + " created!"));
               return false;
            }

            if (args[0].equalsIgnoreCase("delete")) {
               Iterator<Crate> id = crates.iterator();

               while(id.hasNext()) {
                  Crate crate = (Crate)id.next();
                  if (crate.getName().toLowerCase().equals(args[1].toLowerCase())) {
                     id.remove();
                     ConfigSection section = this.config.getSection("crates");
                     section.remove(crate.getName());
                     this.config.save();
                     sender.sendMessage(TextFormat.colorize(prefix + " Crate " + crate.getName() + " removed!"));
                     return false;
                  }
               }

               sender.sendMessage(TextFormat.colorize(prefix + " That crate doesn't exist!"));
               return false;
            }

            if (args[0].equalsIgnoreCase("setblock")) {
               if (!(sender instanceof Player)) {
                  sender.sendMessage(TextFormat.colorize(prefix + " You must be ingame to run this command!"));
                  return false;
               }

               if (crates.isEmpty()) {
                  sender.sendMessage(TextFormat.colorize(prefix + " There are no crates!"));
                  return false;
               }

               for(Crate crate : crates) {
                  if (crate.getName().toLowerCase().equals(args[1].toLowerCase())) {
                     Location l = p.getTargetBlock(5).getLocation();
                     crate.setLocation(l);
                     this.saveCrates();
                     p.sendMessage(TextFormat.colorize(prefix + " Location set!"));
                     return false;
                  }
               }

               p.sendMessage(TextFormat.colorize(prefix + " That crate doesn't exist!"));
               return false;
            }

            if (!args[0].equalsIgnoreCase("additem")) {
               this.sendHelp(p);
               return false;
            }

            if (!(sender instanceof Player)) {
               sender.sendMessage(TextFormat.colorize(prefix + " You must be ingame to run this command!"));
               return false;
            }

            if (crates.isEmpty()) {
               sender.sendMessage(TextFormat.colorize(prefix + " There are no crates!"));
               return false;
            }

            for(Crate crate : crates) {
               if (crate.getName().toLowerCase().equals(args[1].toLowerCase())) {
                  Item item = p.getInventory().getItemInHand();
                  if (item.getName().toLowerCase().equals("air")) {
                     p.sendMessage(TextFormat.colorize(prefix + " You don't have an item in your hand!"));
                     return false;
                  }

                  Reward reward = new Reward(RewardType.Item);
                  reward.setItemReward(item);
                  crate.addReward(reward, 100);
                  int itemID = item.getId();
                  int itemMeta = item.getDamage();
                  int itemAmt = item.getCount();
                  String[] itemlore = item.getLore();
                  String itemname;
                  if (item.hasCustomName()) {
                     itemname = item.getCustomName();
                  } else {
                     itemname = item.getName();
                  }

                  HashMap<String, Integer> enchantments = new HashMap();
                  String itemnameid = item.getName();
                  int count = 0;

                  for(String itemnamesexist : this.config.getSection("crates." + crate.getName() + ".rewards.items").getKeys(false)) {
                     if (itemnamesexist.isEmpty()) {
                        count = 1;
                     } else {
                        ++count;
                     }
                  }

                  itemnameid = itemnameid + "-" + count;
                  itemnameid = itemnameid.replaceAll(" ", "-").toLowerCase();
                  this.config.set("crates." + crate.getName() + ".rewards.items." + itemnameid + ".id", itemID);
                  this.config.set("crates." + crate.getName() + ".rewards.items." + itemnameid + ".durability", itemMeta);
                  this.config.set("crates." + crate.getName() + ".rewards.items." + itemnameid + ".chance", 100);
                  if (item.hasCustomName()) {
                     this.config.set("crates." + crate.getName() + ".rewards.items." + itemnameid + ".name", itemname.replace('§', '&'));
                  } else {
                     this.config.set("crates." + crate.getName() + ".rewards.items." + itemnameid + ".name", "null");
                  }

                  this.config.set("crates." + crate.getName() + ".rewards.items." + itemnameid + ".lore", itemlore);
                  this.config.set("crates." + crate.getName() + ".rewards.items." + itemnameid + ".amount", itemAmt);
                  if (item.hasEnchantments()) {
                     Enchantment[] var21;
                     for(Enchantment enchants : var21 = item.getEnchantments()) {
                        String eID = String.valueOf(enchants.getId());
                        int eLvl = enchants.getLevel();
                        enchantments.put(eID, eLvl);
                     }

                     this.config.set("crates." + crate.getName() + ".rewards.items." + itemnameid + ".enchantments", enchantments);
                  }

                  this.config.save();
                  p.sendMessage(TextFormat.colorize(prefix + " Item added to crate " + crate.getName() + "!"));
                  return false;
               }
            }

            p.sendMessage(TextFormat.colorize(prefix + " That crate doesn't exist!"));
            return false;
         }

         if (args.length == 3) {
            if (args[0].equalsIgnoreCase("givekeyall")) {
               if (crates.isEmpty()) {
                  sender.sendMessage(TextFormat.colorize(prefix + " There are no crates!"));
                  return false;
               }

               for(Crate crate : crates) {
                  if (crate.getName().toLowerCase().equals(args[1].toLowerCase())) {
                     for(Player p2 : this.server.getOnlinePlayers().values()) {
                        if (crate.isVirtual()) {
                           if (!canParse(args[2])) {
                              if (sender instanceof Player) {
                                 sender.sendMessage(TextFormat.colorize(prefix + " Invalid number!"));
                              } else {
                                 this.getLogger().info(TextFormat.DARK_GREEN + "Invalid number!");
                              }

                              return false;
                           }

                           int amount = Integer.parseInt(args[2]);
                           if (amount < 1) {
                              if (sender instanceof Player) {
                                 sender.sendMessage(TextFormat.colorize(prefix + " Invalid number!"));
                              } else {
                                 this.getLogger().info(TextFormat.DARK_GREEN + "Invalid number!");
                              }

                              return false;
                           }

                           if (this.keysConfig.exists("keys." + p2.getUniqueId() + "." + crate.getName())) {
                              int value = this.keysConfig.getInt("keys." + p2.getUniqueId() + "." + crate.getName());
                              this.keysConfig.set("keys." + p2.getUniqueId() + "." + crate.getName(), value + amount);
                           } else {
                              this.keysConfig.set("keys." + p2.getUniqueId() + "." + crate.getName(), amount);
                           }

                           this.keysConfig.save();
                           if (amount == 1) {
                              p2.sendMessage(TextFormat.colorize(prefix + " You've recieved a " + crate.getDisplayName() + " Key!"));
                           } else {
                              p2.sendMessage(TextFormat.colorize(prefix + " You've recieved " + amount + " " + crate.getDisplayName() + " Keys!"));
                           }
                        } else {
                           Inventory inv = p2.getInventory();
                           if (!inv.canAddItem(crate.getKey())) {
                              this.getLogger().info(p2.getName() + "'s inventory is full!");
                              if (sender instanceof Player) {
                                 sender.sendMessage(TextFormat.colorize(prefix + " " + p2.getName() + "'s inventory is full!"));
                              }
                           } else {
                              if (!canParse(args[2])) {
                                 if (sender instanceof Player) {
                                    sender.sendMessage(TextFormat.colorize(prefix + " Invalid number!"));
                                 } else {
                                    this.getLogger().info(TextFormat.DARK_GREEN + "Invalid number!");
                                 }

                                 return false;
                              }

                              int amount = Integer.parseInt(args[2]);
                              if (amount < 1) {
                                 if (sender instanceof Player) {
                                    sender.sendMessage(TextFormat.colorize(prefix + " Invalid number!"));
                                 } else {
                                    this.getLogger().info(TextFormat.DARK_GREEN + "Invalid number!");
                                 }

                                 return false;
                              }

                              Item item = crate.getKey();
                              item.setCount(amount);
                              p2.getInventory().addItem(new Item[]{crate.getKey()});
                              if (amount == 1) {
                                 p2.sendMessage(TextFormat.colorize(prefix + " You've recieved a " + crate.getDisplayName() + " Key!"));
                              } else {
                                 p2.sendMessage(TextFormat.colorize(prefix + " You've recieved " + amount + " " + crate.getDisplayName() + " keys!"));
                              }
                           }
                        }
                     }

                     int amount = Integer.parseInt(args[2]);
                     if (amount == 1) {
                        this.getLogger().info(TextFormat.DARK_GREEN + "Gave " + crate.getName() + " key to all players!");
                        if (sender instanceof Player) {
                           sender.sendMessage(TextFormat.colorize(prefix + " Gave " + crate.getName() + " key to all players!"));
                        }
                     } else {
                        this.getLogger().info(TextFormat.DARK_GREEN + "Gave " + amount + " " + crate.getName() + " keys to all players!");
                        if (sender instanceof Player) {
                           sender.sendMessage(TextFormat.colorize(prefix + " Gave " + amount + " " + crate.getName() + " keys to all players!"));
                        }
                     }

                     return false;
                  }
               }

               sender.sendMessage(TextFormat.colorize(prefix + " That crate doesn't exist!"));
               return false;
            }

            if (args[0].equalsIgnoreCase("addcmd")) {
               if (crates.isEmpty()) {
                  sender.sendMessage(TextFormat.colorize(prefix + " There are no crates!"));
                  return false;
               }

               for(Crate crate : crates) {
                  if (crate.getName().toLowerCase().equals(args[1].toLowerCase())) {
                     for(Reward reward : crate.getRewards().keySet()) {
                        if (reward.getType().equals(RewardType.ConsoleCommand) && reward.getRewardCommand().equalsIgnoreCase(args[2])) {
                           sender.sendMessage(TextFormat.colorize(prefix + " Command: " + args[2] + " is already added!"));
                           return false;
                        }
                     }

                     Reward newReward = new Reward(RewardType.ConsoleCommand);
                     newReward.setRewardCommand(args[2]);
                     crate.addReward(newReward);
                     this.saveCrates();
                     sender.sendMessage(TextFormat.colorize(prefix + " Command: " + args[2] + " added!"));
                     return false;
                  }
               }

               sender.sendMessage(TextFormat.colorize(prefix + " That crate doesn't exist!"));
               return false;
            }

            if (args[0].equalsIgnoreCase("removecmd")) {
               if (crates.isEmpty()) {
                  sender.sendMessage(TextFormat.colorize(prefix + " There are no crates!"));
                  return false;
               }

               for(Crate crate : crates) {
                  if (crate.getName().toLowerCase().equals(args[1].toLowerCase())) {
                     for(Reward reward : crate.getRewards().keySet()) {
                        if (reward.getType().equals(RewardType.ConsoleCommand) && reward.getRewardCommand().equalsIgnoreCase(args[2])) {
                           ConfigSection section = this.config.getSection("crates." + crate.getName() + ".rewards.commands.console");
                           section.remove(reward.getRewardCommand());
                           this.config.save();
                           sender.sendMessage(TextFormat.colorize(prefix + " Command: " + reward.getRewardCommand() + " removed!"));
                           Iterator<Map.Entry<Reward, Integer>> it = crate.getRewards().entrySet().iterator();

                           while(it.hasNext()) {
                              Map.Entry<Reward, Integer> pair = (Map.Entry)it.next();
                              if (((Reward)pair.getKey()).equals(reward)) {
                                 it.remove();
                              }
                           }

                           return false;
                        }
                     }

                     sender.sendMessage(TextFormat.colorize(prefix + " Command: " + args[2] + " is not added to that crate!"));
                     return false;
                  }
               }

               sender.sendMessage(TextFormat.colorize(prefix + " That crate doesn't exist!"));
               return false;
            }

            if (args[0].equalsIgnoreCase("addplayercmd")) {
               if (crates.isEmpty()) {
                  sender.sendMessage(TextFormat.colorize(prefix + " There are no crates!"));
                  return false;
               }

               for(Crate crate : crates) {
                  if (crate.getName().toLowerCase().equals(args[1].toLowerCase())) {
                     for(Reward reward : crate.getRewards().keySet()) {
                        if (reward.getType().equals(RewardType.PlayerCommand) && reward.getRewardCommand().equalsIgnoreCase(args[2])) {
                           sender.sendMessage(TextFormat.colorize(prefix + " Player command: " + args[2] + " is already added!"));
                           return false;
                        }
                     }

                     Reward newReward = new Reward(RewardType.PlayerCommand);
                     newReward.setRewardCommand(args[2]);
                     crate.addReward(newReward);
                     this.saveCrates();
                     sender.sendMessage(TextFormat.colorize(prefix + " Player command: " + args[2] + " added!"));
                     return false;
                  }
               }

               sender.sendMessage(TextFormat.colorize(prefix + " That crate doesn't exist!"));
               return false;
            }

            if (!args[0].equalsIgnoreCase("removeplayercmd")) {
               this.sendHelp(p);
               return false;
            }

            if (crates.isEmpty()) {
               sender.sendMessage(TextFormat.colorize(prefix + " There are no crates!"));
               return false;
            }

            for(Crate crate : crates) {
               if (crate.getName().toLowerCase().equals(args[1].toLowerCase())) {
                  for(Reward reward : crate.getRewards().keySet()) {
                     if (reward.getType().equals(RewardType.PlayerCommand) && reward.getRewardCommand().equalsIgnoreCase(args[2])) {
                        ConfigSection section = this.config.getSection("crates." + crate.getName() + ".rewards.commands.player");
                        section.remove(reward.getRewardCommand());
                        this.config.save();
                        sender.sendMessage(TextFormat.colorize(prefix + " Player command: " + reward.getRewardCommand() + " removed!"));
                        Iterator<Map.Entry<Reward, Integer>> it = crate.getRewards().entrySet().iterator();

                        while(it.hasNext()) {
                           Map.Entry<Reward, Integer> pair = (Map.Entry)it.next();
                           if (((Reward)pair.getKey()).equals(reward)) {
                              it.remove();
                           }
                        }

                        return false;
                     }
                  }

                  sender.sendMessage(TextFormat.colorize(prefix + " Player command: " + args[2] + " is not added to that crate!"));
                  return false;
               }
            }

            sender.sendMessage(TextFormat.colorize(prefix + " That crate doesn't exist!"));
            return false;
         }

         if (args.length == 4) {
            if (args[0].equalsIgnoreCase("givekey")) {
               Player p2 = this.server.getPlayer(args[1]);
               if (p2 == null) {
                  sender.sendMessage(TextFormat.colorize(prefix + " " + args[1] + " isn't online!"));
                  return false;
               }

               if (crates.isEmpty()) {
                  sender.sendMessage(TextFormat.colorize(prefix + " There are no crates!"));
                  return false;
               }

               for(Crate crate : crates) {
                  if (crate.getName().toLowerCase().equals(args[2].toLowerCase())) {
                     if (crate.isVirtual()) {
                        if (canParse(args[3])) {
                           int amount = Integer.parseInt(args[3]);
                           if (amount < 1) {
                              sender.sendMessage(TextFormat.colorize(prefix + " Invalid number!"));
                              return false;
                           }

                           if (this.keysConfig.exists("keys." + p2.getUniqueId() + "." + crate.getName())) {
                              int value = this.keysConfig.getInt("keys." + p2.getUniqueId() + "." + crate.getName());
                              this.keysConfig.set("keys." + p2.getUniqueId() + "." + crate.getName(), value + amount);
                           } else {
                              this.keysConfig.set("keys." + p2.getUniqueId() + "." + crate.getName(), amount);
                           }

                           this.keysConfig.save();
                           if (amount == 1) {
                              sender.sendMessage(TextFormat.colorize(prefix + " Gave " + crate.getName() + " key to " + p2.getName() + "!"));
                           } else {
                              sender.sendMessage(TextFormat.colorize(prefix + " Gave " + amount + " " + crate.getName() + " keys to " + p2.getName() + "!"));
                           }

                           if (amount == 1) {
                              p2.sendMessage(TextFormat.colorize(prefix + " You've recieved a " + crate.getDisplayName() + " Key!"));
                           } else {
                              p2.sendMessage(TextFormat.colorize(prefix + " You've recieved " + amount + " " + crate.getDisplayName() + " keys!"));
                           }
                        } else {
                           sender.sendMessage(TextFormat.colorize(prefix + " Invalid number!"));
                        }

                        return false;
                     }

                     Inventory inv = p2.getInventory();
                     if (!inv.canAddItem(crate.getKey())) {
                        sender.sendMessage(TextFormat.colorize(prefix + " " + p2.getName() + "'s inventory is full!"));
                     } else if (canParse(args[3])) {
                        int amount = Integer.parseInt(args[3]);
                        if (amount < 1) {
                           sender.sendMessage(TextFormat.colorize(prefix + " Invalid number!"));
                           return false;
                        }

                        if (crate.isVirtual()) {
                           if (this.keysConfig.exists("keys." + p2.getUniqueId())) {
                              int value = this.keysConfig.getInt("keys." + p2.getUniqueId() + "." + crate.getName());
                              this.keysConfig.set("keys." + p2.getUniqueId() + "." + crate.getName(), value + amount);
                           } else {
                              this.keysConfig.set("keys." + p2.getUniqueId() + "." + crate.getName(), amount);
                           }

                           this.keysConfig.save();
                        } else {
                           Item item = crate.getKey();
                           item.setCount(amount);
                           p2.getInventory().addItem(new Item[]{crate.getKey()});
                        }

                        if (amount == 1) {
                           sender.sendMessage(TextFormat.colorize(prefix + " Gave " + crate.getName() + " key to " + p2.getName() + "!"));
                        } else {
                           sender.sendMessage(TextFormat.colorize(prefix + " Gave " + amount + " " + crate.getName() + " keys to " + p2.getName() + "!"));
                        }

                        if (amount == 1) {
                           p2.sendMessage(TextFormat.colorize(prefix + " You've recieved a " + crate.getDisplayName() + " Key!"));
                        } else {
                           p2.sendMessage(TextFormat.colorize(prefix + " You've recieved " + amount + " " + crate.getDisplayName() + " keys!"));
                        }
                     } else {
                        sender.sendMessage(TextFormat.colorize(prefix + " Invalid number!"));
                     }

                     return false;
                  }
               }

               sender.sendMessage(TextFormat.colorize(prefix + " That crate doesn't exist!"));
               return false;
            }

            if (args[0].equalsIgnoreCase("addcmd")) {
               if (crates.isEmpty()) {
                  sender.sendMessage(TextFormat.colorize(prefix + " There are no crates!"));
                  return false;
               }

               for(Crate crate : crates) {
                  if (crate.getName().toLowerCase().equals(args[1].toLowerCase())) {
                     StringBuilder message = new StringBuilder();

                     for(int i = 2; i < args.length; ++i) {
                        message.append(args[i]).append(" ");
                     }

                     for(Reward reward : crate.getRewards().keySet()) {
                        if (reward.getType().equals(RewardType.ConsoleCommand) && reward.getRewardCommand().equalsIgnoreCase(message.toString().substring(0, message.length() - 1))) {
                           sender.sendMessage(TextFormat.colorize(prefix + " Command: " + message.toString().substring(0, message.length() - 1) + " is already added!"));
                           return false;
                        }
                     }

                     Reward newReward = new Reward(RewardType.ConsoleCommand);
                     newReward.setRewardCommand(message.toString().substring(0, message.length() - 1));
                     crate.addReward(newReward);
                     this.saveCrates();
                     sender.sendMessage(TextFormat.colorize(prefix + " Command: " + message.toString().substring(0, message.length() - 1) + " added!"));
                     return false;
                  }
               }

               sender.sendMessage(TextFormat.colorize(prefix + " That crate doesn't exist!"));
               return false;
            }

            if (args[0].equalsIgnoreCase("removecmd")) {
               if (crates.isEmpty()) {
                  sender.sendMessage(TextFormat.colorize(prefix + " There are no crates!"));
                  return false;
               }

               for(Crate crate : crates) {
                  if (crate.getName().toLowerCase().equals(args[1].toLowerCase())) {
                     StringBuilder message = new StringBuilder();

                     for(int i = 2; i < args.length; ++i) {
                        message.append(args[i]).append(" ");
                     }

                     for(Reward reward : crate.getRewards().keySet()) {
                        if (reward.getType().equals(RewardType.ConsoleCommand) && reward.getRewardCommand().equalsIgnoreCase(message.toString().substring(0, message.length() - 1))) {
                           ConfigSection section = this.config.getSection("crates." + crate.getName() + ".rewards.commands.console");
                           section.remove(reward.getRewardCommand());
                           this.config.save();
                           sender.sendMessage(TextFormat.colorize(prefix + " Command: " + reward.getRewardCommand() + " removed!"));
                           Iterator<Map.Entry<Reward, Integer>> it = crate.getRewards().entrySet().iterator();

                           while(it.hasNext()) {
                              Map.Entry<Reward, Integer> pair = (Map.Entry)it.next();
                              if (((Reward)pair.getKey()).equals(reward)) {
                                 it.remove();
                              }
                           }

                           return false;
                        }
                     }

                     sender.sendMessage(TextFormat.colorize(prefix + " Command: " + message.toString().substring(0, message.length() - 1) + " is not added to that crate!"));
                     return false;
                  }
               }

               sender.sendMessage(TextFormat.colorize(prefix + " That crate doesn't exist!"));
               return false;
            }

            if (args[0].equalsIgnoreCase("addplayercmd")) {
               if (crates.isEmpty()) {
                  sender.sendMessage(TextFormat.colorize(prefix + " There are no crates!"));
                  return false;
               }

               for(Crate crate : crates) {
                  if (crate.getName().toLowerCase().equals(args[1].toLowerCase())) {
                     StringBuilder message = new StringBuilder();

                     for(int i = 2; i < args.length; ++i) {
                        message.append(args[i]).append(" ");
                     }

                     for(Reward reward : crate.getRewards().keySet()) {
                        if (reward.getType().equals(RewardType.PlayerCommand) && reward.getRewardCommand().equalsIgnoreCase(message.toString().substring(0, message.length() - 1))) {
                           sender.sendMessage(TextFormat.colorize(prefix + " Player command: " + message.toString().substring(0, message.length() - 1) + " is already added!"));
                           return false;
                        }
                     }

                     Reward newReward = new Reward(RewardType.PlayerCommand);
                     newReward.setRewardCommand(message.toString().substring(0, message.length() - 1));
                     crate.addReward(newReward);
                     this.saveCrates();
                     sender.sendMessage(TextFormat.colorize(prefix + " Player command: " + message.toString().substring(0, message.length() - 1) + " added!"));
                     return false;
                  }
               }

               sender.sendMessage(TextFormat.colorize(prefix + " That crate doesn't exist!"));
               return false;
            }

            if (!args[0].equalsIgnoreCase("removeplayercmd")) {
               this.sendHelp(p);
               return false;
            }

            if (crates.isEmpty()) {
               sender.sendMessage(TextFormat.colorize(prefix + " There are no crates!"));
               return false;
            }

            for(Crate crate : crates) {
               if (crate.getName().toLowerCase().equals(args[1].toLowerCase())) {
                  StringBuilder message = new StringBuilder();

                  for(int i = 2; i < args.length; ++i) {
                     message.append(args[i]).append(" ");
                  }

                  for(Reward reward : crate.getRewards().keySet()) {
                     if (reward.getType().equals(RewardType.PlayerCommand) && reward.getRewardCommand().equalsIgnoreCase(message.toString().substring(0, message.length() - 1))) {
                        ConfigSection section = this.config.getSection("crates." + crate.getName() + ".rewards.commands.player");
                        section.remove(reward.getRewardCommand());
                        this.config.save();
                        sender.sendMessage(TextFormat.colorize(prefix + " Player command: " + reward.getRewardCommand() + " removed!"));
                        Iterator<Map.Entry<Reward, Integer>> it = crate.getRewards().entrySet().iterator();

                        while(it.hasNext()) {
                           Map.Entry<Reward, Integer> pair = (Map.Entry)it.next();
                           if (((Reward)pair.getKey()).equals(reward)) {
                              it.remove();
                           }
                        }

                        return false;
                     }
                  }

                  sender.sendMessage(TextFormat.colorize(prefix + " Player command: " + message.toString().substring(0, message.length() - 1) + " is not added to that crate!"));
                  return false;
               }
            }

            sender.sendMessage(TextFormat.colorize(prefix + " That crate doesn't exist!"));
            return false;
         }

         if (args.length > 4) {
            if (args[0].equalsIgnoreCase("addcmd")) {
               if (crates.isEmpty()) {
                  sender.sendMessage(TextFormat.colorize(prefix + " There are no crates!"));
                  return false;
               }

               for(Crate crate : crates) {
                  if (crate.getName().toLowerCase().equals(args[1].toLowerCase())) {
                     StringBuilder message = new StringBuilder();

                     for(int i = 2; i < args.length; ++i) {
                        message.append(args[i]).append(" ");
                     }

                     for(Reward reward : crate.getRewards().keySet()) {
                        if (reward.getType().equals(RewardType.ConsoleCommand) && reward.getRewardCommand().equalsIgnoreCase(message.toString().substring(0, message.length() - 1))) {
                           sender.sendMessage(TextFormat.colorize(prefix + " Command: " + message.toString().substring(0, message.length() - 1) + " is already added!"));
                           return false;
                        }
                     }

                     Reward newReward = new Reward(RewardType.ConsoleCommand);
                     newReward.setRewardCommand(message.toString().substring(0, message.length() - 1));
                     crate.addReward(newReward);
                     this.saveCrates();
                     sender.sendMessage(TextFormat.colorize(prefix + " Command: " + message.toString().substring(0, message.length() - 1) + " added!"));
                     return false;
                  }
               }

               sender.sendMessage(TextFormat.colorize(prefix + " That crate doesn't exist!"));
               return false;
            }

            if (args[0].equalsIgnoreCase("removecmd")) {
               if (crates.isEmpty()) {
                  sender.sendMessage(TextFormat.colorize(prefix + " There are no crates!"));
                  return false;
               }

               for(Crate crate : crates) {
                  if (crate.getName().toLowerCase().equals(args[1].toLowerCase())) {
                     StringBuilder message = new StringBuilder();

                     for(int i = 2; i < args.length; ++i) {
                        message.append(args[i]).append(" ");
                     }

                     for(Reward reward : crate.getRewards().keySet()) {
                        if (reward.getType().equals(RewardType.ConsoleCommand) && reward.getRewardCommand().equalsIgnoreCase(message.toString().substring(0, message.length() - 1))) {
                           ConfigSection section = this.config.getSection("crates." + crate.getName() + ".rewards.commands.console");
                           section.remove(reward.getRewardCommand());
                           this.config.save();
                           sender.sendMessage(TextFormat.colorize(prefix + " Command: " + reward.getRewardCommand() + " removed!"));
                           Iterator<Map.Entry<Reward, Integer>> it = crate.getRewards().entrySet().iterator();

                           while(it.hasNext()) {
                              Map.Entry<Reward, Integer> pair = (Map.Entry)it.next();
                              if (((Reward)pair.getKey()).equals(reward)) {
                                 it.remove();
                              }
                           }

                           return false;
                        }
                     }

                     sender.sendMessage(TextFormat.colorize(prefix + " Command: " + message.toString().substring(0, message.length() - 1) + " is not added to that crate!"));
                     return false;
                  }
               }

               sender.sendMessage(TextFormat.colorize(prefix + " That crate doesn't exist!"));
               return false;
            }

            if (args[0].equalsIgnoreCase("addplayercmd")) {
               if (crates.isEmpty()) {
                  sender.sendMessage(TextFormat.colorize(prefix + " There are no crates!"));
                  return false;
               }

               for(Crate crate : crates) {
                  if (crate.getName().toLowerCase().equals(args[1].toLowerCase())) {
                     StringBuilder message = new StringBuilder();

                     for(int i = 2; i < args.length; ++i) {
                        message.append(args[i]).append(" ");
                     }

                     for(Reward reward : crate.getRewards().keySet()) {
                        if (reward.getType().equals(RewardType.PlayerCommand) && reward.getRewardCommand().equalsIgnoreCase(message.toString().substring(0, message.length() - 1))) {
                           sender.sendMessage(TextFormat.colorize(prefix + " Player command: " + message.toString().substring(0, message.length() - 1) + " is already added!"));
                           return false;
                        }
                     }

                     Reward newReward = new Reward(RewardType.PlayerCommand);
                     newReward.setRewardCommand(message.toString().substring(0, message.length() - 1));
                     crate.addReward(newReward);
                     this.saveCrates();
                     sender.sendMessage(TextFormat.colorize(prefix + " Player command: " + message.toString().substring(0, message.length() - 1) + " added!"));
                     return false;
                  }
               }

               sender.sendMessage(TextFormat.colorize(prefix + " That crate doesn't exist!"));
               return false;
            }

            if (!args[0].equalsIgnoreCase("removeplayercmd")) {
               this.sendHelp(p);
               return false;
            }

            if (crates.isEmpty()) {
               sender.sendMessage(TextFormat.colorize(prefix + " There are no crates!"));
               return false;
            }

            for(Crate crate : crates) {
               if (crate.getName().toLowerCase().equals(args[1].toLowerCase())) {
                  StringBuilder message = new StringBuilder();

                  for(int i = 2; i < args.length; ++i) {
                     message.append(args[i]).append(" ");
                  }

                  for(Reward reward : crate.getRewards().keySet()) {
                     if (reward.getType().equals(RewardType.PlayerCommand) && reward.getRewardCommand().equalsIgnoreCase(message.toString().substring(0, message.length() - 1))) {
                        ConfigSection section = this.config.getSection("crates." + crate.getName() + ".rewards.commands.player");
                        section.remove(reward.getRewardCommand());
                        this.config.save();
                        sender.sendMessage(TextFormat.colorize(prefix + " Player command: " + reward.getRewardCommand() + " removed!"));
                        Iterator<Map.Entry<Reward, Integer>> it = crate.getRewards().entrySet().iterator();

                        while(it.hasNext()) {
                           Map.Entry<Reward, Integer> pair = (Map.Entry)it.next();
                           if (((Reward)pair.getKey()).equals(reward)) {
                              it.remove();
                           }
                        }

                        return false;
                     }
                  }

                  sender.sendMessage(TextFormat.colorize(prefix + " Player command: " + message.toString().substring(0, message.length() - 1) + " is not added to that crate!"));
                  return false;
               }
            }

            sender.sendMessage(TextFormat.colorize(prefix + " That crate doesn't exist!"));
            return false;
         }
      } else if (command.getName().equalsIgnoreCase("keys")) {
         if (!(sender instanceof Player)) {
            sender.sendMessage(TextFormat.colorize(prefix + " You must be ingame to run this command!"));
            return false;
         }

         Player p = (Player)sender;
         if (crates.isEmpty()) {
            p.sendMessage(TextFormat.colorize(prefix + " There are no crates!"));
         } else {
            p.sendMessage(TextFormat.colorize("&e&lList of Keys:"));

            for(Crate crate : crates) {
               if (crate.isVirtual()) {
                  int value = this.keysConfig.getInt("keys." + p.getUniqueId() + "." + crate.getName());
                  p.sendMessage(TextFormat.colorize("- " + crate.getDisplayName() + "&r&f: " + value));
               }
            }
         }
      }

      return false;
   }
}
