package cat.math.shopsigns.events;

import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerInteractEvent;
import cat.math.shopsigns.ShopSigns;

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

	}

    
}