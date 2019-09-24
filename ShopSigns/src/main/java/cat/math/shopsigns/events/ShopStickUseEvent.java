package cat.math.shopsigns.events;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerInteractEvent;
import cat.math.shopsigns.ShopSigns;
import cat.math.shopsigns.Util;
import cat.math.shopsigns.material.AdminShop;
import cat.math.shopsigns.material.PrivateShop;
import cat.math.shopsigns.material.ShopSign;

public class ShopStickUseEvent extends Event {

	private static final HandlerList handlers = new HandlerList();
	
	private static HashMap<String,FileConfiguration> reference = new HashMap<String,FileConfiguration>();
	private static HashMap<String,Location> chestlocation = new HashMap<String,Location>();
	ShopSigns plugin;
	
	public ShopStickUseEvent(ShopSigns plugin) {
		this.plugin = plugin;
	}
	
	@Override
	public HandlerList getHandlers() {
		// TODO Auto-generated method stub
		return handlers;
	}
	
    public static HandlerList getHandlerList() {
        return handlers;
    }

	public void runEvent(PlayerInteractEvent event) {
		
		Bukkit.getServer().getPluginManager().callEvent(this);
		
		Block block = event.getClickedBlock();
		
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
			Player player = event.getPlayer();
			if(!ss.getOwner().getPlayer().getPlayer().getUniqueId().toString().equals(player.getUniqueId().toString())) {
				player.sendMessage(Util.color(plugin.getConfig().getString("errors.shopsign-not-yours", "&cThis shop sign is not yours!")));
				return;
			}
			
			if(!chestlocation.containsKey(player.getUniqueId().toString()) && !ss.isConnected()) {
				reference.put(player.getUniqueId().toString(), shop);
				String s = plugin.getConfig().getString("confirms.shopsign-stored", "&aShopSign located at &e%scoords% &astored.");
				Location l = ss.getSign().getLocation();
				s = s.replace("%scoords%", l.getBlockX()+","+l.getBlockY()+","+l.getBlockZ());
				s = Util.color(s);
				event.getPlayer().sendMessage(s);
			}
			
			else {
				
				if(ss.isConnected()) {
					String s = plugin.getConfig().getString("errors.shopsign-already-connected", 
							"&cThis sign is already connected to a chest. You must either break the chest or this sign to disconnect it.");
					s = Util.color(s);
					event.getPlayer().sendMessage(s);
					return;
				}
				
				ss.connectToChest(chestlocation.get(player.getUniqueId().toString()));
				chestlocation.remove(player.getUniqueId().toString());
			}
			
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
			
			player = event.getPlayer();
			if(Util.getShopFromChest(block) != null) {
				String s = plugin.getConfig().getString("errors.shopchest-already-connected", 
						"&cThis chest is already connected to a sign. You must either break this chest or the sign to disconnect it.");
				s = Util.color(s);
				player.sendMessage(s);
				return;
			}
			
			if(!reference.containsKey(player.getUniqueId().toString())) {
				chestlocation.put(player.getUniqueId().toString(), block.getLocation());
				String s = plugin.getConfig().getString("confirms.shopchest-stored", "&aContainer located at &e%ccoords% &astored.");
				Location l = block.getLocation();
				s = s.replace("%ccoords%", l.getBlockX()+","+l.getBlockY()+","+l.getBlockZ());
				s = Util.color(s);
				event.getPlayer().sendMessage(s);
			}
			else {
				
				shop = reference.get(player.getUniqueId().toString());
				
				type = shop.getInt("type");				
				if(type < 3) ss = new ShopSign(plugin, shop);
				else if(type >= 3 && type < 6) ss = new AdminShop(plugin, shop);
				else ss = new PrivateShop(plugin, shop);
				
				if(ss.isConnected()) {
					String s = plugin.getConfig().getString("errors.shopsign-already-connected", 
							"&cThis sign is already connected to a chest. You must either break the chest or this sign to disconnect it.");
					s = Util.color(s);
					event.getPlayer().sendMessage(s);
					return;
				}
				
				ss.connectToChest(block.getLocation());
				reference.remove(player.getUniqueId().toString());
			}
			
			break;
		default:
			return;
		}
	}
}