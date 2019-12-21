package cat.math.shopsigns.events;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerInteractEvent;

import cat.math.shopsigns.MyEntry;
import cat.math.shopsigns.ShopSigns;
import cat.math.shopsigns.Util;
import cat.math.shopsigns.commands.Shop;
import cat.math.shopsigns.material.PrivateShop;
import cat.math.shopsigns.material.ShopSign;
import javafx.util.Pair;

public class ShopStickHitEvent extends Event {

	private static final HandlerList handlers = new HandlerList();
	ShopSigns plugin;
	
	public ShopStickHitEvent(ShopSigns plugin) {
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
			if(type < 6) return;
			if(!Shop.blargh.containsKey(event.getPlayer())) return;
			PrivateShop ss = new PrivateShop(plugin, shop);
			
			MyEntry<OfflinePlayer, Boolean> cmd = Shop.blargh.get(event.getPlayer());
			if(cmd.getValue()) ss.removePlayer(cmd.getKey());
			else ss.addPlayer(cmd.getKey());
			
			Shop.blargh.remove(event.getPlayer());
			
		default:
			return;
		}
	}

    
}