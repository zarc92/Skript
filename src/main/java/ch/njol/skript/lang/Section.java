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

	public enum Placement {

		/**
		 * This section is allowed anywhere.
		 *
		 * This should only be used if the section does
		 * not have any specific placement needs and even
		 * then it is unlikely you will need this directly
		 * as placement insensitive sections should
		 * be using the shorter overload registration method.
		 */
		ANYWHERE,

		/**
		 * This section is allowed as a direct child to one of
		 * it's allowed parent sections.
		 *
		 * For example:
		 * <code>
		 *     on click:
		 *       switch type of clicked entity:
		 *         case cow: # this is allowed
		 *           case chicken: # this is not allowed as it's direct parent is a case section
		 * </code>
		 */
		DIRECT_CHILD,

		/**
		 * This section is allowed as either a direct or indirect child
		 * to one of it's allowed parent sections.
		 *
		 * For example:
		 * <code>
		 *     on click:
		 *       switch type of clicked entity:
		 *         case cow: # this is allowed
		 *           case chicken: # this is also allowed as it's indirect parent is a switch section
		 * </code>
		 */
		CHILD

	}

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

	/**
	 * Called before a section has it's trigger items parsed
	 */
	public void beforeParse() {

	}

	/**
	 * Called after a section has it's trigger items parsed
	 */
	public void afterParse() {

	}

}
