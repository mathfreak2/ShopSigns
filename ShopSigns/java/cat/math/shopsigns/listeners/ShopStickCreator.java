package cat.math.shopsigns.listeners;

import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import cat.math.shopsigns.ShopSigns;
import cat.math.shopsigns.Util;
import cat.math.shopsigns.material.ShopStick;

public class ShopStickCreator implements Listener {
	
	ShopSigns plugin;
	
	public ShopStickCreator(ShopSigns plugin) {
		this.plugin = plugin;
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onInventoryClick(InventoryClickEvent event) {
		
		if(event.isCancelled()) return;
		
		HumanEntity he = event.getWhoClicked();
		
		if(!(he instanceof Player)) return;
		
		Inventory inv = event.getInventory();
		
		if(!(inv instanceof AnvilInventory)) return;
		
		InventoryView view = event.getView();
		int rawSlot = event.getRawSlot();
		
		if(rawSlot != view.convertSlot(rawSlot)) return;
		if(rawSlot != 2) return;
		
		ItemStack item = event.getCurrentItem();
		if(item == null) return;
		if(!item.getType().equals(Material.STICK)) return;
		
		ItemMeta meta = item.getItemMeta();
		if(meta == null) return;
		
		if(!meta.hasDisplayName()) return;
		String displayName = meta.getDisplayName();
		
		if(displayName.equalsIgnoreCase("Shop Wand")) createShopStick(item, (Player)he);
	}
	
	public void createShopStick(ItemStack item, Player p) {
		
		ItemMeta im = item.getItemMeta();
		im.setDisplayName(ShopStick.name);
		item.setItemMeta(im);
		String message = plugin.getConfig().getString("confirms.make-shopstick");
		message = message.replace("%number%", ""+item.getAmount());
		message = Util.color(message);
		p.sendMessage(message);
	}
}