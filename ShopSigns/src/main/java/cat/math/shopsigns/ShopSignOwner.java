package cat.math.shopsigns;

import java.io.File;
import java.util.List;
import java.util.UUID;

import org.bukkit.OfflinePlayer;

import com.earth2me.essentials.User;

public class ShopSignOwner {
	
	ShopSigns plugin;
	OfflinePlayer player;
	File info;
	
	public ShopSignOwner(ShopSigns plugin, OfflinePlayer player) {
		this.plugin = plugin;
		this.player = player;
		
		try {
			
			File users = new File(plugin.getDataFolder(), "Users");
			info = new File(users,player.getUniqueId().toString());
			if(!info.exists()) info.mkdir();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void sendFeedback(String s) {
		
		User self = getUser();
		List<String> mails = self.getMails();
		
		for(String string : mails) {
			if(s.equals(string)) return;
		}
		
		self.addMail(s);
	}
	
	
	public User getUser() {return plugin.getEssentials().getUser(UUID.fromString(player.getUniqueId().toString()));}
	public OfflinePlayer getPlayer() {return player;}
	public File getDataFolder() {return info;}
}
