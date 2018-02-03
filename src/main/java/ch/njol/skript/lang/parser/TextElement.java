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

public class TextElement implements PatternElement {
	
	private String text;
	
	public TextElement(String text) {
		this.text = text;
	}
	
	@Override
	public int matches(String line, int start) {
		int found = line.indexOf(text, start);
		if (found == -1) // Does not match
			return NO_MATCH;
		
		// If there are spaces littered there, we can match
		if (start != found) { // Other characters, not so much
			for (int i = start; i < found; i++) {
				if (line.charAt(i) != ' ') {
					return NO_MATCH;
				}
			}
		}
		return found + text.length();
	}
	
}
