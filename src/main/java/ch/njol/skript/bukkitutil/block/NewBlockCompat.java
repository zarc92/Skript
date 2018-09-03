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
package ch.njol.skript.bukkitutil.block;

import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.Rotatable;
import org.bukkit.entity.FallingBlock;
import org.bukkit.inventory.ItemStack;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.Aliases;
import ch.njol.skript.aliases.ItemType;
import ch.njol.util.Setter;

/**
 * 1.13+ block compat.
 */
public class NewBlockCompat implements BlockCompat {

	private static class NewBlockValues extends BlockValues {

		Material type;
		BlockData data;
		
		public NewBlockValues(Material type, BlockData data) {
			this.type = type;
			this.data = data;
		}

		@Override
		public boolean equals(@Nullable Object other) {
			if (!(other instanceof NewBlockValues))
				return false;
			NewBlockValues n = (NewBlockValues) other;
			return data.equals(n.data) && type.equals(n.type);
		}

		@SuppressWarnings("null")
		@Override
		public int hashCode() {
			int prime = 31;
			int result = 1;
			result = prime * result + (data == null ? 0 : data.hashCode());
			result = prime * result + type.hashCode();
			return result;
		}
		
	}
	
	private static class NewBlockSetter implements BlockSetter {
		
		private ItemType floorTorch;
		private ItemType wallTorch;
		
		private ItemType specialTorchSides;
		private ItemType specialTorchFloors;
		
		private boolean typesLoaded = false;

		/**
		 * Cached BlockFace values.
		 */
		private BlockFace[] faces = BlockFace.values();
		
		@SuppressWarnings("null") // Late initialization with loadTypes() to avoid circular dependencies
		public NewBlockSetter() {}

		@Override
		public void setBlock(Block block, Material type, @Nullable BlockValues values, int flags) {
			if (!typesLoaded)
				loadTypes();
			
			boolean rotate = (flags | ROTATE) != 0;
			boolean rotateForce = (flags | ROTATE_FORCE) != 0;
			boolean rotateFixType = (flags | ROTATE_FIX_TYPE) != 0;
			boolean applyPhysics = (flags | APPLY_PHYSICS) != 0;
			NewBlockValues ourValues = null;
			if (values != null)
				ourValues = (NewBlockValues) values;
			
			Class<?> dataType = type.data;
			
			/**
			 * Set to true when block is placed. If no special logic places
			 * the block, generic placement will be done.
			 */
			boolean placed = false;
			if (rotate) {
				if (rotateFixType || floorTorch.isOfType(type)) {
					// If floor torch cannot be placed, try a wall torch
					Block under = block.getRelative(0, -1, 0);
					boolean canPlace = true;
					if (!under.getType().isOccluding()) { // Usually cannot be placed, but there are exceptions
						// TODO check for stairs and slabs, currently complicated since there is no 'any' alias
						if (specialTorchFloors.isOfType(under)) {
							canPlace = true;
						} else {
							canPlace = false;
						}
					}
					
					// Can't really place a floor torch, try wall one instead
					if (!canPlace) {
						BlockFace face = findWallTorchSide(block);
						if (face != null) { // Found better torch spot
							block.setType(wallTorch.getMaterial());
							Directional data = (Directional) block.getBlockData();
							data.setFacing(face);
							block.setBlockData(data, applyPhysics);
							placed = true;
						}
					}
				} else if (wallTorch.isOfType(type)) {
					Directional data;
					if (ourValues != null)
						data = (Directional) ourValues.data;
					else
						data = (Directional) Bukkit.createBlockData(type);
					
					Block relative = block.getRelative(data.getFacing());
					if (!relative.getType().isOccluding() && !specialTorchSides.isOfType(relative)) {
						// Attempt to figure out a better rotation
						BlockFace face = findWallTorchSide(block);
						if (face != null) { // Found better torch spot
							block.setType(type);
							data.setFacing(face);
							block.setBlockData(data, applyPhysics);
							placed = true;
						}
					}
				}
			}
			
			// Generic block placement
			if (!placed) {
				block.setType(type);
				if (ourValues != null)
					block.setBlockData(ourValues.data, applyPhysics);
			}
		}
		
		private void loadTypes() {
			floorTorch = Aliases.javaItemType("floor torch");
			wallTorch = Aliases.javaItemType("wall torch");
			
			specialTorchSides = Aliases.javaItemType("special torch sides");
			specialTorchFloors = Aliases.javaItemType("special torch floors");
			
			typesLoaded = true;
		}

		@Nullable
		private BlockFace findWallTorchSide(Block block) {
			for (BlockFace face : faces) {
				assert face != null;
				Block relative = block.getRelative(face);
				if (relative.getType().isOccluding() || specialTorchSides.isOfType(relative))
					return face.getOppositeFace(); // Torch can be rotated towards from this face
			}
			
			return null; // Can't place torch here legally
		}
		
	}
	
	private NewBlockSetter setter = new NewBlockSetter();
	
	@SuppressWarnings("null")
	@Nullable
	@Override
	public BlockValues getBlockValues(BlockState block) {
		// If block doesn't have useful data, data field of type is MaterialData
		if (BlockData.class.isAssignableFrom(block.getType().data))
			return new NewBlockValues(block.getType(), block.getBlockData());
		return null;
	}
	
	@Override
	@Nullable
	public BlockValues getBlockValues(ItemStack stack) {
		Material type = stack.getType();
		if (BlockData.class.isAssignableFrom(type.data)) { // Block has data
			// Create default block data for the type
			return new NewBlockValues(type, Bukkit.createBlockData(type));
		}
		return null;
	}
	
	@Override
	public BlockSetter getSetter() {
		return setter;
	}

	@Override
	public BlockState fallingBlockToState(FallingBlock entity) {
		BlockState state = entity.getWorld().getBlockAt(0, 0, 0).getState();
		state.setBlockData(entity.getBlockData());
		return state;
	}

	@Override
	@Nullable
	public BlockValues createBlockValues(Material type, Map<String, String> states) {
		if (states.isEmpty())
			return null; // Block values not needed
		
		StringBuilder combined = new StringBuilder("[");
		boolean first = true;
		for (Map.Entry<String, String> entry : states.entrySet()) {
			if (first)
				first = false;
			else
				combined.append(',');
			combined.append(entry.getKey()).append('=').append(entry.getValue());
		}
		combined.append(']');
		
		try {
			BlockData data =  Bukkit.createBlockData(type, combined.toString());
			assert data != null;
			return new NewBlockValues(type, data);
		} catch (IllegalArgumentException e) {
			Skript.error("Parsing block state " + combined + " failed!");
			e.printStackTrace();
			return null;
		}	
	}

	@Override
	public boolean isEmpty(Material type) {
		return type == Material.AIR || type == Material.CAVE_AIR || type == Material.VOID_AIR;
	}

	@Override
	public boolean isLiquid(Material type) {
		return type == Material.WATER || type == Material.LAVA;
	}
	
}
