package cat.math.shopsigns.material;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.block.Sign;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import com.earth2me.essentials.User;
import cat.math.shopsigns.ShopSignOwner;
import cat.math.shopsigns.ShopSigns;
import cat.math.shopsigns.Util;

public class ShopSign {
	
	Sign sign;
	ShopSigns plugin;
	
	// 0 is a buy sign, 1 is a sell sign, and 2 is both a buy and a sell sign
	int type;
	
	double buy_price = -1;
	double sell_price = -1;
	int quantity = -1;
	Material item = null;
	ShopSignOwner sso;
	String[] lines;
	
	boolean isConnected = false;
	ShopChest sc = null;
	BossBar bb = null;
	
	public ShopSign(ShopSigns plugin, Sign sign, int type, ShopSignOwner sso, String[] lines) {
		
		this.plugin = plugin;
		this.sign = sign;
		this.type = type;
		this.sso = sso;
		this.lines = lines;
	}
	
	public ShopSign(ShopSigns plugin, FileConfiguration file) {
		
		buy_price = file.getDouble("buy-price");
		sell_price = file.getDouble("sell-price");
		quantity = file.getInt("quantity");
		String i = file.getString("item");
		
		if(i.equals("null")) item = null;
		else item = Material.getMaterial(i);
		
		isConnected = file.getBoolean("connected");
		
		World world = Bukkit.getServer().getWorld(file.getString("world"));
		Location location = new Location(world, file.getInt("x"), file.getInt("y"), file.getInt("z"));
		sign = (Sign)location.getBlock().getState();
		
		this.plugin = plugin;
		
		String uuid = file.getString("owner");
		OfflinePlayer player = Bukkit.getOfflinePlayer(UUID.fromString(uuid));
		sso = new ShopSignOwner(plugin, player);
		
		if(isConnected) {
			
			World chestworld = Bukkit.getServer().getWorld(file.getString("chest-location.world"));
			int x = file.getInt("chest-location.x");
			int y = file.getInt("chest-location.y");
			int z = file.getInt("chest-location.z");
			location = new Location(chestworld, x, y, z);
			sc = new ShopChest(location, this);
			
			BarColor color;
			if(type == 0) color = BarColor.RED;
			else if(type == 1) color = BarColor.GREEN;
			else color = BarColor.YELLOW;
			
			bb = Bukkit.getServer().createBossBar(item.toString().replace("_", " "), color, BarStyle.SEGMENTED_10, BarFlag.CREATE_FOG);
			bb.removeFlag(BarFlag.CREATE_FOG);
			bb.setVisible(true);
			
			bb.setProgress((double)sc.getNumberOfItems()/(double)sc.getTotalSpace());
		}
	}
	
	public void register() {
		
		// Save the shop sign's information into a local data folder specific to the shop sign's owner
		// This file will store the shop sign's location, and all the other information required to
		// identify this particular shop and what to do with it when
		Location location = sign.getLocation();
		World world = sign.getWorld();
		String worldname = world.getName();
		String shopfilename = worldname+"-"+location.getBlockX()+"-"+location.getBlockY()+"-"+location.getBlockZ()+".yml";
		File f = new File(sso.getDataFolder(), shopfilename);
		
		try {
			f.createNewFile();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		FileConfiguration shop = YamlConfiguration.loadConfiguration(f);
		shop.set("world",worldname);
		shop.set("x", location.getBlockX());
		shop.set("y", location.getBlockY());
		shop.set("z", location.getBlockZ());
		shop.set("owner", sso.getPlayer().getUniqueId().toString());
		shop.set("type", type);
		shop.set("buy-price", buy_price);
		shop.set("sell-price", sell_price);
		shop.set("quantity", quantity);
		if(item != null) shop.set("item", item.toString());
		else shop.set("item", "null");
		shop.set("connected", false);
		
		try {
			shop.save(f);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// Send the player a confirm message notifying them they correctly made a shop sign
		String s = plugin.getConfig().getString("confirms.make-shopsign");
		s = s.replace("%coords%", location.getBlockX()+","+location.getBlockY()+","+location.getBlockZ());
		sso.getPlayer().getPlayer().sendMessage(Util.color(s));
	}
	
	public void buy(User buyer) {
		
		if(!isConnected) {
			
			String s = plugin.getConfig().getString("errors.shopsign-not-connected", "&cThis shop sign is not connected to a chest.");
			s = Util.color(s);
			buyer.getBase().sendMessage(s);
			return;
		}
		
		// You can't buy from your own shop, and also if this shop is not a buy sign
		if(buyer.getBase().getUniqueId().toString().equals(sso.getPlayer().getUniqueId().toString()) || buy_price == -1) {
			
			String s = plugin.getConfig().getString("errors.sign-no-buy","&cYou may not buy from that shop.");
			s = Util.color(s);
			buyer.getBase().sendMessage(s);
			return;
		}
		
		// You can't buy from this shop if you don't have enough money
		if(!buyer.canAfford(new BigDecimal(buy_price))) {
			
			String s = plugin.getConfig().getString("errors.buy-no-money", "&cYou do not have enough money.");
			s = Util.color(s);
			buyer.getBase().sendMessage(s);
			return;
		}
		
		Inventory inv = sc.getInventory();
		
		// If the chest is empty, tell the buyer as such and send the owner a mail if this option is enabled
		if(!inv.contains(item, quantity)) {
			
			String s = plugin.getConfig().getString("errors.buy-no-inventory-chest", "&cThis shop is empty!");
			s = Util.color(s);
			buyer.getBase().sendMessage(s);
			boolean b = plugin.getConfig().getBoolean("feedback.mail-owner-shop-empty");
			if(b) {
				String f = plugin.getConfig().getString("feedback.mail-message-owner-shop-empty", 
						"&c[ShopSigns] &eYour chest at &c%ccoords% &elinked to the shop at &c%scoords% &eis empty.");
				f = f.replace("%ccoords%", sc.getLocation().getBlockX()+","+sc.getLocation().getBlockY()+","+sc.getLocation().getBlockZ());
				f = f.replace("%scoords%", sign.getX()+","+sign.getY()+","+sign.getZ());
				f = Util.color(f);
				sso.sendFeedback(f);
			}
			return;
		}
		
		// Get the amount of space in the player's inventory
		Inventory pinv = buyer.getBase().getInventory();
		ItemStack[] contents = pinv.getContents();
		int space = 0;
		
		for(ItemStack is : contents) {
			if(is == null) {
				space += item.getMaxStackSize();
				continue;
			}
			if(is.getType().equals(item)) {
				space += item.getMaxStackSize()-is.getAmount();
				continue;
			}
		}
		
		// If there's not a enough space in the player's inventory, do not allow them to buy from this shop
		if(space < quantity) {
			
			String s = plugin.getConfig().getString("errors.buy-no-inventory", "&cYour inventory is full.");
			s = Util.color(s);
			buyer.getBase().sendMessage(s);
			return;
		}
		
		User owner = plugin.getEssentials().getUser(UUID.fromString(sso.getPlayer().getUniqueId().toString()));
		
		try {
			owner.giveMoney(new BigDecimal(buy_price));
		} catch (Exception e) {
			String s = plugin.getConfig().getString("errors.too-much-money", "&cThat transaction could not go through because one party has too much money.");
			s = Util.color(s);
			buyer.getBase().sendMessage(s);
			return;
		}
		
		// Removes the items from the buy chest and places them in the player's inventory and gives confirm messages to both parties
		sc.removeFromChest(quantity);
		buyer.takeMoney(new BigDecimal(buy_price));
		inv = buyer.getBase().getInventory();
		
		ArrayList<ItemStack> additems = new ArrayList<ItemStack>();
		int amount = quantity;
		
		while(amount > item.getMaxStackSize()) {
			ItemStack is = new ItemStack(item, item.getMaxStackSize());
			amount = amount - item.getMaxStackSize();
			additems.add(is);
		}
		
		if(amount > 0) {
			ItemStack is = new ItemStack(item, amount);
			additems.add(is);
		}
		
		ItemStack[] items = new ItemStack[additems.size()];
		
		for(int i=0; i<additems.size(); i++) {
			items[i] = additems.get(i);
		}
		
		inv.addItem(items);
		
		String s = plugin.getConfig().getString("confirms.buy-success", "&aYou bought &e%quantity% %item%&a.");
		s = s.replace("%quantity%", ""+quantity);
		s = s.replace("%item%", item.toString());
		s = Util.color(s);
		buyer.getBase().sendMessage(s);
		
		if(owner.getBase().isOnline()) {
			
			s = plugin.getConfig().getString("feedback.shop-bought-from", "&e%user% &abought &e%quantity% %item%&a from your shop at &e%scoords%&a.");
			s = s.replace("%user%", buyer.getName());
			s = s.replace("%quantity%", ""+quantity);
			s = s.replace("%item%", item.toString());
			s = s.replace("%scoords%", sign.getX()+","+sign.getY()+","+sign.getZ());
			s = Util.color(s);
			owner.getBase().sendMessage(s);
		}
	}
	
	public void sell(User seller) {
		
		if(!isConnected) {
			
			String s = plugin.getConfig().getString("errors.shopsign-not-connected", "&cThis shop sign is not connected to a chest.");
			s = Util.color(s);
			seller.getBase().sendMessage(s);
			return;
		}
		
		// You can't sell to your own shop and also if this sign is not a sell sign
		if(seller.getBase().getUniqueId().toString().equals(sso.getPlayer().getUniqueId().toString()) || sell_price == -1) {
			
			String s = plugin.getConfig().getString("errors.sign-no-sell","&cYou may not sell to that shop.");
			s = Util.color(s);
			seller.getBase().getPlayer().sendMessage(s);
			return;
		}
		
		User owner = plugin.getEssentials().getUser(sso.getPlayer().getName());
		
		// If the owner does not have enough money, then don't allow the seller to sell to this shop
		if(!owner.canAfford(new BigDecimal(sell_price))) {
			
			String s = plugin.getConfig().getString("errors.sell-no-money", "&cThe shop owner does not have enough money.");
			s = Util.color(s);
			seller.getBase().sendMessage(s);
			return;
		}
		
		int space = sc.getSpace();
		
		// If there is not enough space in the shop chest's inventory, tell the seller as such and send a mail to the owner if this option is enabled
		if(space < quantity) {
			
			String s = plugin.getConfig().getString("errors.sell-no-inventory-chest", "&cThis shop is full!");
			s = Util.color(s);
			seller.getBase().sendMessage(s);
			boolean b = plugin.getConfig().getBoolean("feedback.mail-owner-shop-full");
			if(b) {
				String f = plugin.getConfig().getString("feedback.mail-message-owner-shop-full", 
						"&c[ShopSigns] &eYour chest at &c%ccoords% &elinked to the shop at &c%scoords% &eis full.");
				f = f.replace("%ccoords%", sc.getLocation().getBlockX()+","+sc.getLocation().getBlockY()+","+sc.getLocation().getBlockZ());
				f = f.replace("%scoords%", sign.getX()+","+sign.getY()+","+sign.getZ());
				f = Util.color(f);
				sso.sendFeedback(f);
			}
			return;
		}
		
		Inventory inv = seller.getBase().getInventory();
		
		// If the seller doesn't have the items that would be sold, do not allow them to sell to this shop
		if(!inv.contains(item, quantity)) {
			
			String s = plugin.getConfig().getString("errors.sell-no-inventory", "&cYou do not have enough of the item being sold.");
			s = Util.color(s);
			seller.getBase().sendMessage(s);
			return;
		}
		
		try {
			seller.giveMoney(new BigDecimal(sell_price));
		} catch (Exception e) {
			String s = plugin.getConfig().getString("errors.too-much-money", "&cThat transaction could not go through because one party has too much money.");
			s = Util.color(s);
			seller.getBase().sendMessage(s);
			return;
		}
		
		// Takes the items from the seller's inventory and places them into the chest's inventory and also do the work for the money transaction
		sc.addToChest(quantity);
		owner.takeMoney(new BigDecimal(sell_price));
		inv = seller.getBase().getInventory();
		
		ArrayList<ItemStack> additems = new ArrayList<ItemStack>();
		int amount = quantity;
		
		while(amount > item.getMaxStackSize()) {
			ItemStack is = new ItemStack(item, item.getMaxStackSize());
			amount = amount - item.getMaxStackSize();
			additems.add(is);
		}
		
		if(amount > 0) {
			ItemStack is = new ItemStack(item, amount);
			additems.add(is);
		}
		
		ItemStack[] items = new ItemStack[additems.size()];
		
		for(int i=0; i<additems.size(); i++) {
			items[i] = additems.get(i);
		}
		
		inv.removeItem(items);
		
		String s = plugin.getConfig().getString("confirms.sell-success", "&aYou sold &e%quantity% %item%&a.");
		s = s.replace("%quantity%", ""+quantity);
		s = s.replace("%item%", item.toString());
		s = Util.color(s);
		seller.getBase().sendMessage(s);
		
		if(owner.getBase().isOnline()) {
			
			s = plugin.getConfig().getString("feedback.shop-sell-to", "&e%user% &asold &e%quantity% %item%&a to your shop at &e%scoords%&a.");
			s = s.replace("%user%", seller.getName());
			s = s.replace("%quantity%", ""+quantity);
			s = s.replace("%item%", item.toString());
			s = s.replace("%scoords%", sign.getX()+","+sign.getY()+","+sign.getZ());
			s = Util.color(s);
			owner.getBase().sendMessage(s);
		}
	}
	
	public void remove(Player player) {
		
		File f = new File(sso.getDataFolder(), getFileName());
		
		try {
			f.delete();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		String s = plugin.getConfig().getString("confirms.shopsign-destroyed", 
				"&cYou have unregistered the ShopSign located at &e%scoords%&c.");
		s = s.replace("%scoords%", sign.getX()+","+sign.getY()+","+sign.getZ());
		s = Util.color(s);
		player.sendMessage(s);
	}
	
	public void disconnectChest(Player player) {
		
		if(!isConnected) return;
		FileConfiguration shop = getFile();
		shop.set("chest-location", null);
		shop.set("connected", false);
		File f = new File(sso.getDataFolder(), getFileName());
		
		try {			
			shop.save(f);			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		isConnected = false;
		
		String s = plugin.getConfig().getString("confirms.shopchest-destroyed",
				"&cYou have disconnected the chest associated with the ShopSign at &e%scoords%&c.");
		s = s.replace("%scoords%", sign.getX()+","+sign.getY()+","+sign.getZ());
		s = Util.color(s);
		player.sendMessage(s);
	}
	
	public void connectToChest(Location location) {
		
		Block b = location.getBlock();
		
		switch(b.getType()) {
		case CHEST:
		case TRAPPED_CHEST:
		case BARREL:
		case SHULKER_BOX:
		case BLACK_SHULKER_BOX:
		case RED_SHULKER_BOX:
		case GREEN_SHULKER_BOX:
		case BROWN_SHULKER_BOX:
		case BLUE_SHULKER_BOX:
		case PURPLE_SHULKER_BOX:
		case CYAN_SHULKER_BOX:
		case LIGHT_GRAY_SHULKER_BOX:
		case GRAY_SHULKER_BOX:
		case PINK_SHULKER_BOX:
		case LIME_SHULKER_BOX:
		case YELLOW_SHULKER_BOX:
		case LIGHT_BLUE_SHULKER_BOX:
		case MAGENTA_SHULKER_BOX:
		case ORANGE_SHULKER_BOX:
		case WHITE_SHULKER_BOX:
			break;
		default:
			return;
		}
		
		FileConfiguration shop = getFile();
		
		if(item == null) {
			 
			 Container c = (Container)b;
			 
			 Inventory inv = c.getInventory();
			 boolean isEmpty = true;
			 ItemStack[] contents = inv.getContents();
			 
			 for(ItemStack is : contents) {
				 if(is == null) continue;
				 else {
					 isEmpty = false;
					 item = is.getType();
					 shop.set("item", item.toString());
					 break;
				 }
			 }
			 
			 if(isEmpty) {
				 
				 String s = plugin.getConfig().getString("errors.ambiguous-item", "&cNo item was specified, so you could not link the chest to the shop sign.");
				 s = Util.color(s);
				 sso.getPlayer().getPlayer().sendMessage(s);
				 return;
			 }
			 
		}
		
		if(quantity == -1) {
			 
			 if(!(b instanceof Container)) return;
			 Container c = (Container)b;
			 
			 Inventory inv = c.getInventory();
			 boolean isEmpty = true;
			 boolean isCounting = false;
			 int count = 0;
			 ItemStack[] contents = inv.getContents();
			 
			 for(ItemStack is : contents) {
				 if(is == null) continue;
				 else if(!is.getType().equals(item) && !isCounting) continue;
				 else if(is.getType().equals(item)) {
					 isCounting = true;
					 count += is.getAmount();
				 }
				 else break;
			 }
			 
			 if(isEmpty) {
				 
				 String s = plugin.getConfig().getString("errors.ambiguous-quantity", "&cNo quantity was specified, so you could not link the chest to the shop sign.");
				 s = Util.color(s);
				 sso.getPlayer().getPlayer().sendMessage(s);
				 return;
			 }
			 
			 quantity = count;
		}
		
		shop.set("connected", true);
		shop.set("chest-location.world", location.getWorld().getName());
		shop.set("chest-location.x", location.getBlockX());
		shop.set("chest-location.y", location.getBlockY());
		shop.set("chest-location.z", location.getBlockZ());
		
		try {
			shop.save(new File(sso.getDataFolder(), getFileName()));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		isConnected = true;
		sc = new ShopChest(location, this);
		
		BarColor color;
		if(type == 0) color = BarColor.RED;
		else if(type == 1) color = BarColor.GREEN;
		else color = BarColor.YELLOW;
		
		bb = Bukkit.getServer().createBossBar(item.toString().replace("_", " "), color, BarStyle.SEGMENTED_10, BarFlag.CREATE_FOG);
		bb.removeFlag(BarFlag.CREATE_FOG);
		bb.setVisible(true);
		
		bb.setProgress((double)sc.getNumberOfItems()/(double)sc.getTotalSpace());
		
		String message = plugin.getConfig().getString("confirms.connect-chest");
		message = message.replace("%scoords%", sign.getBlock().getX()+","+sign.getBlock().getY()+","+sign.getBlock().getZ());
		message = message.replace("%ccoords%", location.getBlockX()+","+location.getBlockY()+","+location.getBlockZ());
		sso.getPlayer().getPlayer().sendMessage(Util.color(message));
	}
	
	public void showProgressBar(Player player) {
		
		bb.addPlayer(player);
	}
	
	public void hideProgressBar(Player player) {
		
		bb.removePlayer(player);
	}
	
	public void setQuantity(int i) {quantity = i;}
	public void setItem(Material m) {item = m;}
	public void setBuyPrice(double p) {buy_price = p;}
	public void setSellPrice(double p) {sell_price = p;}
	
	public int getType() {return type;}
	public int getQuantity() {return quantity;}
	public Material getItem() {return item;}
	public double getBuyPrice() {return buy_price;}
	public double getSellPrice() {return sell_price;}
	public Sign getSign() {return sign;}
	public ShopSignOwner getOwner() {return sso;}
	public boolean isConnected() {return isConnected;}
	public ShopChest getConnectedChest() {return sc;}
	public BossBar getProgressBar() {return bb;}
	
	public FileConfiguration getFile() {
		
		Location location = sign.getLocation();
		World world = sign.getWorld();
		String worldname = world.getName();
		String shopfilename = worldname+"-"+location.getBlockX()+"-"+location.getBlockY()+"-"+location.getBlockZ()+".yml";
		FileConfiguration shop = YamlConfiguration.loadConfiguration(new File(sso.getDataFolder(),shopfilename));
		return shop;
	}
	
	public String getFileName() {
		
		Location location = sign.getLocation();
		World world = sign.getWorld();
		String worldname = world.getName();
		String shopfilename = worldname+"-"+location.getBlockX()+"-"+location.getBlockY()+"-"+location.getBlockZ()+".yml";
		return shopfilename;
	}
}