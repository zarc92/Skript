/**
 *   This file is part of Skript.
 *
 *  Skript is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Skript is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 * Copyright 2011-2017 Peter Güttinger and contributors
 */
package ch.njol.skript.aliases;

import java.io.NotSerializableException;
import java.io.StreamCorruptedException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.RandomAccess;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemData.OldItemData;
import ch.njol.skript.bukkitutil.block.BlockValues;
import ch.njol.skript.classes.Serializer;
import ch.njol.skript.lang.Unit;
import ch.njol.skript.localization.Adjective;
import ch.njol.skript.localization.GeneralWords;
import ch.njol.skript.localization.Language;
import ch.njol.skript.localization.Message;
import ch.njol.skript.localization.Noun;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.BlockUtils;
import ch.njol.skript.util.Container;
import ch.njol.skript.util.Container.ContainerType;
import ch.njol.skript.variables.Variables;
import ch.njol.skript.util.EnchantmentType;
import ch.njol.skript.util.Utils;
import ch.njol.util.coll.iterator.EmptyIterable;
import ch.njol.util.coll.iterator.SingleItemIterable;
import ch.njol.yggdrasil.FieldHandler;
import ch.njol.yggdrasil.Fields;
import ch.njol.yggdrasil.Fields.FieldContext;
import ch.njol.yggdrasil.YggdrasilSerializable.YggdrasilExtendedSerializable;

@ContainerType(ItemStack.class)
public class ItemType implements Unit, Iterable<ItemData>, Container<ItemStack>, YggdrasilExtendedSerializable {
	
	static {
		// This handles updating ItemType and ItemData variable records
		Variables.yggdrasil.registerFieldHandler(new FieldHandler() {
			
			@Override
			public boolean missingField(Object o, Field field) throws StreamCorruptedException {
				if (!(o instanceof ItemType || o instanceof ItemData))
					return false;
				if (field.getName().equals("globalMeta"))
					return true; // Just null, no need for updating that data
				return false;
			}
			
			@Override
			public boolean incompatibleField(Object o, Field f, FieldContext field) throws StreamCorruptedException {
				return false;
			}
			
			@Override
			public boolean excessiveField(Object o, FieldContext field) throws StreamCorruptedException {
				if (!(o instanceof ItemType || o instanceof ItemData))
					return false;
				String id = field.getID();
				if (id.equals("meta") || id.equals("enchantments") || id.equals("ignoreMeta") || id.equals("numItems"))
					return true;
				return false;
			}
		});
	}
	
	/**
	 * DO NOT ADD ItemDatas to this list directly!
	 * <p>
	 * This contains all ItemDatas that this ItemType represents. Each of them
	 * can have its own ItemMeta.
	 */
	final ArrayList<ItemData> types = new ArrayList<>(2);
	
	/**
	 * Whether this ItemType represents all types or not.
	 */
	private boolean all = false;
	
	/**
	 * Amount determines how many items this type represents. Negative amounts
	 * are treated as their absolute values when adding items to inventories
	 * and otherwise used as "doesn't matter" flags.
	 */
	private int amount = -1;
	
	/**
	 * ItemTypes to use instead of this one if adding to an inventory or setting a block.
	 */
	@Nullable
	private ItemType item = null, block = null;
	
	/**
	 * Meta that applies for all ItemDatas there.
	 */
	@Nullable
	private ItemMeta globalMeta;
	
	void setItem(final @Nullable ItemType item) {
		if (equals(item)) { // can happen if someone defines a 'x' and 'x item/block' alias that have the same value, e.g. 'dirt' and 'dirt block'
			this.item = null;
		} else {
			if (item != null) {
				if (item.item != null || item.block != null) {
					assert false : this + "; item=" + item + ", item.item=" + item.item + ", item.block=" + item.block;
					this.item = null;
					return;
				}
				item.setAmount(amount);
			}
			this.item = item;
		}
	}
	
	void setBlock(final @Nullable ItemType block) {
		if (equals(block)) {
			this.block = null;
		} else {
			if (block != null) {
				if (block.item != null || block.block != null) {
					assert false : this + "; block=" + block + ", block.item=" + block.item + ", block.block=" + block.block;
					this.block = null;
					return;
				}
				block.setAmount(amount);
			}
			this.block = block;
		}
	}
	
	public ItemType() {}
	
	public ItemType(Material id) {
		add_(new ItemData(id));
	}
	
	public ItemType(Material id, String tags) {
		add_(new ItemData(id, tags));
	}
	
	public ItemType(ItemData d) {
		add_(d.clone());
	}
	
	public ItemType(ItemStack i) {
		amount = i.getAmount();
		add_(new ItemData(i));
	}
	
	public ItemType(BlockState b) {
//		amount = 1;
		add_(new ItemData(b));
		// TODO metadata - spawners, skulls, etc.
	}
	
	/**
	 * Copy constructor.
	 * @param i Another ItemType.
	 */
	private ItemType(ItemType i) {
		setTo(i);
	}
	
	public void setTo(ItemType i) {
		all = i.all;
		amount = i.amount;
		final ItemType bl = i.block, it = i.item;
		block = bl == null ? null : bl.clone();
		item = it == null ? null : it.clone();
		for (final ItemData d : i) {
			types.add(d.clone());
		}
	}

	public ItemType(Block block) {
		this(block.getState());
	}

	/**
	 * Removes the item and block aliases from this alias as it now represents a different item.
	 */
	public void modified() {
		item = block = null;
	}
	
	/**
	 * Returns amount of the item in stack that this type represents.
	 * @return amount.
	 */
	@Override
	public int getAmount() {
		return Math.abs(amount);
	}
	
	/**
	 * Only use this method if you know what you're doing.
	 * 
	 * @return The internal amount, i.e. same as {@link #getAmount()}
	 * or additive inverse number of it.
	 */
	public int getInternalAmount() {
		return amount;
	}
	
	@Override
	public void setAmount(final double amount) {
		setAmount((int) amount);
	}
	
	public void setAmount(final int amount) {
		this.amount = amount;
		if (item != null)
			item.amount = amount;
		if (block != null)
			block.amount = amount;
	}
	
	/**
	 * Checks if this item type represents one of its items (OR) or all of
	 * them (AND). If this has only one item, it doesn't matter.
	 * @return Whether all of the items are represented.
	 */
	public boolean isAll() {
		return all;
	}
	
	public void setAll(final boolean all) {
		this.all = all;
	}
	
	public boolean isOfType(@Nullable ItemStack item) {
		// Duplicate code to avoid creating ItemData
		for (ItemData myType : types) {
			if (item == null) { // Given item null
				if (myType.type == Material.AIR)
					return true; // Both items AIR/null
			} else if (myType.isAlias) {
				return myType.type.equals(item.getType());
			} else {
				return item.isSimilar(myType.stack);
			}
		}
		return false;
		
		// Alternative, simpler implementation
//		if (item == null)
//			return isOfType(Material.AIR, null);
//		return isOfType(new ItemData(item));
	}
	
	public boolean isOfType(@Nullable BlockState block) {
		if (block == null)
			return isOfType(Material.AIR, null);
		
		return isOfType(new ItemData(block));
	}
	
	public boolean isOfType(@Nullable Block block) {
		if (block == null)
			return isOfType(Material.AIR, null);
		return isOfType(block.getState());
	}
	
	public boolean isOfType(ItemData type) {
		for (final ItemData myType : types) {
			if (myType.equals(type))
				return true;
		}
		return false;
	}
	
	public boolean isOfType(Material id, @Nullable String tags) {
		return isOfType(new ItemData(id, tags));
	}
	
	public boolean isOfType(Material id) {
		// TODO avoid object creation
		return isOfType(new ItemData(id, null));
	}
	
	public boolean isSupertypeOf(final ItemType other) {
		if (amount != -1 && other.amount != amount)
			return false;
		outer: for (final ItemData o : other.types) {
			assert o != null;
			for (final ItemData t : types) {
				if (t.isSupertypeOf(o))
					continue outer;
			}
			return false;
		}
		return true;
	}
	
	public ItemType getItem() {
		final ItemType item = this.item;
		return item == null ? this : item;
	}
	
	public ItemType getBlock() {
		final ItemType block = this.block;
		return block == null ? this : block;
	}
	
	/**
	 * @return Whether this ItemType has at least one ItemData that represents an item
	 */
	public boolean hasItem() {
		for (ItemData d : types) {
			if (!d.type.isBlock())
				return true;
		}
		return false;
	}
	
	/**
	 * @return Whether this ItemType has at least one ItemData that represents a block
	 */
	public boolean hasBlock() {
		for (ItemData d : types) {
			if (d.type.isBlock())
				return true;
		}
		return false;
	}
	
	/**
	 * Sets the given block to this ItemType
	 * 
	 * @param block The block to set
	 * @param applyPhysics Whether to run a physics check just after setting the block
	 * @return Whether the block was successfully set
	 */
	public boolean setBlock(Block block, boolean applyPhysics) {
		for (int i = random.nextInt(types.size()); i < types.size(); i++) {
			ItemData d = types.get(i);
			if (!d.type.isBlock()) // Ignore items which cannot be placed
				continue;
			if (BlockUtils.set(block, d, applyPhysics))
				return true;
		}
		return false;
	}
	
	/**
	 * Intersects all ItemDatas with all ItemDatas of the given ItemType, returning an ItemType with at most n*m ItemDatas, where n = #ItemDatas of this ItemType, and m =
	 * #ItemDatas of the argument.
	 * 
	 * @see ItemData#intersection(ItemData)
	 * @param other
	 * @return A new item type which is the intersection of the two item types or null if the intersection is empty.
	 */
	@Nullable
	public ItemType intersection(ItemType other) {
		ItemType r = new ItemType();
		for (ItemData d1 : types) {
			for (ItemData d2 : other.types) {
				assert d2 != null;
				r.add_(d1.intersection(d2));
			}
		}
		if (r.types.isEmpty())
			return null;
		return r;
	}
	
	/**
	 * @param type Some ItemData. Only a copy of it will be stored.
	 */
	public void add(@Nullable ItemData type) {
		if (type != null) {
			add_(type.clone());
		}
	}
	
	/**
	 * @param type A cloned or newly created ItemData
	 */
	private void add_(@Nullable ItemData type) {
		if (type != null) {
			types.add(type);
			//numItems += type.numItems();
			modified();
		}
	}
	
	public void addAll(Collection<ItemData> types) {
		this.types.addAll(types);
		modified();
	}
	
	public void remove(ItemData type) {
		if (types.remove(type)) {
			//numItems -= type.numItems();
			modified();
		}
	}
	
	void remove(int index) {
		ItemData type = types.remove(index);
		//numItems -= type.numItems();
		modified();
	}
	
	@Override
	public Iterator<ItemStack> containerIterator() {
		return new Iterator<ItemStack>() {
			@SuppressWarnings("null")
			Iterator<ItemData> iter = types.iterator();
			
			@Override
			public boolean hasNext() {
				return iter.hasNext();
			}
			
			@Override
			public ItemStack next() {
				if (!hasNext())
					throw new NoSuchElementException();
				ItemStack is = iter.next().getStack().clone();
				is.setAmount(getAmount());
				return is;
			}
			
			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}
	
	/**
	 * Gets all ItemStacks this ItemType represents. Only use this if you know what you're doing, as it returns only one element if this is not an 'every' alias.
	 * 
	 * @return An Iterable whose iterator will always return the same item(s)
	 */
	public Iterable<ItemStack> getAll() {
		if (!isAll()) {
			final ItemStack i = getRandom();
			if (i == null)
				return EmptyIterable.get();
			return new SingleItemIterable<>(i);
		}
		return new Iterable<ItemStack>() {
			@Override
			public Iterator<ItemStack> iterator() {
				return containerIterator();
			}
		};
	}
	
	@Nullable
	public ItemStack removeAll(@Nullable ItemStack item) {
		boolean wasAll = all;
		int oldAmount = amount;
		all = true;
		amount = -1;
		try {
			return removeFrom(item);
		} finally {
			all = wasAll;
			amount = oldAmount;
		}
	}
	
	/**
	 * Removes this type from the item stack if appropriate
	 * 
	 * @param item
	 * @return The passed ItemStack or null if the resulting amount is <= 0
	 */
	@Nullable
	public ItemStack removeFrom(@Nullable ItemStack item) {
		if (item == null) // Cannot remove from null/AIR
			return null;
		if (!isOfType(item)) // Wrong item type
			return item;
		if (all)
			return null;
		int a = item.getAmount() - getAmount();
		if (a <= 0)
			return null;
		item.setAmount(a);
		return item;
	}
	
	/**
	 * Adds this ItemType to the given item stack
	 * 
	 * @param item
	 * @return The passed ItemStack or a new one if the passed is null or air
	 */
	@Nullable
	public ItemStack addTo(final @Nullable ItemStack item) {
		if (item == null || item.getType() == Material.AIR)
			return getRandom();
		if (isOfType(item))
			item.setAmount(Math.min(item.getAmount() + getAmount(), item.getMaxStackSize()));
		return item;
	}
	
	@Override
	public ItemType clone() {
		return new ItemType(this);
	}
	
	private final static Random random = new Random();
	
	/**
	 * @return One random ItemStack that this ItemType represents. If you have a List or an Inventory, use {@link #addTo(Inventory)} or {@link #addTo(List)} respectively.
	 * @see #addTo(Inventory)
	 * @see #addTo(ItemStack)
	 * @see #addTo(ItemStack[])
	 * @see #addTo(List)
	 * @see #removeFrom(Inventory)
	 * @see #removeFrom(ItemStack)
	 * @see #removeFrom(List...)
	 */
	@Nullable
	public ItemStack getRandom() {
		int numItems = types.size();
		int index = random.nextInt(numItems);
		ItemStack is = types.get(index).getStack().clone();
		is.setAmount(getAmount());
		return is;
	}
	
	/**
	 * Test whether this ItemType can be put into the given inventory completely.
	 * <p>
	 * REMIND If this ItemType represents multiple items with OR, this function will immediately return false.<br/>
	 * CondCanHold currently blocks aliases without 'every'/'all' as temporary solution.
	 * 
	 * @param invi
	 * @return Whether this item type can be added to the given inventory
	 */
	public boolean hasSpace(final Inventory invi) {
		if (!isAll()) {
			if (getItem().types.size() != 1)
				return false;
		}
		return addTo(getStorageContents(invi));
	}
	
	public static ItemStack[] getCopiedContents(Inventory invi) {
		final ItemStack[] buf = invi.getContents();
		for (int i = 0; i < buf.length; i++)
			if (buf[i] != null)
				buf[i] = buf[i].clone();
		return buf;
	}
	
	/**
	 * Gets copy of storage contents, i.e. ignores armor and off hand. This is due to Spigot 1.9
	 * added armor slots, and off hand to default inventory index.
	 * @param invi Inventory
	 * @return Copied storage contents
	 */
	public static ItemStack[] getStorageContents(final Inventory invi) {
		if (invi instanceof PlayerInventory) {
			ItemStack[] buf = invi.getContents();
			ItemStack[] tBuf = new ItemStack[36];
			for (int i = 0; i < 36; i++)
				if (buf[i] != null)
					tBuf[i] = buf[i].clone();
			return tBuf;
		} else return getCopiedContents(invi);
	}
	
	/**
	 * @return List of ItemDatas. The returned list is not modifiable, use {@link #add(ItemData)} and {@link #remove(ItemData)} if you need to change the list, or use the
	 *         {@link #iterator()}.
	 */
	@SuppressWarnings("null")
	public List<ItemData> getTypes() {
		return Collections.unmodifiableList(types);
	}
	
	public int numTypes() {
		return types.size();
	}
	
	/**
	 * @return How many different items this item type represents
	 */
	public int numItems() {
		return types.size();
	}
	
	@Override
	public Iterator<ItemData> iterator() {
		return new Iterator<ItemData>() {
			private int next = 0;
			
			@Override
			public boolean hasNext() {
				return next < types.size();
			}
			
			@SuppressWarnings("null")
			@Override
			public ItemData next() {
				if (!hasNext())
					throw new NoSuchElementException();
				return types.get(next++);
			}
			
			@Override
			public void remove() {
				if (next <= 0)
					throw new IllegalStateException();
				ItemType.this.remove(--next);
			}
		};
	}
	
	public boolean isContainedIn(Iterable<ItemStack> items) {
		int amount = getAmount();
		for (final ItemData d : types) {
			int found = 0;
			for (final ItemStack i : items) {
				if (d.isOfType(i)) {
					found += i == null ? 1 : i.getAmount();
					if (found >= amount) {
						if (!all)
							return true;
						break;
					}
				}
			}
			if (all && found < amount)
				return false;
		}
		return all;
	}
	
	public boolean isContainedIn(ItemStack[] list) {
		int amount = getAmount();
		for (final ItemData d : types) {
			int found = 0;
			for (final ItemStack i : list) {
				if (d.isOfType(i)) {
					found += i == null ? 1 : i.getAmount();
					if (found >= amount) {
						if (!all)
							return true;
						break;
					}
				}
			}
			if (all && found < amount)
				return false;
		}
		return all;
	}
	
	public boolean removeAll(Inventory invi) {
		final boolean wasAll = all;
		final int oldAmount = amount;
		all = true;
		amount = -1;
		try {
			return removeFrom(invi);
		} finally {
			all = wasAll;
			amount = oldAmount;
		}
	}
	
	/**
	 * Removes this type from the given inventory. Does not call updateInventory for players.
	 * 
	 * @param invi
	 * @return Whether everything could be removed from the inventory
	 */
	public boolean removeFrom(Inventory invi) {
		ItemStack[] buf = getCopiedContents(invi);
		
		@SuppressWarnings("unchecked")
		final boolean ok = removeFrom(Arrays.asList(buf));
		
		invi.setContents(buf);
		return ok;
	}
	
	@SafeVarargs
	public final boolean removeAll(List<ItemStack>... lists) {
		final boolean wasAll = all;
		final int oldAmount = amount;
		all = true;
		amount = -1;
		try {
			return removeFrom(lists);
		} finally {
			all = wasAll;
			amount = oldAmount;
		}
	}
	
	/**
	 * @param lists The lists to remove this type from. Each list should implement {@link RandomAccess} or this method will be slow.
	 * @return Whether this whole item type could be removed (i.e. returns false if the lists didn't contain this item type completely)
	 */
	@SafeVarargs
	public final boolean removeFrom(final List<ItemStack>... lists) {
		int removed = 0;
		boolean ok = true;
		
		for (final ItemData d : types) {
			if (all)
				removed = 0;
			for (final List<ItemStack> list : lists) {
				if (list == null)
					continue;
				assert list instanceof RandomAccess;
				for (int i = 0; i < list.size(); i++) {
					final ItemStack is = list.get(i);
					if (is != null && d.isOfType(is)) {
						if (all && amount == -1) {
							list.set(i, null);
							removed = 1;
							continue;
						}
						final int toRemove = Math.min(is.getAmount(), getAmount() - removed);
						removed += toRemove;
						if (toRemove == is.getAmount()) {
							list.set(i, null);
						} else {
							is.setAmount(is.getAmount() - toRemove);
						}
						if (removed == getAmount()) {
							if (!all)
								return true;
							break;
						}
					}
				}
			}
			if (all)
				ok &= removed == getAmount();
		}
		
		if (!all)
			return false;
		return ok;
	}
	
	/**
	 * Adds this ItemType to the given list, without filling existing stacks.
	 * 
	 * @param list
	 */
	public void addTo(final List<ItemStack> list) {
		if (!isAll()) {
			list.add(getItem().getRandom());
			return;
		}
		for (final ItemStack is : getItem().getAll())
			list.add(is);
	}
	
	/**
	 * Tries to add this ItemType to the given inventory. Does not call updateInventory for players.
	 * 
	 * @param invi
	 * @return Whether everything could be added to the inventory
	 */
	public boolean addTo(final Inventory invi) {
		// important: don't use inventory.add() - it ignores max stack sizes
		ItemStack[] buf = invi.getContents();
		if (buf == null)
			return false;
		
		ItemStack[] tBuf = buf.clone();
		if (invi instanceof PlayerInventory) {
			buf = new ItemStack[36];
			for(int i = 0; i < 36; ++i) {
				buf[i] = tBuf[i];
			}
		}
		
		final boolean b = addTo(buf);
		
		if (invi instanceof PlayerInventory) {
			buf = Arrays.copyOf(buf, tBuf.length);
			for (int i = tBuf.length - 5; i < tBuf.length; ++i) {
				buf[i] = tBuf[i];
			}
		}
		
		invi.setContents(buf);
		return b;
	}
	
	private static boolean addTo(@Nullable ItemStack is, ItemStack[] buf) {
		if (is == null || is.getType() == Material.AIR)
			return true;
		int added = 0;
		for (int i = 0; i < buf.length; i++) {
			if (Utils.itemStacksEqual(is, buf[i])) {
				final int toAdd = Math.min(buf[i].getMaxStackSize() - buf[i].getAmount(), is.getAmount() - added);
				added += toAdd;
				buf[i].setAmount(buf[i].getAmount() + toAdd);
				if (added == is.getAmount())
					return true;
			}
		}
		for (int i = 0; i < buf.length; i++) {
			if (buf[i] == null) {
				final int toAdd = Math.min(is.getMaxStackSize(), is.getAmount() - added);
				added += toAdd;
				buf[i] = is.clone();
				buf[i].setAmount(toAdd);
				if (added == is.getAmount())
					return true;
			}
		}
		return false;
	}
	
	public boolean addTo(final ItemStack[] buf) {
		if (!isAll()) {
			return addTo(getItem().getRandom(), buf);
		}
		boolean ok = true;
		for (ItemStack is : getItem().getAll()) {
			ok &= addTo(is, buf);
		}
		return ok;
	}
	
	/**
	 * Tests whether a given set of ItemTypes is a subset of another set of ItemTypes.
	 * <p>
	 * This method works differently that normal set operations, as is e.g. returns true if set == {everything}.
	 * 
	 * @param set
	 * @param sub
	 * @return Whether all item types in <tt>sub</tt> have at least one {@link #isSupertypeOf(ItemType) super type} in <tt>set</tt>
	 */
	public static boolean isSubset(final ItemType[] set, final ItemType[] sub) {
		outer: for (final ItemType i : sub) {
			assert i != null;
			for (final ItemType t : set) {
				if (t.isSupertypeOf(i))
					continue outer;
			}
			return false;
		}
		return true;
	}
	
	@Override
	public boolean equals(final @Nullable Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof ItemType))
			return false;
		final ItemType other = (ItemType) obj;
		if (all != other.all)
			return false;
		if (amount != other.amount)
			return false;
		if (!types.equals(other.types))
			return false;
		return true;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (all ? 1231 : 1237);
		result = prime * result + amount;
		result = prime * result + types.hashCode();
		return result;
	}
	
	@Override
	public String toString() {
		return toString(false, 0, null);
	}
	
	@Override
	public String toString(final int flags) {
		return toString(false, flags, null);
	}
	
	public String toString(final int flags, final @Nullable Adjective a) {
		return toString(false, flags, a);
	}
	
	private String toString(final boolean debug, final int flags, final @Nullable Adjective a) {
		final StringBuilder b = new StringBuilder();
//		if (types.size() == 1 && !types.get(0).hasDataRange()) {
//			if (getAmount() != 1)
//				b.append(amount + " ");
//			if (isAll())
//				b.append(getAmount() == 1 ? "every " : "of every ");
//		} else {
//			if (getAmount() != 1)
//				b.append(amount + " of ");
//			b.append(isAll() ? "every " : "any ");
//		}
		final boolean plural = amount != 1 && amount != -1 || (flags & Language.F_PLURAL) != 0;
		if (amount != -1 && amount != 1) {
			b.append(amount + " ");
		} else {
			b.append(Noun.getArticleWithSpace(types.get(0).getGender(), flags));
		}
		if (a != null)
			b.append(a.toString(types.get(0).getGender(), flags));
		for (int i = 0; i < types.size(); i++) {
			if (i != 0) {// this belongs here as size-1 can be 0
				if (i == types.size() - 1)
					b.append(" " + (isAll() ? GeneralWords.and : GeneralWords.or) + " ");
				else
					b.append(", ");
			}
			b.append(types.get(i).toString(debug, plural));
		}
//		final Map<Enchantment, Integer> enchs = enchantments;
//		if (enchs == null)
//			return "" + b.toString();
//		b.append(Language.getSpaced("enchantments.of"));
//		int i = 0;
//		for (final Entry<Enchantment, Integer> e : enchs.entrySet()) {
//			if (i != 0) {
//				if (i != enchs.size() - 1)
//					b.append(", ");
//				else
//					b.append(" " + GeneralWords.and + " ");
//			}
//			final Enchantment ench = e.getKey();
//			if (ench == null)
//				continue;
//			b.append(EnchantmentType.toString(ench));
//			b.append(" ");
//			b.append(e.getValue());
//			i++;
//		}
//		if (meta != null) {
//			final ItemMeta m = (ItemMeta) meta;
//			if (m.hasDisplayName()) {
//				b.append(" " + m_named.toString() + " ");
//				b.append("\"" + m.getDisplayName() + "\"");
//			}
//			if (debug)
//				b.append(" meta=[").append(meta).append("]");
//		}
		return "" + b.toString();
	}
	
	public static String toString(final ItemStack i) {
		return new ItemType(i).toString();
	}
	
	public static String toString(final ItemStack i, final int flags) {
		return new ItemType(i).toString(flags);
	}
	
	public static String toString(Block b, int flags) {
		return new ItemType(b).toString(flags);
	}
	
	public String getDebugMessage() {
		return toString(true, 0, null);
	}
	
	@Override
	public Fields serialize() throws NotSerializableException {
		final Fields f = new Fields(this);
		return f;
	}
	
	@Override
	public void deserialize(final Fields fields) throws StreamCorruptedException, NotSerializableException {
		fields.setFields(this);
		
		// Legacy data (before aliases rework) update
		if (!types.isEmpty()) {
			@SuppressWarnings("rawtypes")
			ArrayList noGenerics = types;
			if (noGenerics.get(0).getClass().equals(OldItemData.class)) { // Sorry generics :)
				for (int i = 0; i < types.size(); i++) {
					OldItemData old = (OldItemData) (Object) types.get(i); // Grab and hack together OldItemData
					// TODO update for 1.13
					ItemData data = new ItemData(Material.values()[old.typeid]); // Create new ItemData based on it
					types.set(i, data); // Replace old with new
					// TODO support for data values
				}
			}
		}
	}
	
	/**
	 * Gets raw item names ("minecraft:some_item"). If they are not available,
	 * empty list will be returned.
	 * @return names List of names that could be retrieved.
	 */
	public List<String> getRawNames() {
		List<String> rawNames = new ArrayList<>();
		for (ItemData data : types) {
			assert data != null;
			String id = Aliases.getMinecraftId(data);
			if (id != null)
				rawNames.add(id);
		}
		
		return rawNames;
	}
	
	/**
	 * Gets all enchantments of this item.
	 * @return Enchantments.
	 * @deprecated Use {@link ItemType#getEnchantmentTypes()}
	 */
	@Deprecated
	@Nullable
	public Map<Enchantment,Integer> getEnchantments() {
		if (globalMeta == null)
			return null;
		assert globalMeta != null;
		Map<Enchantment,Integer> enchants = globalMeta.getEnchants();
		if (enchants.isEmpty())
			return null;
		return enchants;
	}
	
	/**
	 * Adds enchantments to this item type.
	 * @param enchantments Enchantments.
	 * @deprecated Use {@link ItemType#addEnchantments(EnchantmentType...)}
	 */
	@Deprecated
	public void addEnchantments(Map<Enchantment,Integer> enchantments) {
		if (globalMeta == null)
			globalMeta = ItemData.itemFactory.getItemMeta(Material.STONE);
		for (Map.Entry<Enchantment,Integer> entry : enchantments.entrySet()) {
			assert globalMeta != null;
			globalMeta.addEnchant(entry.getKey(), entry.getValue(), true);
		}
	}
	
	/**
	 * Gets all enchantments of this item.
	 * @return the enchantments of this item type.
	 */
	@Nullable
	public EnchantmentType[] getEnchantmentTypes() {
		if (globalMeta == null)
			return null;
		
		assert globalMeta != null;
		Set<Entry<Enchantment, Integer>> enchants = globalMeta.getEnchants().entrySet();
		
		return enchants.stream()
			.map(enchant -> new EnchantmentType(enchant.getKey(), enchant.getValue()))
			.toArray(EnchantmentType[]::new);
	}
	
	/**
	 * Checks whether this item type has enchantments.
	 */
	public boolean hasEnchantments() {
		if (globalMeta == null)
			return false;
		
		assert globalMeta != null;
		return globalMeta.hasEnchants();
	}
	
	/**
	 * Checks whether this item type has the given enchantments.
	 * @param enchantments the enchantments to be checked.
	 */
	public boolean hasEnchantments(Enchantment... enchantments) {
		if (!hasEnchantments())
			return false;
		
		for (Enchantment enchantment : enchantments) {
			assert globalMeta != null;
			if (!globalMeta.hasEnchant(enchantment))
				return false;
		}
		return true;
	}
	
	/**
	 * Checks wether this item type contains at most one of the given enchantments.
	 * @param enchantments the enchantments to be checked.
	 */
	public boolean hasAnyEnchantments(Enchantment... enchantments) {
		if (!hasEnchantments())
			return false;
		
		for (Enchantment enchantment : enchantments) {
			assert globalMeta != null;
			if (globalMeta.hasEnchant(enchantment))
				return true;
		}
		return false;
	}
	
	/**
	 * Checks wether this item type contains the given enchantments.
	 * @param enchantments the enchantments to be checked.
	 */
	public boolean hasEnchantments(EnchantmentType... enchantments) {
		if (!hasEnchantments())
			return false;
		
		for (EnchantmentType enchantment : enchantments) {
			assert globalMeta != null;
			if (!globalMeta.hasEnchant(enchantment.getType()))
				return false;
		}
		return true;
	}
	
	/**
	 * Adds the given enchantments to the item type.
	 * @param enchantments the enchantments to be added.
	 */
	public void addEnchantments(EnchantmentType... enchantments) {
		if (globalMeta == null)
			globalMeta = ItemData.itemFactory.getItemMeta(Material.STONE);
		
		for (EnchantmentType enchantment : enchantments) {
			assert globalMeta != null;
			globalMeta.addEnchant(enchantment.getType(), enchantment.getLevel(), true);
		}
	}
	
	/**
	 * Removes the given enchantments from this item type.
	 * @param enchantments the enchantments to be removed.
	 */
	public void removeEnchantments(EnchantmentType... enchantments) {
		if (globalMeta == null)
			return;
		
		for (EnchantmentType enchantment : enchantments) {
			assert globalMeta != null;
			globalMeta.removeEnchant(enchantment.getType());
		}
	}
	
	/**
	 * Clears all enchantments from this item type except the ones that are
	 * defined for individual item datas only.
	 */
	public void clearEnchantments() {
		if (globalMeta == null)
			return; // No enchantments
		assert globalMeta != null;
		Set<Enchantment> enchants = globalMeta.getEnchants().keySet();
		for (Enchantment ench : enchants) {
			assert globalMeta != null;
			globalMeta.removeEnchant(ench);
		}
	}
	
	/**
	 * Gets item meta that applies to this type if it exists.
	 * @return Item meta or null.
	 */
	@Nullable
	public ItemMeta getItemMeta() {
		return globalMeta;
	}

	/**
	 * Sets item meta that is applied for everything this type represents.
	 * Note that previous item meta is overridden if it exists.
	 * @param meta New item meta.
	 */
	public void setItemMeta(ItemMeta meta) {
		globalMeta = meta;
		
		// Apply new meta to all datas
		for (ItemData data : types) {
			data.applyMeta(meta);
		}
	}
	
	/**
	 * Clears item meta from this type. Metas which individual item dates may
	 * have will not be touched.
	 */
	public void clearItemMeta() {
		globalMeta = null;
	}

	public Material getMaterial() {
		ItemData data = types.get(0);
		if (data == null)
			throw new IllegalStateException("material not found");
		return data.getType();
	}
}
