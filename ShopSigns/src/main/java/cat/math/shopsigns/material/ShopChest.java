package cat.math.shopsigns.material;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import cat.math.shopsigns.Util;

import org.bukkit.block.Container;

public class ShopChest {
	
	Location location;
	ShopSign ss;
	
	public ShopChest(Location location, ShopSign ss) {
		this.location = location;
		this.ss = ss;
	}
	
	public Inventory getInventory() {
		
		Block b = location.getBlock();
		
		if(!(b.getState() instanceof Container)) return null;
		
		Inventory inv = ((Container)b.getState()).getInventory();
		
		return inv;
	}
	
	public int getSpace() {
		
		Inventory inv = getInventory();		
		ItemStack[] contents = inv.getContents();		
		Material m = ss.getItem();
		
		int space = 0;
		
		for(ItemStack is : contents) {
			if(is == null) {
				
				space += m.getMaxStackSize();
				continue;
			}
			
			if(is.getType().equals(m)) {
				
				space += m.getMaxStackSize()-is.getAmount();
				continue;
			}
		}
		
		return space;
	}
	
	public int getTotalSpace() {
		
		Inventory inv = getInventory();
		ItemStack[] contents = inv.getContents();
		Material m = ss.getItem();
		
		int space = 0;
		
		for(ItemStack is : contents) {
			if(is == null || is.getType().equals(m)) {
				space += m.getMaxStackSize();
			}
		}
		
		return space;
	}
	
	public int getNumberOfItems() {
		
		Inventory inv = getInventory();		
		ItemStack[] contents = inv.getContents();		
		Material m = ss.getItem();
		
		int num = 0;
		
		for(ItemStack is : contents) {
			if(is != null && is.getType().equals(m)) {
				num += is.getAmount();
			}
		}
		
		return num;
	}
	
	public void removeFromChest(int amount) {
		
		Inventory inv = getInventory();
		ItemStack[] contents = inv.getContents();
		Material mat = ss.getItem();
		ItemStack removing = new ItemStack(mat);
		removing.setItemMeta(ss.getMeta());
		
		for(int i=0; i<contents.length; i++) {
			
			ItemStack is = contents[i];
			
			if(is == null) continue;
			
			if(!is.isSimilar(removing)) continue;
			
			if(amount >= is.getAmount()) {
				
				amount -= is.getAmount();
				is = null;
				contents[i] = is;
			}
			
			else {
				
				is.setAmount(is.getAmount()-amount);
				amount = 0;
				contents[i] = is;
				break;
			}
			
		}
		
		inv.setContents(contents);
	}
	
	public void addToChest(int amount) {
		
		Inventory inv = getInventory();
		ItemStack[] contents = inv.getContents();
		Material item = ss.getItem();
		ItemStack adding = new ItemStack(ss.getItem());
		adding.setItemMeta(ss.getMeta());
		
		for(int i=0; i<contents.length; i++) {
			
			ItemStack is = contents[i];
			
			if(is == null) {
				
				if(amount > adding.getMaxStackSize()) {
					
					is = new ItemStack(ss.getItem(), adding.getMaxStackSize());
					is.setItemMeta(ss.getMeta());
					amount -= adding.getMaxStackSize();
					contents[i] = is;
					continue;
				}
				
				else {
					
					is = new ItemStack(ss.getItem(), amount);
					is.setItemMeta(ss.getMeta());
					amount = 0;
					contents[i] = is;
					break;
				}
			}
			
			if(!is.isSimilar(adding)) continue;
			
			if(is.getAmount() == adding.getMaxStackSize()) continue;
			
			int space = adding.getMaxStackSize() - is.getAmount();
			
			if(amount > space) {
				
				is.setAmount(adding.getMaxStackSize());
				amount -= space;
				contents[i] = is;
			}
			
			else  {
				
				is.setAmount(is.getAmount() + amount);
				amount = 0;
				contents[i] = is;
				break;
			}
		}
		
		inv.setContents(contents);
	}
	
	public Location getLocation() {return location;}
}