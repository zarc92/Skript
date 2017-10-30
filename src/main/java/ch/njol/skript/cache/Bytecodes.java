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
 * Copyright 2011-2016 Peter Güttinger and contributors
 * 
 */

package ch.njol.skript.cache;

/**
 * Defines "bytecodes" for Skript's internal script data format.
 */
public class Bytecodes {
	
	/**
	 * Initializes syntax element given as parameter with expressions
	 * from stack.
	 */
	public static final int CALL_INIT = 0;
	
	/**
	 * Creates a variable string with parts from stack.
	 */
	public static final int CALL_NEWSTRING = 1;
	
	/**
	 * Pushes the argument to stack.
	 */
	public static final int PUSH = 2;
	
	// very much TODO
}
