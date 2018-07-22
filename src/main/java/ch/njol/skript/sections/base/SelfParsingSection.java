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
package ch.njol.skript.sections.base;

import java.util.List;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.lang.Section;
import ch.njol.skript.lang.TriggerItem;

public abstract class SelfParsingSection extends Section {

	/**
	 * Does nothing in order to enforce that a SelfParsingSection must
	 * really be self parsing.
	 */
	public final void setTriggerItems(@Nullable final List<TriggerItem> items) {

	}

}
