package cat.math.shopsigns.commands;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;

import cat.math.shopsigns.ShopSigns;
import cat.math.shopsigns.Util;
import cat.math.shopsigns.material.ShopSign;

public class Find implements TabExecutor {

	ShopSigns plugin;
	
	public Find(ShopSigns plugin) {
		this.plugin = plugin;
	}
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		
		ArrayList<String> list = new ArrayList<>();
		
		if(args.length == 1) {
			
			list.add("buy");
			list.add("sell");
			return list;
		}
		
		if(args.length == 2 && args[1].contentEquals("")) {
			
			Material[] mats = Material.values();
			for(int i=0; i<10; i++) {
				list.add(mats[i].toString());
			}
			return list;
		}
		
		if(args.length == 2 && !args[1].contentEquals("")) {
			
			Material[] mats = Material.values();
			String mat = args[1];
			int len = mat.length();
			for(Material m : mats) {
				
				if(m.toString().length() < len) continue;
				
				if(mat.equalsIgnoreCase(m.toString().substring(0, len))) {
					
					list.add(m.toString());
					if(list.size() == 10) return list;
				}
			}
			
			return list;
		}
		
		if(args.length == 3) {
			
			list.add("<player> or page #");
			return list;
		}
		
		if(args.length == 4) {
			
			list.add("page #");
			return list;
		}
		
		list.add("Too many arguments!");
		return list;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		
		if(!(sender instanceof Player)) {
			
			sender.sendMessage(Util.color("&cOnly players may use this command!"));
			return true;
		}
		
		Player player = (Player) sender;
		
		if(args.length < 2 || args.length > 4) {
			
			displayUsage(player);
			return true;
		}
		
		if(!args[0].equalsIgnoreCase("buy") && !args[0].equalsIgnoreCase("sell")) {
			
			displayUsage(player);
			return true;
		}
		
		if(Material.getMaterial(args[1].toUpperCase()) == null) {
			
			player.sendMessage(Util.color("&cMaterial not recognized."));
			return true;
		}
		
		if(args.length == 3 && Util.stringToInt(args[2]) == -1 && Util.searchForPlayer(args[2]) == null) {
			
			player.sendMessage(Util.color("&cPlayer not found."));
			return true;
		}
		
		ArrayList<FileConfiguration> shops = new ArrayList<>();
		File users = new File(plugin.getDataFolder(), "Users");
		Material item = Material.getMaterial(args[1].toUpperCase());
		int page = 1;
		OfflinePlayer p = null;
		boolean buying;
		
		if(args[0].equalsIgnoreCase("buy")) buying = true;
		else buying = false;
		
		if(args.length == 3 && Util.stringToInt(args[2]) == -1) {
			
			p = Util.searchForPlayer(args[2]);
			shops = getShops(player, item, buying, new File(users, p.getUniqueId().toString()));
			displayShops(player, shops, 1);
			return true;
		}
		
		if(args.length == 3 && p == null) {
			
			page = Util.stringToInt(args[2]);
		}
		
		if(args.length == 4) {
			
			page = Util.stringToInt(args[3]);
			p = Util.searchForPlayer(args[2]);
			shops = getShops(player, item, buying, new File(users, p.getUniqueId().toString()));
			displayShops(player, shops, page);
			return true;
		}
		
		if(page <= 0) page = 1;
		
		for(File f : users.listFiles()) {
			
			shops.addAll(getShops(player, item, buying, f));
		}
		
		displayShops(player, shops, page);
		return true;
	}
	
	private void displayUsage(Player player) {
		
		player.sendMessage(Util.color("/find [buy|sell] [item] <player>"));
	}
	
	private void displayShops(Player recieving, ArrayList<FileConfiguration> shops, int page) {
		
		ArrayList<FileConfiguration> sorted = sortByDistance(recieving, shops);
		int totalpages = (int)Math.ceil(sorted.size()/10D);
		if(totalpages == 0) totalpages = 1;
		if(page > totalpages) page = totalpages;
		StringBuilder message = new StringBuilder();
		
		message.append("&6|----|Page("+page+"/"+totalpages+")|----|\n");
		
		for(int i=10*(page-1); i<10*page; i++) {
			
			if(i >= sorted.size()) break;
			FileConfiguration shop = sorted.get(i);
			message.append(formatShopInfo(recieving, shop));
		}
		
		recieving.sendMessage(Util.color(new String(message)));
	}
	
	private String formatShopInfo(Player recieving, FileConfiguration shop) {
		
		StringBuilder info = new StringBuilder();
		int x = shop.getInt("x");
		int y = shop.getInt("y");
		int z = shop.getInt("z");
		Location loc = new Location(Bukkit.getWorld(shop.getString("world")),x,y,z);
		double distance = loc.distance(recieving.getLocation());
		double df = (int)(100 * distance)/100D;
		double buy_price = shop.getDouble("buy-price");
		double sell_price = shop.getDouble("sell-price");
		String owner = shop.getString("owner");
		owner = Bukkit.getOfflinePlayer(UUID.fromString(owner)).getName();
		
		if(buy_price != -1)
			info.append("&o&6Buy Price: &r&b")
				.append(buy_price)
				.append(" ");
		
		if(sell_price != -1)
			info.append("&o&6Sell Price: &r&b")
				.append(sell_price)
				.append(" ");
		
		info.append("&o&6Amount: &r&b")
			.append(shop.getInt("quantity"))
			.append(" ")
			
			.append("&o&6Location: &r&b")
			.append(x)
			.append(", ")
			.append(y)
			.append(", ")
			.append(z)
			.append(" ")
			
			.append("&o&6Owner: &r&b")
			.append(owner)
			.append(" ")
			
			.append("&o&6Distance: &r&b")
			.append(df)
			.append(" ")
			
			.append("\n");
		
		return new String(info);
	}
	
	private ArrayList<FileConfiguration> getShops(Player player, Material item, boolean buying, File shopowner) {
		
		ArrayList<FileConfiguration> shops = new ArrayList<>();
		File[] list = shopowner.listFiles();
		
		for(File f : list) {
			
			FileConfiguration shop = YamlConfiguration.loadConfiguration(f);
			
			if(buying && shop.getInt("buy-price") == -1) continue;
			if(!buying && shop.getInt("sell-price") == -1) continue;		
			if(shop.getItemStack("item").getType() != item) continue;
			if(!Bukkit.getWorld(shop.getString("world")).equals(player.getLocation().getWorld())) continue;
			if(!shop.getBoolean("connected")) continue;
			
			ShopSign ss = new ShopSign(plugin, shop);
			if(buying && ss.getConnectedChest().getNumberOfItems() == 0) continue;
			if(!buying && ss.getConnectedChest().getSpace() == 0) continue;
			
			shops.add(shop);
		}
		
		
		return shops;
	}
	
	private ArrayList<FileConfiguration> sortByDistance(Player player, ArrayList<FileConfiguration> shops) {
		
		if(shops.size() == 0) return shops;
		
		HashMap<FileConfiguration, Double> distances = new HashMap<>();
		
		for(FileConfiguration f : shops) {
			
			int x = f.getInt("x");
			int y = f.getInt("y");
			int z = f.getInt("z");
			World world = Bukkit.getWorld(f.getString("world"));
			Location shoploc = new Location(world, x, y, z);
			Location playerloc = player.getLocation();
			double distance = playerloc.distanceSquared(shoploc);
			distances.put(f, distance);
		}
		
		ArrayList<FileConfiguration> sorted = new ArrayList<>();
		
		double min = -1;
		FileConfiguration maptomin = null;
		
		while(!distances.isEmpty()) {
			
			for(FileConfiguration f : distances.keySet()) {
				
				if(min == -1) {
					min = distances.get(f);
					maptomin = f;
					continue;
				}
				
				double distance = distances.get(f);
				
				if(distance < min) {
					min = distance;
					maptomin = f;
				}
			
			}
			
			sorted.add(maptomin);
			distances.remove(maptomin);
			min = -1;
		}
		
		return sorted;
	}

}
