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
package ch.njol.skript.lang;

import java.util.Iterator;
import java.util.List;

import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;

public abstract class Section extends TriggerSection implements SyntaxElement {

	@Nullable
	@SuppressWarnings("unchecked")
	public static Section parse(String s, @Nullable String defaultError) {
		return (Section) SkriptParser.parse(s, (Iterator) Skript.getSections().iterator(), defaultError);
	}

	@Override
	public void setTriggerItems(List<TriggerItem> items) {
		super.setTriggerItems(items);
	}

	/**
	 * Run when the section is reached
	 *
	 * @return whether or not the code within the section should run
	 */
	public abstract boolean execute(Event e);

	@Nullable
	@Override
	public TriggerItem walk(Event e) {
		debug(e, true);
		return walk(e, execute(e));
	}

}
