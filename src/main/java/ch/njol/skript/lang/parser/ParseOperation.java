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

import ch.njol.skript.SkriptAPIException;

/**
 * Represents a parse operation which parsed elements can mutate when they
 * are matched.
 */
public class ParseOperation {
	
	/**
	 * Internal representation of marks as 32-bit unsigned value.
	 */
	private int marks;
	
	/**
	 * Parser responsible for this operation.
	 */
	private SkriptParser parser;
	
	public ParseOperation(SkriptParser parser) {
		this.parser = parser;
	}
	
	/**
	 * Toggles a mark with id from given enum entry. If the mark does not
	 * exist, it is set, and if it does, it is removed.
	 * @param e Enum instance.
	 */
	public void toggleMark(Enum<?> e) {
		int index = e.ordinal();
		if (index > 31) { // Data must fit to an int
			throw new SkriptAPIException("a pattern cannot have more than 32 marks");
		}
		marks ^= 1 << index;
	}
	
	/**
	 * Checks if there is a mark for given enum instance.
	 * @param e Enum instance.
	 * @return Whether a mark exists for it.
	 */
	public boolean hasMark(Enum<?> e) {
		int index = e.ordinal();
		if (index > 31) { // Data must fit to an int
			throw new SkriptAPIException("a pattern cannot have more than 32 marks");
		}
		return (marks >>> index) == 1;
	}
	
	/**
	 * Gets the parser that is responsible for this operation.
	 * @return Responsible parser.
	 */
	public SkriptParser getResponsibleParser() {
		return parser;
	}
	
	public void parse(String code, int parseMode) {
		// TODO return type based on SkriptParser return type when done
		parser.parse(code, parseMode, this);
	}
}
