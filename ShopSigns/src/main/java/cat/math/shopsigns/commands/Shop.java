package cat.math.shopsigns.commands;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import cat.math.shopsigns.ShopSigns;
import cat.math.shopsigns.Util;
import cat.math.shopsigns.material.ShopSign;
import cat.math.shopsigns.MyEntry;

public class Shop implements TabExecutor {

	ShopSigns plugin;
	public static HashMap<Player, MyEntry<OfflinePlayer, Boolean>> blargh = new HashMap<>();
	
	public Shop(ShopSigns plugin) {
		this.plugin = plugin;
	}
	
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

		if(args.length == 0) {
			
			sender.sendMessage(Util.color("&c/shop [add|remove|guide]"));
			return true;
		}
		
		if(!(sender instanceof Player)) {
			
			sender.sendMessage(Util.color("&cOnly players may use this command!"));
			return true;
		}
		
		Player p = (Player) sender;
		
		if(args[0].contentEquals("guide")) {
			
			int page = 1;
			
			if(args.length > 1) {
				
				page = Util.stringToInt(args[1]);				
				if(page <= 0) page = 1;
			}
			
			displayHelpPage(p, page);
			return true;
		}
		
		if(args.length == 1) {
			
			sender.sendMessage(Util.color("&c/shop [add|remove|guide]"));
			return true;
		}
		
		OfflinePlayer player = Util.searchForPlayer(args[1]);
		
		if(player == null) {
			
			sender.sendMessage(Util.color("&cPlayer not found."));
			return true;
		}
		
		String uuid = p.getUniqueId().toString();
		File data = new File(plugin.getDataFolder(),"Users");
		boolean owner = false;
		
		for(String s : data.list()) {
			
			if(uuid.contentEquals(s)) {
				
				for(File f : new File(data, s).listFiles()) {
					
					FileConfiguration c = YamlConfiguration.loadConfiguration(f);
					ShopSign ss = new ShopSign(plugin, c);
					if(ss.getType() >= 6) owner = true;
				}
				
				break;
			}
		}
		
		if(!owner) {
			
			sender.sendMessage(Util.color("&cYou do not own any private shops."));
			return true;
		}
		
		if(args[0].equalsIgnoreCase("add")) {
			
			
			blargh.put(p, new MyEntry<OfflinePlayer, Boolean>(player, false));
			sender.sendMessage(Util.color("&aNow smack the shop to which you want to add this user! :)"));
			return true;
		}
		
		if(args[0].equalsIgnoreCase("remove")) {
			
			blargh.put(p, new MyEntry<OfflinePlayer, Boolean>(player, true));
			sender.sendMessage(Util.color("&aNow smack the shop from which you want to remove this user! :)"));
			return true;
		}
		
		return false;
	}
	
	private void displayHelpPage(Player player, int page) {
		
		StringBuilder message = new StringBuilder();
		if(page > 5) page = 5;
		
		if(page == 1) {
			
			message.append("&b---Page(1/5)---\n")
				.append("Default sign format\n")
				.append("On the first line of the shop sign, to sell something, put in [sell]. To buy, it is [buy]. The option for both also exists as [buy : sell].\n")
				.append("On the second line, type in the item you wish to buy and/or sell")
				.append("On the third line, type the number of items you want to sell at a time.\n")
				.append("On the final line, put the price of the item.\n")
				.append("For example, to make a buy/sell sign buying and selling diamonds,\n")
				.append(" - [buy : sell]\n")
				.append(" - Diamond\n")
				.append(" - 1\n")
				.append(" - 200 : 50\n")
				.append("This example sign will have you sell diamonds 1 at a time at $200 each and buy them 1 at a time for $50 each.");
		}
		
		if(page == 2) {
			
			message.append("&b---Page(2/5)---\n")
				.append("Chest linking\n")
				.append("Once you have set up a shop sign, you will need a chest from which the sign will draw.\n")
				.append("This can be anything from chests to shulker boxes to barrels, but you will need a special linking tool.")
				.append("First, however, you must place the item you want to buy or sell in the chest.")
				.append("Then, you will need to name a stick in an anvil to 'Shop Wand'.\n")
				.append("Once you do this, you can right click the sign and chest (in any order) with the wand to connect them.")
				.append("You should receive a confirm message after each step of the process.");
		}
		
		if(page == 3) {
			
			message.append("&b---Page(3/5)---\n")
				.append("Buying and Selling\n")
				.append("To buy and sell from other people's shop signs, simply right click the sign to buy and left click it to sell.")
				.append(" A boss bar should show up on your screen that will tell you how full or empty the shop is. ")
				.append("Some shops, you may need special permission to use. The first line of these shops will be preceded by a 'P'.")
				.append("If you make one of these shops, you can add or remove others' permission by doing /shop add or /shop remove and then smack the private shop with the shop wand.");
			
		}
		
		if(page == 4) {
			
			message.append("&b---Page(4/5)---\n")
				.append("Finding Shops")
				.append("To find a shop, the command is &e/find [buy|sell] [item] <player> <page>&b. If you're looking to buy an item,")
				.append(" then you do /find buy while if you're looking to sell an item, do /find sell. The second argument is fairly ")
				.append("self-explanatory. Type the name of the material you're looking to buy or sell. The third argument depends on ")
				.append("whether or not you care from whom you're buying/selling. If you don't care, you can leave this blank to view ")
				.append("the first page of the list of shops that will come up. To view subsequent pages, simply retype the command and ")
				.append("add a page number at the end. You can look for a specific person's shops by adding a player name in between ")
				.append("the item and the page number.");
		}
		
		if(page == 5) {
			
			message.append("&b---Page(5/5)---\n")
				.append("Notifications\n")
				.append("If this setting is turned on, a sell shop when empty or a buy shop when full will send the shop owner a mail letting them know as such.")
				.append(" The notification will include specific information about the sign so the player may know which one to restock.")
				.append(" With that last bit of information, I conclude this tutorial for this plugin. Happy shopping :)");
		}
		
		player.sendMessage(Util.color(new String(message)));
	}

}
