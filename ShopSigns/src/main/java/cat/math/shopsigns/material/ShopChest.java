package cat.math.shopsigns.material;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
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
		
		for(int i=contents.length-1;i>=0;i--) {
			
			ItemStack is = contents[i];
			if(is.getType().equals(ss.getItem())) {
				
				if(amount > is.getAmount()) {
					amount = amount - is.getAmount();
					is.setAmount(0);
				}
				
				else {
					
					int a = is.getAmount()-amount;
					is.setAmount(a);
					break;
				}
			}
		}
		
		inv.setContents(contents);
	}
	
	public void addToChest(int amount) {
		
		Inventory inv = getInventory();
		ItemStack[] contents = inv.getContents();
		Material item = ss.getItem();
		int index = 0;
		
		for(ItemStack is : contents) {
			if(is == null) {				
				if(amount > item.getMaxStackSize()) {
					
					is = new ItemStack(item, item.getMaxStackSize());
					contents[index] = is;
					amount = amount - item.getMaxStackSize();
					continue;
				}
				
				else {
					
					is = new ItemStack(item, amount);
					contents[index] = is;
					break;
				}
			}
			
			if(is.getType().equals(item)) {
				
				if(is.getAmount() == is.getMaxStackSize()) continue;
				
				if(amount > item.getMaxStackSize()-is.getAmount()) {
					
					int space = item.getMaxStackSize()-is.getAmount();
					is = new ItemStack(item, item.getMaxStackSize());
					contents[index] = is;
					amount = amount - space;
					continue;
				}
				
				else {
					
					is = new ItemStack(item, is.getAmount()+amount);
					contents[index] = is;
					break;
				}
			}
			index++;
		}
		
		inv.setContents(contents);
	}
	
	public Location getLocation() {return location;}
}