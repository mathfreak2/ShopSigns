package cat.math.shopsigns;

import java.io.File;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import cat.math.shopsigns.material.ShopStick;
import net.md_5.bungee.api.ChatColor;

public class Util {

	public static int stringToInt(String s) {

        int result = 0;
        try {
            for(int i=0; i<s.length(); i++) {
                double m;
                if(s.charAt(i) == '0') m = 0;
                else if(s.charAt(i) == '1') m = 1;
                else if(s.charAt(i) == '2') m = 2;
                else if(s.charAt(i) == '3') m = 3;
                else if(s.charAt(i) == '4') m = 4;
                else if(s.charAt(i) == '5') m = 5;
                else if(s.charAt(i) == '6') m = 6;
                else if(s.charAt(i) == '7') m = 7;
                else if(s.charAt(i) == '8') m = 8;
                else if(s.charAt(i) == '9') m = 9;
                else return -1;
                result += Math.pow(10, s.length()-i-1)*m;
            }
        }
        catch(Exception e) {
            return -1;
        }

        return result;
    }
	
	public static double stringToDouble(String s) {
		
		double result = 0;
		int point = s.indexOf('.');
		if(point == -1) return (double)stringToInt(s);
		
		try {
            for(int i=0; i<s.length(); i++) {
                double m;
                if(s.charAt(i) == '0') m = 0;
                else if(s.charAt(i) == '1') m = 1;
                else if(s.charAt(i) == '2') m = 2;
                else if(s.charAt(i) == '3') m = 3;
                else if(s.charAt(i) == '4') m = 4;
                else if(s.charAt(i) == '5') m = 5;
                else if(s.charAt(i) == '6') m = 6;
                else if(s.charAt(i) == '7') m = 7;
                else if(s.charAt(i) == '8') m = 8;
                else if(s.charAt(i) == '9') m = 9;
                else if(s.charAt(i) == '.') continue;
                else return -1;
                if(i-1 < point) result += Math.pow(10, point-i-1)*m;
                else result += Math.pow(10, point-i)*m;
            }
        }
        catch(Exception e) {
            return -1;
        }
		
		return result;
	}
	
	public static String color(String s) {
		
		s = ChatColor.translateAlternateColorCodes('&', s);
		return s;
	}
	
	public static boolean isShopStick(ItemStack item) {
		
		if(item == null) return false;
		ItemMeta im = item.getItemMeta();
		if(im == null) return false;
		if(!item.getType().equals(ShopStick.stick)) return false;
		return im.getDisplayName().equals(ShopStick.name);
	}
	
	public static FileConfiguration getShopSign(Block b) {
		
		File directory = ShopSigns.getPlugin(ShopSigns.class).getDataFolder();
		File users = new File(directory, "Users");
		if(!users.exists()) users.mkdir();
		String[] ownerslist = users.list();
		
		for(String s : ownerslist) {
			
			File owner = new File(users, s);
			String[] shoplist = owner.list();
			
			for(String m : shoplist) {
				
				File f = new File(owner, m);
				FileConfiguration shop = YamlConfiguration.loadConfiguration(f);
				
				int x = shop.getInt("x");
				int y = shop.getInt("y");
				int z = shop.getInt("z");
				String worldname = shop.getString("world");
				
				if(b.getX() == x && b.getY() == y && b.getZ() == z && b.getWorld().getName().equals(worldname)) return shop;
			}
		}
		
		return null;
	}
	
public static FileConfiguration getShopFromChest(Block b) {
		
		File directory = ShopSigns.getPlugin(ShopSigns.class).getDataFolder();
		File users = new File(directory, "Users");
		if(!users.exists()) users.mkdir();
		String[] ownerslist = users.list();
		
		for(String s : ownerslist) {
			
			File owner = new File(users, s);
			String[] shoplist = owner.list();
			
			for(String m : shoplist) {
				
				File f = new File(owner, m);
				FileConfiguration shop = YamlConfiguration.loadConfiguration(f);
				
				int x;
				if(shop.isSet("chest-location.x")) x = shop.getInt("chest-location.x");
				else continue;
				int y = shop.getInt("chest-location.y");
				int z = shop.getInt("chest-location.z");
				String worldname = shop.getString("chest-location.world");
				
				if(b.getX() == x && b.getY() == y && b.getZ() == z && b.getWorld().getName().equals(worldname)) return shop;
			}
		}
		
		return null;
	}
	
	public static Material parseItem(String s) {
		
		if(stringToInt(s) == -1) {
			s = s.replace(" ", "_");
			s = s.toUpperCase();
			return Material.getMaterial(s);
		}
		
		else {
			
			int x = stringToInt(s);
			Material[] values = Material.values();
			if(x >= values.length) return null; 
			else return values[x];
		}
	}
	
	public static OfflinePlayer searchForPlayer(String name) {
		
		OfflinePlayer[] players = Bukkit.getServer().getOfflinePlayers();
		
		for(OfflinePlayer p : players) {
			String n = p.getName();
			if(n.equalsIgnoreCase(name)) return p;
		}
		
		return null;
	}
}