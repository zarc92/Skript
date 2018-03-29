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
package ch.njol.skript.lang.parser;

import ch.njol.skript.classes.ClassInfo;

/**
 * Represents an element that might
 */
public class VariableElement implements PatternElement {
	
	/**
	 * Types which this element could take.
	 */
	private ClassInfo<?>[] classes;
	
	/**
	 * Plurality of types (with same indices).
	 */
	private boolean[] plurality;
	
	/**
	 * Parse mode. See {@link ParserMagic}.
	 */
	private int parseMode;
	
	public VariableElement(ClassInfo<?>[] classes, boolean[] plurality, int parseMode) {
		this.classes = classes.clone();
		this.plurality = plurality.clone();
		this.parseMode = parseMode;
	}
	
	@Override
	public int matches(String line, int start, ParseOperation op) {
		
		
		return 0;
	}
	
}
