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

package ch.njol.skript.lang.parser;

/**
 * Something that can be parsed
 */
public interface PatternElement {
	
	public static final int NO_MATCH = -1;
	
	/**
	 * Checks if the line, starting from given position, matches with this
	 * pattern element. If it does, the end of match should be returned.
	 * If it does not, {@link NO_MATCH} should be returned.
	 * @param line Line of text.
	 * @param start Start in the line.
	 * @return Where match ends or {@link NO_MATCH}.
	 */
	int matches(String line, int start);
}
