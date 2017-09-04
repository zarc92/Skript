/*
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
 * 
 */

package ch.njol.skript.variables2;

import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Variable storage, version 2. Implementations of this class will handle
 * getting, setting and saving variables.
 */
public interface VariableStorage {
	
	/**
	 * Gets a variable with given name.
	 * @param name Name of variable.
	 * @param e Event associated with the variable.
	 * @param local If it is a local variable or not.
	 * @return Variable or null if not found.
	 */
	@Nullable Object getVariable(String name, @Nullable Event e, boolean local);
	
	/**
	 * Sets a variable with given name.
	 * @param name Name of variable.
	 * @param e Event associated with the variable.
	 * @param local If it is a local variable or not.
	 * @param value New value. Can be null to remove the variable.
	 */
	void setVariable(String name, @Nullable Event e, boolean local, @Nullable Object value);
	
	/**
	 * Flushes all variables to storage from memory. This should be called on
	 * server shutdown, but rarely else.
	 */
	void flush();
}
