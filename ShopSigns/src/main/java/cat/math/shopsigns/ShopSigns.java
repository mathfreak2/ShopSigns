package cat.math.shopsigns;

import java.io.File;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.earth2me.essentials.Essentials;

import cat.math.shopsigns.commands.Find;
import cat.math.shopsigns.commands.Shop;
import cat.math.shopsigns.listeners.PlayerInteraction;
import cat.math.shopsigns.listeners.ShopSignCreator;
import cat.math.shopsigns.listeners.ShopStickCreator;

public class ShopSigns extends JavaPlugin {
	
	Essentials essentials;
	File users;
	
	@Override
	public void onEnable() {
		
		PluginManager pm = Bukkit.getServer().getPluginManager();
		Plugin e = pm.getPlugin("Essentials");
		if(e == null) {
			getLogger().log(Level.SEVERE, "Cannot resolve dependent plugins.");
			return;
		}
		essentials = (Essentials) e;
		
		loadConfig();
		registerEvents();
		registerCommands();
		prepareDataSpace();
	}
	
	public void loadConfig() {
		
		File directory = getDataFolder();
		
		try {
			
			if(!directory.exists())
				directory.mkdir();
			
			File config = new File(directory, "config.yml");
			if(!config.exists()) this.saveDefaultConfig();
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		FileConfiguration config = this.getConfig();
		
		try {
			config.load(new File(directory, "config.yml"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void registerEvents() {
		
		PluginManager pm = Bukkit.getServer().getPluginManager();
		pm.registerEvents(new ShopStickCreator(this), this);
		pm.registerEvents(new PlayerInteraction(this), this);
		pm.registerEvents(new ShopSignCreator(this), this);
	}
	
	private void registerCommands() {
		
		this.getCommand("shop").setExecutor(new Shop(this));
		this.getCommand("shopsigns").setExecutor(new cat.math.shopsigns.commands.ShopSigns(this));
		this.getCommand("find").setExecutor(new Find(this));
	}
	
	private void prepareDataSpace() {
		
		File directory = this.getDataFolder();
		
		try {
			
			if(!directory.exists()) directory.mkdir();
			File shopowners = new File(directory, "Users");
			if(!shopowners.exists()) shopowners.mkdir();
			users = shopowners;
			
			File material_ids = new File(directory, "material-ids.yml");
			if(!material_ids.exists()) material_ids.createNewFile();
			FileConfiguration mats = YamlConfiguration.loadConfiguration(material_ids);
			mats.set("README", "Do not change any of these values.");
			Material[] list = Material.values();
			for(int i=0; i<list.length; i++) {
				mats.set(list[i].toString(), i);
			}
			
			mats.save(material_ids);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void onDisable() {
		
	}
	
	public Essentials getEssentials() {return essentials;}
	
}