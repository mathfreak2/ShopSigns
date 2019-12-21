package cat.math.shopsigns.listeners;

import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import cat.math.shopsigns.ShopSigns;

public class Preprocessor implements Listener {

	ShopSigns plugin;
	
	public Preprocessor(ShopSigns plugin) {
		this.plugin = plugin;
	}
	
	public void onCommandPreprocess(PlayerCommandPreprocessEvent event) {
		
		switch(event.getMessage()) {
		
		case "/buy":
			break;
		case "/sell":
			break;
		default:
			return;
		}
	}
}
