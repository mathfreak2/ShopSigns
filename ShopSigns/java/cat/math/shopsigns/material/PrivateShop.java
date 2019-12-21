package cat.math.shopsigns.material;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.block.Sign;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import com.earth2me.essentials.User;

import cat.math.shopsigns.ShopSignOwner;
import cat.math.shopsigns.ShopSigns;
import cat.math.shopsigns.Util;

public class PrivateShop extends ShopSign {

	ArrayList<String> allowedPlayers = new ArrayList<String>();
	
	public PrivateShop(ShopSigns plugin, Sign sign, int type, ShopSignOwner sso, String[] lines) {
		super(plugin, sign, type, sso, lines);
	}
	
	public PrivateShop(ShopSigns plugin, FileConfiguration file) {
		super(plugin, file);
		
		List<String> a = file.getStringList("allowed-players");
		allowedPlayers.addAll(a);
	}
	
	@Override
	public void buy(User buyer) {
		
		if(!hasPermission(buyer.getBase())) {
			
			String s = plugin.getConfig().getString("errors.sign-no-buy-permission","&cYou do not have permission to buy from that shop.");
			s = Util.color(s);
			buyer.getBase().sendMessage(s);
			return;
		}
		
		super.buy(buyer);
	}
	
	@Override
	public void sell(User seller) {
		
		if(!hasPermission(seller.getBase())) {
			
			String s = plugin.getConfig().getString("errors.sign-no-sell-permission","&cYou do not have permission to sell to that shop.");
			s = Util.color(s);
			seller.getBase().sendMessage(s);
			return;
		}
		
		super.sell(seller);
	}
	
	private boolean hasPermission(Player player) {
		return (allowedPlayers.contains(player.getUniqueId().toString()) ||
				player.hasPermission("shopsigns.bypass-privacy"));
	}
	
	public void addPlayer(Player player) {
		
		if(allowedPlayers.contains(player.getUniqueId().toString())) {
			
			String s = plugin.getConfig().getString("errors.add-permission-failure", "&cThat person already has permission to this shop.");
			s = Util.color(s);
			sso.getPlayer().getPlayer().sendMessage(s);
			return;
		}
		
		allowedPlayers.add(player.getUniqueId().toString());
		FileConfiguration shop = getFile();
		shop.set("allowed-players", allowedPlayers);
		try {
			shop.save(new File(sso.getDataFolder(),getFileName()));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		String s = plugin.getConfig().getString("confirms.add-permission-success", "&aYou have added &e%user% &ato this shop!");
		s = s.replace("%user%", player.getName());
		s = Util.color(s);
		sso.getPlayer().getPlayer().sendMessage(s);
		return;
	}
	
	public void removePlayer(Player player) {
		
		if(!allowedPlayers.contains(player.getUniqueId().toString())) {
			
			String s = plugin.getConfig().getString("errors.remove-permission-failure", "&cThat person does not already have permission to this shop.");
			s = Util.color(s);
			sso.getPlayer().getPlayer().sendMessage(s);
			return;
		}
		
		allowedPlayers.remove(player.getUniqueId().toString());
		FileConfiguration shop = getFile();
		shop.set("allowed-players", allowedPlayers);
		try {
			shop.save(new File(sso.getDataFolder(),getFileName()));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		String s = plugin.getConfig().getString("confirms.remove-permission-success", "&cYou have removed &e%user% &cfrom this shop.");
		s = s.replace("%user%", player.getName());
		s = Util.color(s);
		sso.getPlayer().getPlayer().sendMessage(s);
		return;
	}
}