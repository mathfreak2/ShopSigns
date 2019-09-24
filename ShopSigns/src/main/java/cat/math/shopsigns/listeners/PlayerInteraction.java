package cat.math.shopsigns.listeners;

import java.util.ArrayList;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitScheduler;
import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.User;
import cat.math.shopsigns.ShopSigns;
import cat.math.shopsigns.Util;
import cat.math.shopsigns.events.ShopStickHitEvent;
import cat.math.shopsigns.events.ShopStickUseEvent;
import cat.math.shopsigns.material.AdminShop;
import cat.math.shopsigns.material.PrivateShop;
import cat.math.shopsigns.material.ShopSign;

public class PlayerInteraction implements Listener {
	
	ShopSigns plugin;
	static ArrayList<String> progressbardelay = new ArrayList<String>();
	
	public PlayerInteraction(ShopSigns plugin) {
		this.plugin = plugin;
	}
	
	@EventHandler
	public void onInteract(PlayerInteractEvent event) {
		
		Action action = event.getAction();
		
		// If nothing was clicked on, do not proceed
		if(event.getClickedBlock().isEmpty()) return;
		
		switch(action) {
		
		case LEFT_CLICK_BLOCK:
			onLeftClick(event);
			break;
		case RIGHT_CLICK_BLOCK:
			onRightClick(event);
			break;
		default:
			return;
		}
	}
	
	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) {
		
		Block b = event.getBlock();
		
		switch (b.getType()) {
		
		case ACACIA_SIGN:
		case ACACIA_WALL_SIGN:
		case BIRCH_SIGN:
		case BIRCH_WALL_SIGN:
		case JUNGLE_SIGN:
		case JUNGLE_WALL_SIGN:
		case SPRUCE_SIGN:
		case SPRUCE_WALL_SIGN:
		case DARK_OAK_SIGN:
		case DARK_OAK_WALL_SIGN:
		case OAK_SIGN:
		case OAK_WALL_SIGN:
			FileConfiguration shop = Util.getShopSign(b);
			if(shop == null) return;
			int type = shop.getInt("type");			
			ShopSign ss;			
			if(type < 3) ss = new ShopSign(plugin, shop);
			else if(type >= 3 && type < 6) ss = new AdminShop(plugin, shop);
			else ss = new PrivateShop(plugin, shop);
			ss.remove(event.getPlayer());
			break;
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
			shop = Util.getShopFromChest(b);
			if(shop == null) return;
			type = shop.getInt("type");			
			if(type < 3) ss = new ShopSign(plugin, shop);
			else if(type >= 3 && type < 6) ss = new AdminShop(plugin, shop);
			else ss = new PrivateShop(plugin, shop);
			ss.disconnectChest(event.getPlayer());
			break;
		default:
			return;
		}
	}
	
	@EventHandler
	public void onLookingAtShop(PlayerMoveEvent event) {
		
		Player player = event.getPlayer();
		Block block = player.getTargetBlock(null, 10);
		
		switch(block.getType()) {
		
		case ACACIA_SIGN:
		case ACACIA_WALL_SIGN:
		case BIRCH_SIGN:
		case BIRCH_WALL_SIGN:
		case JUNGLE_SIGN:
		case JUNGLE_WALL_SIGN:
		case SPRUCE_SIGN:
		case SPRUCE_WALL_SIGN:
		case DARK_OAK_SIGN:
		case DARK_OAK_WALL_SIGN:
		case OAK_SIGN:
		case OAK_WALL_SIGN:
			FileConfiguration shop = Util.getShopSign(block);
			if(shop == null) return;
			int type = shop.getInt("type");			
			ShopSign ss;			
			if(type < 3) ss = new ShopSign(plugin, shop);
			else if(type >= 3 && type < 6) ss = new AdminShop(plugin, shop);
			else ss = new PrivateShop(plugin, shop);
			displayShopProgressBar(player, ss);
			return;
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
			shop = Util.getShopFromChest(block);
			if(shop == null) return;
			type = shop.getInt("type");			
			if(type < 3) ss = new ShopSign(plugin, shop);
			else if(type >= 3 && type < 6) ss = new AdminShop(plugin, shop);
			else ss = new PrivateShop(plugin, shop);
			displayShopProgressBar(player, ss);
			return;
		default:
			return;
		}
		
	}
	
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		progressbardelay.remove(event.getPlayer().getUniqueId().toString());
	}
	
	public void displayShopProgressBar(Player player, ShopSign ss) {
		
		if(!ss.isConnected()) return;
		if(!plugin.getConfig().getBoolean("feedback.show-progress-bars", true)) return;
		if(progressbardelay.contains(player.getUniqueId().toString())) return;
		
		ss.showProgressBar(player);
		progressbardelay.add(player.getUniqueId().toString());
		
		class Runner implements Runnable {
			
			ShopSign ss;
			Player p;
			
			public Runner(Player p, ShopSign ss) {
				this.ss = ss;
				this.p = p;
			}
			
			@Override
			public void run() {
				
				ss.hideProgressBar(p);
				PlayerInteraction.progressbardelay.remove(p.getUniqueId().toString());
			}
		}
		
		BukkitScheduler bs = Bukkit.getServer().getScheduler();
		bs.runTaskLater(plugin, new Runner(player, ss), plugin.getConfig().getLong("feedback.progress-bar-delay", 100));
		
	}
	
	public void onLeftClick(PlayerInteractEvent event) {
		
		Block block = event.getClickedBlock();
		
		if(Util.isShopStick(event.getItem())) {
			
			switch (block.getType()) {
			
			case ACACIA_SIGN:
			case ACACIA_WALL_SIGN:
			case BIRCH_SIGN:
			case BIRCH_WALL_SIGN:
			case JUNGLE_SIGN:
			case JUNGLE_WALL_SIGN:
			case SPRUCE_SIGN:
			case SPRUCE_WALL_SIGN:
			case DARK_OAK_SIGN:
			case DARK_OAK_WALL_SIGN:
			case OAK_SIGN:
			case OAK_WALL_SIGN:
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
				ShopStickHitEvent e = new ShopStickHitEvent(plugin);
				e.runEvent(event);
				break;
			default:
				return;
			}
		}
		
		else {
			
			switch (block.getType()) {
			
			case ACACIA_SIGN:
			case ACACIA_WALL_SIGN:
			case BIRCH_SIGN:
			case BIRCH_WALL_SIGN:
			case JUNGLE_SIGN:
			case JUNGLE_WALL_SIGN:
			case SPRUCE_SIGN:
			case SPRUCE_WALL_SIGN:
			case DARK_OAK_SIGN:
			case DARK_OAK_WALL_SIGN:
			case OAK_SIGN:
			case OAK_WALL_SIGN:
				
				FileConfiguration shop = Util.getShopSign(block);
				if(shop == null) return;
				
				int type = shop.getInt("type");
				
				ShopSign ss;
				
				if(type < 3) ss = new ShopSign(plugin, shop);
				else if(type >= 3 && type < 6) ss = new AdminShop(plugin, shop);
				else ss = new PrivateShop(plugin, shop);
				
				Essentials essentials = plugin.getEssentials();
				User user = essentials.getUser(event.getPlayer());
				
				ss.sell(user);
				
				break;
			default:
				return;
			}
			
		}
		
	}
	
	public void onRightClick(PlayerInteractEvent event) {
		
		Block block = event.getClickedBlock();
		
		if(Util.isShopStick(event.getItem())) {
			
			switch (block.getType()) {
			
			case ACACIA_SIGN:
			case ACACIA_WALL_SIGN:
			case BIRCH_SIGN:
			case BIRCH_WALL_SIGN:
			case JUNGLE_SIGN:
			case JUNGLE_WALL_SIGN:
			case SPRUCE_SIGN:
			case SPRUCE_WALL_SIGN:
			case DARK_OAK_SIGN:
			case DARK_OAK_WALL_SIGN:
			case OAK_SIGN:
			case OAK_WALL_SIGN:
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
				ShopStickUseEvent e = new ShopStickUseEvent(plugin);
				e.runEvent(event);
				break;
			default:
				return;
			}
		}
		
		else {
			
			switch (block.getType()) {
			
			case ACACIA_SIGN:
			case ACACIA_WALL_SIGN:
			case BIRCH_SIGN:
			case BIRCH_WALL_SIGN:
			case JUNGLE_SIGN:
			case JUNGLE_WALL_SIGN:
			case SPRUCE_SIGN:
			case SPRUCE_WALL_SIGN:
			case DARK_OAK_SIGN:
			case DARK_OAK_WALL_SIGN:
			case OAK_SIGN:
			case OAK_WALL_SIGN:
				FileConfiguration shop = Util.getShopSign(block);
				if(shop == null) return;
				
				int type = shop.getInt("type");
				
				ShopSign ss;
				
				if(type < 3) ss = new ShopSign(plugin, shop);
				else if(type >= 3 && type < 6) ss = new AdminShop(plugin, shop);
				else ss = new PrivateShop(plugin, shop);
				
				Essentials essentials = plugin.getEssentials();
				User user = essentials.getUser(event.getPlayer());
				
				ss.buy(user);
				
				break;
			default:
				return;
			}
		}
	}
}