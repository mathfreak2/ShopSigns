package cat.math.shopsigns.listeners;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Sign;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import cat.math.shopsigns.ShopSignOwner;
import cat.math.shopsigns.ShopSigns;
import cat.math.shopsigns.Util;
import cat.math.shopsigns.material.AdminShop;
import cat.math.shopsigns.material.PrivateShop;
import cat.math.shopsigns.material.ShopSign;

public class ShopSignCreator implements Listener {
	
	ShopSigns plugin;
	
	public ShopSignCreator(ShopSigns plugin) {
		this.plugin = plugin;
	}
	
	private void restoreDefaultSignFormat(int error) {
		
		Bukkit.getServer().getLogger().log(Level.SEVERE, "Sign format not configured properly with error code "+error+". Resetting sign format to default values...");
		FileConfiguration config = plugin.getConfig();
		ArrayList<String> buy_format = new ArrayList<String>(4);
		ArrayList<String> sell_format = new ArrayList<String>(4);
		ArrayList<String> buy_sell_format = new ArrayList<String>(4);
		
		buy_format.set(0, "%buy%");
		buy_format.set(1, "%item%");
		buy_format.set(2, "%quantity%");
		buy_format.set(3, "%buy_price%");
		config.set("sign-format.buy", buy_format);
		
		sell_format.set(0, "%sell%");
		sell_format.set(1, "%item%");
		sell_format.set(2, "%quantity%");
		sell_format.set(3, "%sell_price%");
		config.set("sign-format.sell", sell_format);
		
		buy_sell_format.set(0, "%buy_sell%");
		buy_sell_format.set(1, "%item%");
		buy_sell_format.set(2, "%quantity%");
		buy_sell_format.set(3, "%buy_sell_price%");
		config.set("sign-format.buy-sell", buy_sell_format);
		
		config.set("sign-format.keywords.buy", "[buy]");
		config.set("sign-format.keywords.sell", "[sell]");
		config.set("sign-format.keywords.buy-sell", "[buy : sell]");
		config.set("sign-format.keywords.buy-sell-price", "%buy_price% : %sell_price%");
	}
	
	@EventHandler
	public void onSignChanged(SignChangeEvent event) {
		
		List<String> buy_format = plugin.getConfig().getStringList("sign-format.buy");
		List<String> sell_format = plugin.getConfig().getStringList("sign-format.sell");
		List<String> buy_sell_format = plugin.getConfig().getStringList("sign-format.buy-sell");
		
		// If the configured format is not of the right length or does not have the required keywords,
		// restore the default configuration.		
		if(buy_format.size() != 4 || sell_format.size() != 4 || buy_sell_format.size() != 4) restoreDefaultSignFormat(0);	
		if(!buy_format.contains("%buy%") || !buy_format.contains("%buy_price%")) restoreDefaultSignFormat(1);
		if(!sell_format.contains("%sell%") || !sell_format.contains("%sell_price%")) restoreDefaultSignFormat(2);
		if(!buy_sell_format.contains("%buy_sell%") || !buy_sell_format.contains("%buy_sell_price%")) restoreDefaultSignFormat(3);
		if(!plugin.getConfig().isSet("sign-format.keywords.buy")) restoreDefaultSignFormat(4);
		if(!plugin.getConfig().isSet("sign-format.keywords.sell")) restoreDefaultSignFormat(5);
		if(!plugin.getConfig().isSet("sign-format.keywords.buy-sell")) restoreDefaultSignFormat(6);
		if(!plugin.getConfig().isSet("sign-format.keywords.buy-sell-price")) restoreDefaultSignFormat(7);
		if(!plugin.getConfig().getString("sign-format.keywords.buy-sell-price").contains("%buy_price%")) restoreDefaultSignFormat(8);
		if(!plugin.getConfig().getString("sign-format.keywords.buy-sell-price").contains("%sell_price%")) restoreDefaultSignFormat(9);
		if(!plugin.getConfig().isSet("sign-format.color")) plugin.getConfig().set("sign-format.color", "&b");
		if(!plugin.getConfig().getString("sign-format.color").contains("&")) plugin.getConfig().set("sign-format.color", "&b");
		
		String[] lines = event.getLines();
		
		int shopsigntype = -1;
		
		// Check if the signs have the correct formatting to be shopsigns.
		for(String s : lines) {
			if(s.equals(plugin.getConfig().getString("sign-format.keywords.buy","[buy]"))) {
				shopsigntype = 0;
				break;
			}
			else if(s.equals(plugin.getConfig().getString("sign-format.keywords.sell","[sell]"))) {
				shopsigntype = 1;
				break;
			}
			else if(s.equals(plugin.getConfig().getString("sign-format.keywords.buy-sell","[buy : sell]"))) {
				shopsigntype = 2;
				break;
			}
			else if(s.equals(plugin.getConfig().getString("sign-format.keywords.admin-shop","S") + 
					plugin.getConfig().getString("sign-format.keywords.buy","[buy]"))) {
				shopsigntype = 3;
				break;
			}
			else if(s.equals(plugin.getConfig().getString("sign-format.keywords.admin-shop","S") + 
					plugin.getConfig().getString("sign-format.keywords.sell","[sell]"))) {
				shopsigntype = 4;
				break;
			}
			else if(s.equals(plugin.getConfig().getString("sign-format.keywords.admin-shop","S") + 
					plugin.getConfig().getString("sign-format.keywords.buy-sell","[buy : sell]"))) {
				shopsigntype = 5;
				break;
			}
			else if(s.equals(plugin.getConfig().getString("sign-format.keywords.private-shop","P") + 
					plugin.getConfig().getString("sign-format.keywords.buy","[buy]"))) {
				shopsigntype = 6;
				break;
			}
			else if(s.equals(plugin.getConfig().getString("sign-format.keywords.private-shop","P") + 
					plugin.getConfig().getString("sign-format.keywords.sell","[sell]"))) {
				shopsigntype = 7;
				break;
			}
			else if(s.equals(plugin.getConfig().getString("sign-format.keywords.private-shop","P") + 
					plugin.getConfig().getString("sign-format.keywords.buy-sell","[buy : sell]"))) {
				shopsigntype = 8;
				break;
			}
		}
		
		// If this is not a shopsign, no further work is required.
		if(shopsigntype == -1) return;
		
		switch (shopsigntype) {
		
		case 0:
			parseBuySign(event, buy_format);
			break;
		case 1:
			parseSellSign(event, sell_format);
			break;
		case 2:
			parseBuySellSign(event, buy_sell_format);
			break;
		case 3:
			parseAdminBuySign(event, buy_format);
			break;
		case 4:
			parseAdminSellSign(event, sell_format);
			break;
		case 5:
			parseAdminBuySellSign(event, buy_sell_format);
			break;
		case 6:
			parsePrivateBuySign(event, buy_format);
			break;
		case 7:
			parsePrivateSellSign(event, sell_format);
			break;
		case 8:
			parsePrivateBuySellSign(event, buy_sell_format);
			break;
		}
	}
	
	private void parsePrivateBuySellSign(SignChangeEvent event, List<String> buy_sell_format) {

		String fline1 = buy_sell_format.get(0);
		String fline2 = buy_sell_format.get(1);
		String fline3 = buy_sell_format.get(2);
		String fline4 = buy_sell_format.get(3);
		
		// Determine which line the %buy_sell% is in the config
		int buy_sell_line;
				
		if(fline1.contains("%buy_sell%")) buy_sell_line = 1;
		else if(fline2.contains("%buy_sell%")) buy_sell_line = 2;
		else if(fline3.contains("%buy_sell%")) buy_sell_line = 3;
		else if(fline4.contains("%buy_sell%")) buy_sell_line = 4;
		else {restoreDefaultSignFormat(14); buy_sell_line = 1;}
				
		// Determine which line the %buy_sell_price% is in the config
		int price_line;
				
		if(fline1.contains("%buy_sell_price%")) price_line = 1;
		else if(fline2.contains("%buy_sell_price%")) price_line = 2;
		else if(fline3.contains("%buy_sell_price%")) price_line = 3;
		else if(fline4.contains("%buy_sell_price%")) price_line = 4;
		else {restoreDefaultSignFormat(15); buy_sell_line = 1; price_line = 4;}
				
		// Determine which line the %item% is in the config if it exists
		int item_line = 0;
				
		if(fline1.contains("%item%")) item_line = 1;
		else if(fline2.contains("%item%")) item_line = 2;
		else if(fline3.contains("%item%")) item_line = 3;
		else if(fline4.contains("%item%")) item_line = 4;
				
		// Determine which line the %quantity% is in the config if it exists
		int quantity_line = 0;
				
		if(fline1.contains("%quantity%")) quantity_line = 1;
		else if(fline2.contains("%quantity%")) quantity_line = 2;
		else if(fline3.contains("%quantity%")) quantity_line = 3;
		else if(fline4.contains("%quantity%")) quantity_line = 4;
		
		// If the sign does not match the configured format, do not make this sign a shopsign
		if(!event.getLine(buy_sell_line-1).equals(plugin.getConfig().getString("sign-format.keywords.private-shop") + 
				plugin.getConfig().getString("sign-format.keywords.buy-sell"))) return;
		
		String buy_sell_price = plugin.getConfig().getString("sign-format.keywords.buy-sell-price", "%buy_price% : %sell_price%");
		
		boolean buybeforesell = true;
		double parsed_buy_price = -1;
		double parsed_sell_price = -1;
		
		if(buy_sell_price.indexOf("%sell_price%") < buy_sell_price.indexOf("%buy_price%")) buybeforesell = false;
		
		String buy_sell = event.getLine(price_line-1);
		buy_sell = buy_sell.replace("$", "");
		
		// If the %buy_price% is before %sell_price% in the config, parse %buy_price% first, else parse %sell_price% first
		if(buybeforesell) {
			
			int b = buy_sell_price.indexOf("%buy_price%");
			
			// If the syntax of the configured sign format does not match, then do not make this a sign shop
			if(!buy_sell.substring(0,b).equals(buy_sell_price.substring(0,b))) {
				Bukkit.getServer().broadcastMessage(buy_sell.substring(0,b) + " : " + buy_sell_price.substring(0,b));
				return;
			}
			
			String p1 = buy_sell.substring(b);
			
			// Measure the length of the number
			int i = 0;
			
			while(p1.charAt(i) == '1' || p1.charAt(i) == '2' || p1.charAt(i) == '3' ||
					p1.charAt(i) == '4' || p1.charAt(i) == '5' || p1.charAt(i) == '6' ||
					p1.charAt(i) == '7' || p1.charAt(i) == '8' || p1.charAt(i) == '9' ||
					p1.charAt(i) == '.') {i++;}
			
			String p2 = p1.substring(0, i);
			parsed_buy_price = Util.stringToDouble(p2);
			
			p1 = p1.substring(i);
			String in_between = buy_sell_price.substring(b+11,buy_sell_price.indexOf("%sell_price%"));
			
			// If the syntax of the configured sign format between the %buy_price% and %sell_price% does not match, then
			// do not make this a sign shop
			if(!p1.substring(0,in_between.length()).equals(in_between)) {
				Bukkit.getServer().broadcastMessage(p1.substring(0,in_between.length()) + " : " + in_between);
				return;
			}
			
			p1 = p1.substring(in_between.length());
			
			// Determine the length of the number
			i = 0;
			
			while(p1.charAt(i) == '1' || p1.charAt(i) == '2' || p1.charAt(i) == '3' ||
					p1.charAt(i) == '4' || p1.charAt(i) == '5' || p1.charAt(i) == '6' ||
					p1.charAt(i) == '7' || p1.charAt(i) == '8' || p1.charAt(i) == '9' ||
					p1.charAt(i) == '.') {i++; if(i==p1.length()) break;}
			
			parsed_sell_price = Util.stringToDouble(p1.substring(0,i));
			
			p1 = p1.substring(i);
			p2 = buy_sell_price.substring(buy_sell_price.indexOf("%sell_price%")+12);
			
			// If the configured format does not match the rest of the line, do not make this a sign shop
			if(!p1.equals(p2)) {
				Bukkit.getServer().broadcastMessage(p1 + " : " + p2);
				return;
			}
		}
		else {
			
			int b = buy_sell_price.indexOf("%sell_price%");
			
			// If the syntax of the configured sign format does not match, then do not make this a sign shop
			if(!buy_sell.substring(0,b).equals(buy_sell_price.substring(0,b))) {
				Bukkit.getServer().broadcastMessage(buy_sell.substring(0,b) + " : " + buy_sell_price.substring(0,b));
				return;
			}
			
			String p1 = buy_sell.substring(b);
			
			// Measure the length of the number
			int i = 0;
			
			while(p1.charAt(i) == '1' || p1.charAt(i) == '2' || p1.charAt(i) == '3' ||
					p1.charAt(i) == '4' || p1.charAt(i) == '5' || p1.charAt(i) == '6' ||
					p1.charAt(i) == '7' || p1.charAt(i) == '8' || p1.charAt(i) == '9' ||
					p1.charAt(i) == '.') {i++;}
			
			String p2 = p1.substring(0, i);
			parsed_sell_price = Util.stringToDouble(p2);
			
			p1 = p1.substring(i);
			String in_between = buy_sell_price.substring(b+12,buy_sell_price.indexOf("%buy_price%"));
			
			// If the syntax of the configured sign format between the %buy_price% and %sell_price% does not match, then
			// do not make this a sign shop
			if(!p1.substring(0,in_between.length()).equals(in_between)) {
				Bukkit.getServer().broadcastMessage(p1.substring(0,in_between.length()) + " : " + in_between);
				return;
			}
			
			p1 = p1.substring(in_between.length());
			
			// Determine the length of the number
			i = 0;
			
			while(p1.charAt(i) == '1' || p1.charAt(i) == '2' || p1.charAt(i) == '3' ||
					p1.charAt(i) == '4' || p1.charAt(i) == '5' || p1.charAt(i) == '6' ||
					p1.charAt(i) == '7' || p1.charAt(i) == '8' || p1.charAt(i) == '9' ||
					p1.charAt(i) == '.') {i++; if(i==p1.length()) break;}
			
			parsed_buy_price = Util.stringToDouble(p1.substring(0,i));
			
			p1 = p1.substring(i);
			p2 = buy_sell_price.substring(buy_sell_price.indexOf("%buy_price%")+11);
			
			// If the configured format does not match the rest of the line, do not make this a sign shop
			if(!p1.equals(p2)) {
				Bukkit.getServer().broadcastMessage(p1 + " : " + p2);
				return;
			}
		}
		
		// Get the line the item is supposed to be listed on if there is such a line
		Material mat = null;
		if(item_line != 0) {
			
			String item = event.getLine(item_line-1);
			mat = Util.parseItem(item);
			
			// If no item is listed when the config requires as such, do not make this a shopsign
			if(mat == null) return;
		}
		
		// Get the line the quantity is supposed to be listed on if there is such a line
		int q = -1;
		if(quantity_line != 0) {
			
			String quantity = event.getLine(quantity_line-1);
			q = Util.stringToInt(quantity);
			
			// If no quantity is listed when the config requires as such, do not make this a shopsign
			if(q == -1) return;
		}
		
		ShopSignOwner sso = new ShopSignOwner(plugin, (OfflinePlayer) event.getPlayer());
		PrivateShop ss = new PrivateShop(plugin, (Sign)event.getBlock().getState(), 8, sso, event.getLines());
		ss.setBuyPrice(parsed_buy_price);
		ss.setSellPrice(parsed_sell_price);
		
		if(mat != null) ss.setItem(mat);
		if(q != -1) ss.setQuantity(q);
		
		for(int i=0; i<4; i++) {
			String line = event.getLine(i);
			line = plugin.getConfig().getString("sign-format.color") + line;
			line = Util.color(line);
			event.setLine(i, line);
		}
		
		ss.register();
		
	}

	private void parsePrivateSellSign(SignChangeEvent event, List<String> sell_format) {

		String fline1 = sell_format.get(0);
		String fline2 = sell_format.get(1);
		String fline3 = sell_format.get(2);
		String fline4 = sell_format.get(3);
		
		// Determine which line the %buy% is in the config
		int sell_line;
				
		if(fline1.contains("%sell%")) sell_line = 1;
		else if(fline2.contains("%sell%")) sell_line = 2;
		else if(fline3.contains("%sell%")) sell_line = 3;
		else if(fline4.contains("%sell%")) sell_line = 4;
		else {restoreDefaultSignFormat(12); sell_line = 1;}
				
		// Determine which line the %sell_price% is in the config
		int price_line;
				
		if(fline1.contains("%sell_price%")) price_line = 1;
		else if(fline2.contains("%sell_price%")) price_line = 2;
		else if(fline3.contains("%sell_price%")) price_line = 3;
		else if(fline4.contains("%sell_price%")) price_line = 4;
		else {restoreDefaultSignFormat(13); sell_line = 1; price_line = 4;}
				
		// Determine which line the %item% is in the config if it exists
		int item_line = 0;
				
		if(fline1.contains("%item%")) item_line = 1;
		else if(fline2.contains("%item%")) item_line = 2;
		else if(fline3.contains("%item%")) item_line = 3;
		else if(fline4.contains("%item%")) item_line = 4;
				
		// Determine which line the %quantity% is in the config if it exists
		int quantity_line = 0;
				
		if(fline1.contains("%quantity%")) quantity_line = 1;
		else if(fline2.contains("%quantity%")) quantity_line = 2;
		else if(fline3.contains("%quantity%")) quantity_line = 3;
		else if(fline4.contains("%quantity%")) quantity_line = 4;	
		
		// If the sign does not match the configured format, do not make this sign a shopsign
		if(!event.getLine(sell_line-1).equals(plugin.getConfig().getString("sign-format.keywords.private-shop") + 
				plugin.getConfig().getString("sign-format.keywords.sell"))) return;
		
		// Get the line the price is supposed to be on and see if it has a listed price
		String price = event.getLine(price_line-1);
		
		// If the $ sign is included in this line, ignore it
		price = price.replace("$", "");
		
		// Parse the number here. If it's not a number, do not count this as a buy sign
		double parsed_price = Util.stringToDouble(price);
		if(parsed_price == -1) return;
		
		// Get the line the item is supposed to be listed on if there is such a line
		Material mat = null;
		if(item_line != 0) {
			
			String item = event.getLine(item_line-1);
			mat = Util.parseItem(item);
			
			// If no item is listed when the config requires as such, do not make this a shopsign
			if(mat == null) return;
		}
		
		// Get the line the quantity is supposed to be listed on if there is such a line
		int q = -1;
		if(quantity_line != 0) {
			
			String quantity = event.getLine(quantity_line-1);
			q = Util.stringToInt(quantity);
			
			// If no quantity is listed when the config requires as such, do not make this a shopsign
			if(q == -1) return;
		}
		
		ShopSignOwner sso = new ShopSignOwner(plugin, (OfflinePlayer) event.getPlayer());
		PrivateShop ss = new PrivateShop(plugin, (Sign)event.getBlock().getState(), 7, sso, event.getLines());
		ss.setSellPrice(parsed_price);
		
		if(mat != null) ss.setItem(mat);
		if(q != -1) ss.setQuantity(q);
		
		for(int i=0; i<4; i++) {
			String line = event.getLine(i);
			line = plugin.getConfig().getString("sign-format.color") + line;
			line = Util.color(line);
			event.setLine(i, line);
		}
		
		ss.register();
		
	}

	private void parsePrivateBuySign(SignChangeEvent event, List<String> buy_format) {
		
		String fline1 = buy_format.get(0);
		String fline2 = buy_format.get(1);
		String fline3 = buy_format.get(2);
		String fline4 = buy_format.get(3);
		
		// Determine which line the %buy% is in the config
		int buy_line;
		
		if(fline1.contains("%buy%")) buy_line = 1;
		else if(fline2.contains("%buy%")) buy_line = 2;
		else if(fline3.contains("%buy%")) buy_line = 3;
		else if(fline4.contains("%buy%")) buy_line = 4;
		else {restoreDefaultSignFormat(10); buy_line = 1;}
		
		// Determine which line the %buy_price% is in the config
		int price_line;
		
		if(fline1.contains("%buy_price%")) price_line = 1;
		else if(fline2.contains("%buy_price%")) price_line = 2;
		else if(fline3.contains("%buy_price%")) price_line = 3;
		else if(fline4.contains("%buy_price%")) price_line = 4;
		else {restoreDefaultSignFormat(11); buy_line = 1; price_line = 4;}
		
		// Determine which line the %item% is in the config if it exists
		int item_line = 0;
		
		if(fline1.contains("%item%")) item_line = 1;
		else if(fline2.contains("%item%")) item_line = 2;
		else if(fline3.contains("%item%")) item_line = 3;
		else if(fline4.contains("%item%")) item_line = 4;
		
		// Determine which line the %quantity% is in the config if it exists
		int quantity_line = 0;
		
		if(fline1.contains("%quantity%")) quantity_line = 1;
		else if(fline2.contains("%quantity%")) quantity_line = 2;
		else if(fline3.contains("%quantity%")) quantity_line = 3;
		else if(fline4.contains("%quantity%")) quantity_line = 4;
		
		// If the sign does not match the configured format, do not make this sign a shopsign
		if(!event.getLine(buy_line-1).equals(plugin.getConfig().getString("sign-format.keywords.private-shop") + 
				plugin.getConfig().getString("sign-format.keywords.buy"))) return;
		
		// Get the line the price is supposed to be on and see if it has a listed price
		String price = event.getLine(price_line-1);
		
		// If the $ sign is included in this line, ignore it
		price = price.replace("$", "");
		
		// Parse the number here. If it's not a number, do not count this as a buy sign
		double parsed_price = Util.stringToDouble(price);
		if(parsed_price == -1) return;
		
		// Get the line the item is supposed to be listed on if there is such a line
		Material mat = null;
		if(item_line != 0) {
			
			String item = event.getLine(item_line-1);
			mat = Util.parseItem(item);
			
			// If no item is listed when the config requires as such, do not make this a shopsign
			if(mat == null) return;
		}
		
		// Get the line the quantity is supposed to be listed on if there is such a line
		int q = -1;
		if(quantity_line != 0) {
			
			String quantity = event.getLine(quantity_line-1);
			q = Util.stringToInt(quantity);
			
			// If no quantity is listed when the config requires as such, do not make this a shopsign
			if(q == -1) return;
		}
		
		ShopSignOwner sso = new ShopSignOwner(plugin, (OfflinePlayer) event.getPlayer());
		PrivateShop ss = new PrivateShop(plugin, (Sign)event.getBlock().getState(), 6, sso, event.getLines());
		ss.setBuyPrice(parsed_price);
		
		if(mat != null) ss.setItem(mat);
		if(q != -1) ss.setQuantity(q);
		
		// Change the color of the sign to let the user know they have set it up properly
		for(int i=0; i<4; i++) {
			String line = event.getLine(i);
			line = plugin.getConfig().getString("sign-format.color", "&b") + line;
			line = Util.color(line);
			event.setLine(i, line);
		}
		
		ss.register();
		
	}

	private void parseAdminBuySellSign(SignChangeEvent event, List<String> buy_sell_format) {
		
		String fline1 = buy_sell_format.get(0);
		String fline2 = buy_sell_format.get(1);
		String fline3 = buy_sell_format.get(2);
		String fline4 = buy_sell_format.get(3);
		
		// Determine which line the %buy_sell% is in the config
		int buy_sell_line;
				
		if(fline1.contains("%buy_sell%")) buy_sell_line = 1;
		else if(fline2.contains("%buy_sell%")) buy_sell_line = 2;
		else if(fline3.contains("%buy_sell%")) buy_sell_line = 3;
		else if(fline4.contains("%buy_sell%")) buy_sell_line = 4;
		else {restoreDefaultSignFormat(14); buy_sell_line = 1;}
				
		// Determine which line the %buy_sell_price% is in the config
		int price_line;
				
		if(fline1.contains("%buy_sell_price%")) price_line = 1;
		else if(fline2.contains("%buy_sell_price%")) price_line = 2;
		else if(fline3.contains("%buy_sell_price%")) price_line = 3;
		else if(fline4.contains("%buy_sell_price%")) price_line = 4;
		else {restoreDefaultSignFormat(15); buy_sell_line = 1; price_line = 4;}
				
		// Determine which line the %item% is in the config if it exists
		int item_line = 0;
				
		if(fline1.contains("%item%")) item_line = 1;
		else if(fline2.contains("%item%")) item_line = 2;
		else if(fline3.contains("%item%")) item_line = 3;
		else if(fline4.contains("%item%")) item_line = 4;
				
		// Determine which line the %quantity% is in the config if it exists
		int quantity_line = 0;
				
		if(fline1.contains("%quantity%")) quantity_line = 1;
		else if(fline2.contains("%quantity%")) quantity_line = 2;
		else if(fline3.contains("%quantity%")) quantity_line = 3;
		else if(fline4.contains("%quantity%")) quantity_line = 4;
		
		// If the sign does not match the configured format, do not make this sign a shopsign
		if(!event.getLine(buy_sell_line-1).equals(plugin.getConfig().getString("sign-format.keywords.admin-shop") + 
				plugin.getConfig().getString("sign-format.keywords.buy-sell"))) return;
		
		String buy_sell_price = plugin.getConfig().getString("sign-format.keywords.buy-sell-price", "%buy_price% : %sell_price%");
		
		boolean buybeforesell = true;
		double parsed_buy_price = -1;
		double parsed_sell_price = -1;
		
		if(buy_sell_price.indexOf("%sell_price%") < buy_sell_price.indexOf("%buy_price%")) buybeforesell = false;
		
		String buy_sell = event.getLine(price_line-1);
		buy_sell = buy_sell.replace("$", "");
		
		// If the %buy_price% is before %sell_price% in the config, parse %buy_price% first, else parse %sell_price% first
		if(buybeforesell) {
			
			int b = buy_sell_price.indexOf("%buy_price%");
			
			// If the syntax of the configured sign format does not match, then do not make this a sign shop
			if(!buy_sell.substring(0,b).equals(buy_sell_price.substring(0,b))) {
				Bukkit.getServer().broadcastMessage(buy_sell.substring(0,b) + " : " + buy_sell_price.substring(0,b));
				return;
			}
			
			String p1 = buy_sell.substring(b);
			
			// Measure the length of the number
			int i = 0;
			
			while(p1.charAt(i) == '1' || p1.charAt(i) == '2' || p1.charAt(i) == '3' ||
					p1.charAt(i) == '4' || p1.charAt(i) == '5' || p1.charAt(i) == '6' ||
					p1.charAt(i) == '7' || p1.charAt(i) == '8' || p1.charAt(i) == '9' ||
					p1.charAt(i) == '.') {i++;}
			
			String p2 = p1.substring(0, i);
			parsed_buy_price = Util.stringToDouble(p2);
			
			p1 = p1.substring(i);
			String in_between = buy_sell_price.substring(b+11,buy_sell_price.indexOf("%sell_price%"));
			
			// If the syntax of the configured sign format between the %buy_price% and %sell_price% does not match, then
			// do not make this a sign shop
			if(!p1.substring(0,in_between.length()).equals(in_between)) {
				Bukkit.getServer().broadcastMessage(p1.substring(0,in_between.length()) + " : " + in_between);
				return;
			}
			
			p1 = p1.substring(in_between.length());
			
			// Determine the length of the number
			i = 0;
			
			while(p1.charAt(i) == '1' || p1.charAt(i) == '2' || p1.charAt(i) == '3' ||
					p1.charAt(i) == '4' || p1.charAt(i) == '5' || p1.charAt(i) == '6' ||
					p1.charAt(i) == '7' || p1.charAt(i) == '8' || p1.charAt(i) == '9' ||
					p1.charAt(i) == '.') {i++; if(i==p1.length()) break;}
			
			parsed_sell_price = Util.stringToDouble(p1.substring(0,i));
			
			p1 = p1.substring(i);
			p2 = buy_sell_price.substring(buy_sell_price.indexOf("%sell_price%")+12);
			
			// If the configured format does not match the rest of the line, do not make this a sign shop
			if(!p1.equals(p2)) {
				Bukkit.getServer().broadcastMessage(p1 + " : " + p2);
				return;
			}
		}
		else {
			
			int b = buy_sell_price.indexOf("%sell_price%");
			
			// If the syntax of the configured sign format does not match, then do not make this a sign shop
			if(!buy_sell.substring(0,b).equals(buy_sell_price.substring(0,b))) {
				Bukkit.getServer().broadcastMessage(buy_sell.substring(0,b) + " : " + buy_sell_price.substring(0,b));
				return;
			}
			
			String p1 = buy_sell.substring(b);
			
			// Measure the length of the number
			int i = 0;
			
			while(p1.charAt(i) == '1' || p1.charAt(i) == '2' || p1.charAt(i) == '3' ||
					p1.charAt(i) == '4' || p1.charAt(i) == '5' || p1.charAt(i) == '6' ||
					p1.charAt(i) == '7' || p1.charAt(i) == '8' || p1.charAt(i) == '9' ||
					p1.charAt(i) == '.') {i++;}
			
			String p2 = p1.substring(0, i);
			parsed_sell_price = Util.stringToDouble(p2);
			
			p1 = p1.substring(i);
			String in_between = buy_sell_price.substring(b+12,buy_sell_price.indexOf("%buy_price%"));
			
			// If the syntax of the configured sign format between the %buy_price% and %sell_price% does not match, then
			// do not make this a sign shop
			if(!p1.substring(0,in_between.length()).equals(in_between)) {
				Bukkit.getServer().broadcastMessage(p1.substring(0,in_between.length()) + " : " + in_between);
				return;
			}
			
			p1 = p1.substring(in_between.length());
			
			// Determine the length of the number
			i = 0;
			
			while(p1.charAt(i) == '1' || p1.charAt(i) == '2' || p1.charAt(i) == '3' ||
					p1.charAt(i) == '4' || p1.charAt(i) == '5' || p1.charAt(i) == '6' ||
					p1.charAt(i) == '7' || p1.charAt(i) == '8' || p1.charAt(i) == '9' ||
					p1.charAt(i) == '.') {i++; if(i==p1.length()) break;}
			
			parsed_buy_price = Util.stringToDouble(p1.substring(0,i));
			
			p1 = p1.substring(i);
			p2 = buy_sell_price.substring(buy_sell_price.indexOf("%buy_price%")+11);
			
			// If the configured format does not match the rest of the line, do not make this a sign shop
			if(!p1.equals(p2)) {
				Bukkit.getServer().broadcastMessage(p1 + " : " + p2);
				return;
			}
		}
		
		// Get the line the item is supposed to be listed on if there is such a line
		Material mat = null;
		if(item_line != 0) {
			
			String item = event.getLine(item_line-1);
			mat = Util.parseItem(item);
			
			// If no item is listed when the config requires as such, do not make this a shopsign
			if(mat == null) return;
		}
		
		// Get the line the quantity is supposed to be listed on if there is such a line
		int q = -1;
		if(quantity_line != 0) {
			
			String quantity = event.getLine(quantity_line-1);
			q = Util.stringToInt(quantity);
			
			// If no quantity is listed when the config requires as such, do not make this a shopsign
			if(q == -1) return;
		}
		
		ShopSignOwner sso = new ShopSignOwner(plugin, (OfflinePlayer) event.getPlayer());
		AdminShop ss = new AdminShop(plugin, (Sign)event.getBlock().getState(), 5, sso, event.getLines());
		ss.setBuyPrice(parsed_buy_price);
		ss.setSellPrice(parsed_sell_price);
		
		if(mat != null) ss.setItem(mat);
		if(q != -1) ss.setQuantity(q);
		
		for(int i=0; i<4; i++) {
			String line = event.getLine(i);
			line = plugin.getConfig().getString("sign-format.color") + line;
			line = Util.color(line);
			event.setLine(i, line);
		}
		
		ss.register();
		
	}

	private void parseAdminSellSign(SignChangeEvent event, List<String> sell_format) {
		
		String fline1 = sell_format.get(0);
		String fline2 = sell_format.get(1);
		String fline3 = sell_format.get(2);
		String fline4 = sell_format.get(3);
		
		// Determine which line the %buy% is in the config
		int sell_line;
				
		if(fline1.contains("%sell%")) sell_line = 1;
		else if(fline2.contains("%sell%")) sell_line = 2;
		else if(fline3.contains("%sell%")) sell_line = 3;
		else if(fline4.contains("%sell%")) sell_line = 4;
		else {restoreDefaultSignFormat(12); sell_line = 1;}
				
		// Determine which line the %sell_price% is in the config
		int price_line;
				
		if(fline1.contains("%sell_price%")) price_line = 1;
		else if(fline2.contains("%sell_price%")) price_line = 2;
		else if(fline3.contains("%sell_price%")) price_line = 3;
		else if(fline4.contains("%sell_price%")) price_line = 4;
		else {restoreDefaultSignFormat(13); sell_line = 1; price_line = 4;}
				
		// Determine which line the %item% is in the config if it exists
		int item_line = 0;
				
		if(fline1.contains("%item%")) item_line = 1;
		else if(fline2.contains("%item%")) item_line = 2;
		else if(fline3.contains("%item%")) item_line = 3;
		else if(fline4.contains("%item%")) item_line = 4;
				
		// Determine which line the %quantity% is in the config if it exists
		int quantity_line = 0;
				
		if(fline1.contains("%quantity%")) quantity_line = 1;
		else if(fline2.contains("%quantity%")) quantity_line = 2;
		else if(fline3.contains("%quantity%")) quantity_line = 3;
		else if(fline4.contains("%quantity%")) quantity_line = 4;	
		
		// If the sign does not match the configured format, do not make this sign a shopsign
		if(!event.getLine(sell_line-1).equals(plugin.getConfig().getString("sign-format.keywords.admin-shop") + 
				plugin.getConfig().getString("sign-format.keywords.sell"))) return;
		
		// Get the line the price is supposed to be on and see if it has a listed price
		String price = event.getLine(price_line-1);
		
		// If the $ sign is included in this line, ignore it
		price = price.replace("$", "");
		
		// Parse the number here. If it's not a number, do not count this as a buy sign
		double parsed_price = Util.stringToDouble(price);
		if(parsed_price == -1) return;
		
		// Get the line the item is supposed to be listed on if there is such a line
		Material mat = null;
		if(item_line != 0) {
			
			String item = event.getLine(item_line-1);
			mat = Util.parseItem(item);
			
			// If no item is listed when the config requires as such, do not make this a shopsign
			if(mat == null) return;
		}
		
		// Get the line the quantity is supposed to be listed on if there is such a line
		int q = -1;
		if(quantity_line != 0) {
			
			String quantity = event.getLine(quantity_line-1);
			q = Util.stringToInt(quantity);
			
			// If no quantity is listed when the config requires as such, do not make this a shopsign
			if(q == -1) return;
		}
		
		ShopSignOwner sso = new ShopSignOwner(plugin, (OfflinePlayer) event.getPlayer());
		AdminShop ss = new AdminShop(plugin, (Sign)event.getBlock().getState(), 4, sso, event.getLines());
		ss.setSellPrice(parsed_price);
		
		if(mat != null) ss.setItem(mat);
		if(q != -1) ss.setQuantity(q);
		
		for(int i=0; i<4; i++) {
			String line = event.getLine(i);
			line = plugin.getConfig().getString("sign-format.color") + line;
			line = Util.color(line);
			event.setLine(i, line);
		}
		
		ss.register();
		
	}

	private void parseAdminBuySign(SignChangeEvent event, List<String> buy_format) {
		
		String fline1 = buy_format.get(0);
		String fline2 = buy_format.get(1);
		String fline3 = buy_format.get(2);
		String fline4 = buy_format.get(3);
		
		// Determine which line the %buy% is in the config
		int buy_line;
		
		if(fline1.contains("%buy%")) buy_line = 1;
		else if(fline2.contains("%buy%")) buy_line = 2;
		else if(fline3.contains("%buy%")) buy_line = 3;
		else if(fline4.contains("%buy%")) buy_line = 4;
		else {restoreDefaultSignFormat(10); buy_line = 1;}
		
		// Determine which line the %buy_price% is in the config
		int price_line;
		
		if(fline1.contains("%buy_price%")) price_line = 1;
		else if(fline2.contains("%buy_price%")) price_line = 2;
		else if(fline3.contains("%buy_price%")) price_line = 3;
		else if(fline4.contains("%buy_price%")) price_line = 4;
		else {restoreDefaultSignFormat(11); buy_line = 1; price_line = 4;}
		
		// Determine which line the %item% is in the config if it exists
		int item_line = 0;
		
		if(fline1.contains("%item%")) item_line = 1;
		else if(fline2.contains("%item%")) item_line = 2;
		else if(fline3.contains("%item%")) item_line = 3;
		else if(fline4.contains("%item%")) item_line = 4;
		
		// Determine which line the %quantity% is in the config if it exists
		int quantity_line = 0;
		
		if(fline1.contains("%quantity%")) quantity_line = 1;
		else if(fline2.contains("%quantity%")) quantity_line = 2;
		else if(fline3.contains("%quantity%")) quantity_line = 3;
		else if(fline4.contains("%quantity%")) quantity_line = 4;
		
		// If the sign does not match the configured format, do not make this sign a shopsign
		if(!event.getLine(buy_line-1).equals(plugin.getConfig().getString("sign-format.keywords.admin-shop") + 
				plugin.getConfig().getString("sign-format.keywords.buy"))) return;
		
		// Get the line the price is supposed to be on and see if it has a listed price
		String price = event.getLine(price_line-1);
		
		// If the $ sign is included in this line, ignore it
		price = price.replace("$", "");
		
		// Parse the number here. If it's not a number, do not count this as a buy sign
		double parsed_price = Util.stringToDouble(price);
		if(parsed_price == -1) return;
		
		// Get the line the item is supposed to be listed on if there is such a line
		Material mat = null;
		if(item_line != 0) {
			
			String item = event.getLine(item_line-1);
			mat = Util.parseItem(item);
			
			// If no item is listed when the config requires as such, do not make this a shopsign
			if(mat == null) return;
		}
		
		// Get the line the quantity is supposed to be listed on if there is such a line
		int q = -1;
		if(quantity_line != 0) {
			
			String quantity = event.getLine(quantity_line-1);
			q = Util.stringToInt(quantity);
			
			// If no quantity is listed when the config requires as such, do not make this a shopsign
			if(q == -1) return;
		}
		
		ShopSignOwner sso = new ShopSignOwner(plugin, (OfflinePlayer) event.getPlayer());
		AdminShop ss = new AdminShop(plugin, (Sign)event.getBlock().getState(), 3, sso, event.getLines());
		ss.setBuyPrice(parsed_price);
		
		if(mat != null) ss.setItem(mat);
		if(q != -1) ss.setQuantity(q);
		
		// Change the color of the sign to let the user know they have set it up properly
		for(int i=0; i<4; i++) {
			String line = event.getLine(i);
			line = plugin.getConfig().getString("sign-format.color", "&b") + line;
			line = Util.color(line);
			event.setLine(i, line);
		}
		
		ss.register();
	}

	private void parseBuySign(SignChangeEvent event, List<String> buy_format) {
		
		String fline1 = buy_format.get(0);
		String fline2 = buy_format.get(1);
		String fline3 = buy_format.get(2);
		String fline4 = buy_format.get(3);
		
		// Determine which line the %buy% is in the config
		int buy_line;
		
		if(fline1.contains("%buy%")) buy_line = 1;
		else if(fline2.contains("%buy%")) buy_line = 2;
		else if(fline3.contains("%buy%")) buy_line = 3;
		else if(fline4.contains("%buy%")) buy_line = 4;
		else {restoreDefaultSignFormat(10); buy_line = 1;}
		
		// Determine which line the %buy_price% is in the config
		int price_line;
		
		if(fline1.contains("%buy_price%")) price_line = 1;
		else if(fline2.contains("%buy_price%")) price_line = 2;
		else if(fline3.contains("%buy_price%")) price_line = 3;
		else if(fline4.contains("%buy_price%")) price_line = 4;
		else {restoreDefaultSignFormat(11); buy_line = 1; price_line = 4;}
		
		// Determine which line the %item% is in the config if it exists
		int item_line = 0;
		
		if(fline1.contains("%item%")) item_line = 1;
		else if(fline2.contains("%item%")) item_line = 2;
		else if(fline3.contains("%item%")) item_line = 3;
		else if(fline4.contains("%item%")) item_line = 4;
		
		// Determine which line the %quantity% is in the config if it exists
		int quantity_line = 0;
		
		if(fline1.contains("%quantity%")) quantity_line = 1;
		else if(fline2.contains("%quantity%")) quantity_line = 2;
		else if(fline3.contains("%quantity%")) quantity_line = 3;
		else if(fline4.contains("%quantity%")) quantity_line = 4;
		
		// If the sign does not match the configured format, do not make this sign a shopsign
		if(!event.getLine(buy_line-1).equals(plugin.getConfig().getString("sign-format.keywords.buy"))) return;
		
		// Get the line the price is supposed to be on and see if it has a listed price
		String price = event.getLine(price_line-1);
		
		// If the $ sign is included in this line, ignore it
		price = price.replace("$", "");
		
		// Parse the number here. If it's not a number, do not count this as a buy sign
		double parsed_price = Util.stringToDouble(price);
		if(parsed_price == -1) return;
		
		// Get the line the item is supposed to be listed on if there is such a line
		Material mat = null;
		if(item_line != 0) {
			
			String item = event.getLine(item_line-1);
			mat = Util.parseItem(item);
			
			// If no item is listed when the config requires as such, do not make this a shopsign
			if(mat == null) return;
		}
		
		// Get the line the quantity is supposed to be listed on if there is such a line
		int q = -1;
		if(quantity_line != 0) {
			
			String quantity = event.getLine(quantity_line-1);
			q = Util.stringToInt(quantity);
			
			// If no quantity is listed when the config requires as such, do not make this a shopsign
			if(q == -1) return;
		}
		
		ShopSignOwner sso = new ShopSignOwner(plugin, (OfflinePlayer) event.getPlayer());
		ShopSign ss = new ShopSign(plugin, (Sign)event.getBlock().getState(), 0, sso, event.getLines());
		ss.setBuyPrice(parsed_price);
		
		if(mat != null) ss.setItem(mat);
		if(q != -1) ss.setQuantity(q);
		
		// Change the color of the sign to let the user know they have set it up properly
		for(int i=0; i<4; i++) {
			String line = event.getLine(i);
			line = plugin.getConfig().getString("sign-format.color") + line;
			line = Util.color(line);
			event.setLine(i, line);
		}
		
		ss.register();
	}
	
	private void parseSellSign(SignChangeEvent event, List<String> sell_format) {
		
		String fline1 = sell_format.get(0);
		String fline2 = sell_format.get(1);
		String fline3 = sell_format.get(2);
		String fline4 = sell_format.get(3);
		
		// Determine which line the %buy% is in the config
		int sell_line;
				
		if(fline1.contains("%sell%")) sell_line = 1;
		else if(fline2.contains("%sell%")) sell_line = 2;
		else if(fline3.contains("%sell%")) sell_line = 3;
		else if(fline4.contains("%sell%")) sell_line = 4;
		else {restoreDefaultSignFormat(12); sell_line = 1;}
				
		// Determine which line the %sell_price% is in the config
		int price_line;
				
		if(fline1.contains("%sell_price%")) price_line = 1;
		else if(fline2.contains("%sell_price%")) price_line = 2;
		else if(fline3.contains("%sell_price%")) price_line = 3;
		else if(fline4.contains("%sell_price%")) price_line = 4;
		else {restoreDefaultSignFormat(13); sell_line = 1; price_line = 4;}
				
		// Determine which line the %item% is in the config if it exists
		int item_line = 0;
				
		if(fline1.contains("%item%")) item_line = 1;
		else if(fline2.contains("%item%")) item_line = 2;
		else if(fline3.contains("%item%")) item_line = 3;
		else if(fline4.contains("%item%")) item_line = 4;
				
		// Determine which line the %quantity% is in the config if it exists
		int quantity_line = 0;
				
		if(fline1.contains("%quantity%")) quantity_line = 1;
		else if(fline2.contains("%quantity%")) quantity_line = 2;
		else if(fline3.contains("%quantity%")) quantity_line = 3;
		else if(fline4.contains("%quantity%")) quantity_line = 4;	
		
		// If the sign does not match the configured format, do not make this sign a shopsign
		if(!event.getLine(sell_line-1).equals(plugin.getConfig().getString("sign-format.keywords.sell"))) return;
		
		// Get the line the price is supposed to be on and see if it has a listed price
		String price = event.getLine(price_line-1);
		
		// If the $ sign is included in this line, ignore it
		price = price.replace("$", "");
		
		// Parse the number here. If it's not a number, do not count this as a buy sign
		double parsed_price = Util.stringToDouble(price);
		if(parsed_price == -1) return;
		
		// Get the line the item is supposed to be listed on if there is such a line
		Material mat = null;
		if(item_line != 0) {
			
			String item = event.getLine(item_line-1);
			mat = Util.parseItem(item);
			
			// If no item is listed when the config requires as such, do not make this a shopsign
			if(mat == null) return;
		}
		
		// Get the line the quantity is supposed to be listed on if there is such a line
		int q = -1;
		if(quantity_line != 0) {
			
			String quantity = event.getLine(quantity_line-1);
			q = Util.stringToInt(quantity);
			
			// If no quantity is listed when the config requires as such, do not make this a shopsign
			if(q == -1) return;
		}
		
		ShopSignOwner sso = new ShopSignOwner(plugin, (OfflinePlayer) event.getPlayer());
		ShopSign ss = new ShopSign(plugin, (Sign)event.getBlock().getState(), 1, sso, event.getLines());
		ss.setSellPrice(parsed_price);
		
		if(mat != null) ss.setItem(mat);
		if(q != -1) ss.setQuantity(q);
		
		for(int i=0; i<4; i++) {
			String line = event.getLine(i);
			line = plugin.getConfig().getString("sign-format.color") + line;
			line = Util.color(line);
			event.setLine(i, line);
		}
		
		ss.register();
	}
	
	private void parseBuySellSign(SignChangeEvent event, List<String> buy_sell_format) {
		
		String fline1 = buy_sell_format.get(0);
		String fline2 = buy_sell_format.get(1);
		String fline3 = buy_sell_format.get(2);
		String fline4 = buy_sell_format.get(3);
		
		// Determine which line the %buy_sell% is in the config
		int buy_sell_line;
				
		if(fline1.contains("%buy_sell%")) buy_sell_line = 1;
		else if(fline2.contains("%buy_sell%")) buy_sell_line = 2;
		else if(fline3.contains("%buy_sell%")) buy_sell_line = 3;
		else if(fline4.contains("%buy_sell%")) buy_sell_line = 4;
		else {restoreDefaultSignFormat(14); buy_sell_line = 1;}
				
		// Determine which line the %buy_sell_price% is in the config
		int price_line;
				
		if(fline1.contains("%buy_sell_price%")) price_line = 1;
		else if(fline2.contains("%buy_sell_price%")) price_line = 2;
		else if(fline3.contains("%buy_sell_price%")) price_line = 3;
		else if(fline4.contains("%buy_sell_price%")) price_line = 4;
		else {restoreDefaultSignFormat(15); buy_sell_line = 1; price_line = 4;}
				
		// Determine which line the %item% is in the config if it exists
		int item_line = 0;
				
		if(fline1.contains("%item%")) item_line = 1;
		else if(fline2.contains("%item%")) item_line = 2;
		else if(fline3.contains("%item%")) item_line = 3;
		else if(fline4.contains("%item%")) item_line = 4;
				
		// Determine which line the %quantity% is in the config if it exists
		int quantity_line = 0;
				
		if(fline1.contains("%quantity%")) quantity_line = 1;
		else if(fline2.contains("%quantity%")) quantity_line = 2;
		else if(fline3.contains("%quantity%")) quantity_line = 3;
		else if(fline4.contains("%quantity%")) quantity_line = 4;
		
		// If the sign does not match the configured format, do not make this sign a shopsign
		if(!event.getLine(buy_sell_line-1).equals(plugin.getConfig().getString("sign-format.keywords.buy-sell"))) return;
		
		String buy_sell_price = plugin.getConfig().getString("sign-format.keywords.buy-sell-price", "%buy_price% : %sell_price%");
		
		boolean buybeforesell = true;
		double parsed_buy_price = -1;
		double parsed_sell_price = -1;
		
		if(buy_sell_price.indexOf("%sell_price%") < buy_sell_price.indexOf("%buy_price%")) buybeforesell = false;
		
		String buy_sell = event.getLine(price_line-1);
		buy_sell = buy_sell.replace("$", "");
		
		// If the %buy_price% is before %sell_price% in the config, parse %buy_price% first, else parse %sell_price% first
		if(buybeforesell) {
			
			int b = buy_sell_price.indexOf("%buy_price%");
			
			// If the syntax of the configured sign format does not match, then do not make this a sign shop
			if(!buy_sell.substring(0,b).equals(buy_sell_price.substring(0,b))) {
				Bukkit.getServer().broadcastMessage(buy_sell.substring(0,b) + " : " + buy_sell_price.substring(0,b));
				return;
			}
			
			String p1 = buy_sell.substring(b);
			
			// Measure the length of the number
			int i = 0;
			
			while(p1.charAt(i) == '1' || p1.charAt(i) == '2' || p1.charAt(i) == '3' ||
					p1.charAt(i) == '4' || p1.charAt(i) == '5' || p1.charAt(i) == '6' ||
					p1.charAt(i) == '7' || p1.charAt(i) == '8' || p1.charAt(i) == '9' ||
					p1.charAt(i) == '.') {i++;}
			
			String p2 = p1.substring(0, i);
			parsed_buy_price = Util.stringToDouble(p2);
			
			p1 = p1.substring(i);
			String in_between = buy_sell_price.substring(b+11,buy_sell_price.indexOf("%sell_price%"));
			
			// If the syntax of the configured sign format between the %buy_price% and %sell_price% does not match, then
			// do not make this a sign shop
			if(!p1.substring(0,in_between.length()).equals(in_between)) {
				Bukkit.getServer().broadcastMessage(p1.substring(0,in_between.length()) + " : " + in_between);
				return;
			}
			
			p1 = p1.substring(in_between.length());
			
			// Determine the length of the number
			i = 0;
			
			while(p1.charAt(i) == '1' || p1.charAt(i) == '2' || p1.charAt(i) == '3' ||
					p1.charAt(i) == '4' || p1.charAt(i) == '5' || p1.charAt(i) == '6' ||
					p1.charAt(i) == '7' || p1.charAt(i) == '8' || p1.charAt(i) == '9' ||
					p1.charAt(i) == '.') {i++; if(i==p1.length()) break;}
			
			parsed_sell_price = Util.stringToDouble(p1.substring(0,i));
			
			p1 = p1.substring(i);
			p2 = buy_sell_price.substring(buy_sell_price.indexOf("%sell_price%")+12);
			
			// If the configured format does not match the rest of the line, do not make this a sign shop
			if(!p1.equals(p2)) {
				Bukkit.getServer().broadcastMessage(p1 + " : " + p2);
				return;
			}
		}
		else {
			
			int b = buy_sell_price.indexOf("%sell_price%");
			
			// If the syntax of the configured sign format does not match, then do not make this a sign shop
			if(!buy_sell.substring(0,b).equals(buy_sell_price.substring(0,b))) {
				Bukkit.getServer().broadcastMessage(buy_sell.substring(0,b) + " : " + buy_sell_price.substring(0,b));
				return;
			}
			
			String p1 = buy_sell.substring(b);
			
			// Measure the length of the number
			int i = 0;
			
			while(p1.charAt(i) == '1' || p1.charAt(i) == '2' || p1.charAt(i) == '3' ||
					p1.charAt(i) == '4' || p1.charAt(i) == '5' || p1.charAt(i) == '6' ||
					p1.charAt(i) == '7' || p1.charAt(i) == '8' || p1.charAt(i) == '9' ||
					p1.charAt(i) == '.') {i++;}
			
			String p2 = p1.substring(0, i);
			parsed_sell_price = Util.stringToDouble(p2);
			
			p1 = p1.substring(i);
			String in_between = buy_sell_price.substring(b+12,buy_sell_price.indexOf("%buy_price%"));
			
			// If the syntax of the configured sign format between the %buy_price% and %sell_price% does not match, then
			// do not make this a sign shop
			if(!p1.substring(0,in_between.length()).equals(in_between)) {
				Bukkit.getServer().broadcastMessage(p1.substring(0,in_between.length()) + " : " + in_between);
				return;
			}
			
			p1 = p1.substring(in_between.length());
			
			// Determine the length of the number
			i = 0;
			
			while(p1.charAt(i) == '1' || p1.charAt(i) == '2' || p1.charAt(i) == '3' ||
					p1.charAt(i) == '4' || p1.charAt(i) == '5' || p1.charAt(i) == '6' ||
					p1.charAt(i) == '7' || p1.charAt(i) == '8' || p1.charAt(i) == '9' ||
					p1.charAt(i) == '.') {i++; if(i==p1.length()) break;}
			
			parsed_buy_price = Util.stringToDouble(p1.substring(0,i));
			
			p1 = p1.substring(i);
			p2 = buy_sell_price.substring(buy_sell_price.indexOf("%buy_price%")+11);
			
			// If the configured format does not match the rest of the line, do not make this a sign shop
			if(!p1.equals(p2)) {
				Bukkit.getServer().broadcastMessage(p1 + " : " + p2);
				return;
			}
		}
		
		// Get the line the item is supposed to be listed on if there is such a line
		Material mat = null;
		if(item_line != 0) {
			
			String item = event.getLine(item_line-1);
			mat = Util.parseItem(item);
			
			// If no item is listed when the config requires as such, do not make this a shopsign
			if(mat == null) return;
		}
		
		// Get the line the quantity is supposed to be listed on if there is such a line
		int q = -1;
		if(quantity_line != 0) {
			
			String quantity = event.getLine(quantity_line-1);
			q = Util.stringToInt(quantity);
			
			// If no quantity is listed when the config requires as such, do not make this a shopsign
			if(q == -1) return;
		}
		
		ShopSignOwner sso = new ShopSignOwner(plugin, (OfflinePlayer) event.getPlayer());
		ShopSign ss = new ShopSign(plugin, (Sign)event.getBlock().getState(), 0, sso, event.getLines());
		ss.setBuyPrice(parsed_buy_price);
		ss.setSellPrice(parsed_sell_price);
		
		if(mat != null) ss.setItem(mat);
		if(q != -1) ss.setQuantity(q);
		
		for(int i=0; i<4; i++) {
			String line = event.getLine(i);
			line = plugin.getConfig().getString("sign-format.color") + line;
			line = Util.color(line);
			event.setLine(i, line);
		}
		
		ss.register();
	}
}