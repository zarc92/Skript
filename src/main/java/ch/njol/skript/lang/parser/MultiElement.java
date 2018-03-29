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

import java.util.ArrayList;
import java.util.List;

/**
 * Represents an element that contains many parts.
 */
public class MultiElement implements PatternElement {
	
	private List<PatternElement> elements;
	
	public MultiElement(List<PatternElement> elements) {
		this.elements = new ArrayList<>(elements);
	}
	
	@Override
	public int matches(String line, int start) {
		for (PatternElement e : elements) {
			int end = e.matches(line, start);
			if (end == NO_MATCH)
				return NO_MATCH;
			start = end;
		}
		return start;
	}
	
}
