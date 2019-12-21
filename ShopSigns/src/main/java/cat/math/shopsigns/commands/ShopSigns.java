package cat.math.shopsigns.commands;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import cat.math.shopsigns.Util;

public class ShopSigns implements TabExecutor {

	cat.math.shopsigns.ShopSigns plugin;
	
	public ShopSigns(cat.math.shopsigns.ShopSigns plugin) {
		this.plugin = plugin;
	}
	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		
		if(!(sender instanceof Player) || !sender.hasPermission("shopsigns.admin")) {
			
			sender.sendMessage(Util.color("&cYou do not have permission."));
			return true;
		}
		
		if(args.length == 0) {
			
			sender.sendMessage(Util.color("&c/shopsigns [options]"));
			return true;
		}
		
		if(args.length == 1) {
			
			sender.sendMessage(Util.color("&c/shopsigns [options]"));
			return true;
		}
		
		if(args[0].contentEquals("list")) {
			
			OfflinePlayer player = Util.searchForPlayer(args[1]);
			
			if(player == null) {
				
				sender.sendMessage(Util.color("&cPlayer not found."));
				return true;
			}
			
			String uuid = player.getUniqueId().toString();
			File users = new File(plugin.getDataFolder(), "Users");
			File shops = new File(users, uuid);
			StringBuilder message = new StringBuilder();
			
			int page = 1;
			
			if(args.length > 2) {
				
				page = Util.stringToInt(args[2]);
				if(page <= 0) page = 1;
				if(page > shops.list().length) page = shops.list().length;
			}
			
			File f = shops.listFiles()[page-1];
			FileConfiguration c = YamlConfiguration.loadConfiguration(f);
			message.append("&b---Page("+page+"/"+shops.list().length+")---\n");
			
			Map<String, Object> list = c.getValues(true);
			
			for(String s : list.keySet()) {
				
				message.append(s+": "+list.get(s)+"\n");
			}
			
			sender.sendMessage(Util.color(new String(message)));
			return true;
		}
		
		sender.sendMessage(Util.color("&c/shopsigns [options]"));
		return true;
	}
}
