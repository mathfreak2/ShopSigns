package cat.math.shopsigns.material;

import java.math.BigDecimal;
import java.util.ArrayList;

import org.bukkit.block.Sign;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.earth2me.essentials.User;

import cat.math.shopsigns.ShopSignOwner;
import cat.math.shopsigns.ShopSigns;
import cat.math.shopsigns.Util;

public class AdminShop extends ShopSign {

	public AdminShop(ShopSigns plugin, Sign sign, int type, ShopSignOwner sso, String[] lines) {
		super(plugin, sign, type, sso, lines);
	}
	
	public AdminShop(ShopSigns plugin, FileConfiguration file) {
		super(plugin, file);
	}
	
	@Override
	public void buy(User buyer) {
		
		if(type == 4 || !buyer.getBase().hasPermission("shopsigns.adminshop-buy")) {
			
			String s = plugin.getConfig().getString("errors.sign-no-buy","&cYou may not buy from that shop.");
			s = Util.color(s);
			buyer.getBase().sendMessage(s);
			return;
		}
		
		if(item == null) {
			 
			 String s = plugin.getConfig().getString("errors.ambiguous-item", "&cNo item was specified, so you could not link the chest to the shop sign.");
			 s = Util.color(s);
			 sso.getPlayer().getPlayer().sendMessage(s);
			 return;
		}
		
		if(quantity == -1) {
			 
			 String s = plugin.getConfig().getString("errors.ambiguous-quantity", "&cNo quantity was specified, so you could not link the chest to the shop sign.");
			 s = Util.color(s);
			 sso.getPlayer().getPlayer().sendMessage(s);
			 return;
		}
		
		// You can't buy from this shop if you don't have enough money
		if(!buyer.canAfford(new BigDecimal(buy_price))) {
			
			String s = plugin.getConfig().getString("errors.buy-no-money", "&cYou do not have enough money.");
			s = Util.color(s);
			buyer.getBase().sendMessage(s);
			return;
		}
		
		// Get the amount of space in the player's inventory
		Inventory pinv = buyer.getBase().getInventory();
		ItemStack[] contents = pinv.getContents();
		int space = 0;
				
		for(ItemStack is : contents) {
			if(is == null) {
				space += item.getMaxStackSize();
				continue;
			}
			if(is.getType().equals(item)) {
				space += item.getMaxStackSize()-is.getAmount();
				continue;
			}
		}
				
		// If there's not a enough space in the player's inventory, do not allow them to buy from this shop
		if(space < quantity) {
					
			String s = plugin.getConfig().getString("errors.buy-no-inventory", "&cYour inventory is full.");
			s = Util.color(s);
			buyer.getBase().sendMessage(s);
			return;
		}
		
		buyer.takeMoney(new BigDecimal(buy_price));
		Inventory inv = buyer.getBase().getInventory();
		
		ArrayList<ItemStack> additems = new ArrayList<ItemStack>();
		int amount = quantity;
		
		while(amount > item.getMaxStackSize()) {
			ItemStack is = new ItemStack(item, item.getMaxStackSize());
			amount = amount - item.getMaxStackSize();
			additems.add(is);
		}
		
		if(amount > 0) {
			ItemStack is = new ItemStack(item, amount);
			additems.add(is);
		}
		
		ItemStack[] items = new ItemStack[additems.size()];
		
		for(int i=0; i<additems.size(); i++) {
			items[i] = additems.get(i);
		}
		
		inv.addItem(items);
		
		String s = plugin.getConfig().getString("confirms.buy-success", "&aYou bought &e%quantity% %item%&a.");
		s = s.replace("%quantity%", ""+quantity);
		s = s.replace("%item%", item.toString());
		s = Util.color(s);
		buyer.getBase().sendMessage(s);
	}
	
	@Override
	public void sell(User seller) {
		
		if(type == 3) {
			
			String s = plugin.getConfig().getString("errors.sign-no-sell","&cYou may not sell to that shop.");
			s = Util.color(s);
			seller.getBase().getPlayer().sendMessage(s);
			return;
		}
		
		if(item == null) {
			 
			 String s = plugin.getConfig().getString("errors.ambiguous-item", "&cNo item was specified, so you could not link the chest to the shop sign.");
			 s = Util.color(s);
			 sso.getPlayer().getPlayer().sendMessage(s);
			 return;
		}
		
		if(quantity == -1) {
			 
			 String s = plugin.getConfig().getString("errors.ambiguous-quantity", "&cNo quantity was specified, so you could not link the chest to the shop sign.");
			 s = Util.color(s);
			 sso.getPlayer().getPlayer().sendMessage(s);
			 return;
		}
		
		Inventory inv = seller.getBase().getInventory();
		
		// If the seller doesn't have the items that would be sold, do not allow them to sell to this shop
		if(!inv.contains(item, quantity)) {
			
			String s = plugin.getConfig().getString("errors.sell-no-inventory", "&cYou do not have enough of the item being sold.");
			s = Util.color(s);
			seller.getBase().sendMessage(s);
			return;
		}
		
		try {
			seller.giveMoney(new BigDecimal(buy_price));
		} catch (Exception e) {
			String s = plugin.getConfig().getString("errors.too-much-money", "&cThat transaction could not go through because one party has too much money.");
			s = Util.color(s);
			seller.getBase().sendMessage(s);
			return;
		}
		
		inv = seller.getBase().getInventory();
		
		ArrayList<ItemStack> additems = new ArrayList<ItemStack>();
		int amount = quantity;
		
		while(amount > item.getMaxStackSize()) {
			ItemStack is = new ItemStack(item, item.getMaxStackSize());
			amount = amount - item.getMaxStackSize();
			additems.add(is);
		}
		
		if(amount > 0) {
			ItemStack is = new ItemStack(item, amount);
			additems.add(is);
		}
		
		ItemStack[] items = new ItemStack[additems.size()];
		
		for(int i=0; i<additems.size(); i++) {
			items[i] = additems.get(i);
		}
		
		inv.removeItem(items);
		
		String s = plugin.getConfig().getString("confirms.sell-success", "&aYou sold &e%quantity% %item%&a.");
		s = s.replace("%quantity%", ""+quantity);
		s = s.replace("%item%", item.toString());
		s = Util.color(s);
		seller.getBase().sendMessage(s);
	}
	
	@Override
	public boolean isConnected() {return false;}
}