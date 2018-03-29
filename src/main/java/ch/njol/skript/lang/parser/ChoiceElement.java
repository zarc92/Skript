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
 * Represents an element which has multiple possibilities, of which only
 * one must be matched for this element to match.
 */
public class ChoiceElement implements PatternElement {
	
	private List<PatternElement> choices;
	
	public ChoiceElement(List<PatternElement> choices) {
		this.choices = new ArrayList<>(choices);
	}
	
	@Override
	public int matches(String line, int start) {
		for (PatternElement choice : choices) {
			int end = choice.matches(line, start);
			if (end != NO_MATCH) { // This choice matched!
				return end;
			}
		}
		
		return NO_MATCH; // No of choices matched
	}
	
}
