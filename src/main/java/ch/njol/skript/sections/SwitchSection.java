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
package ch.njol.skript.sections;

import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Comparator;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Section;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.registrations.Comparators;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.util.Kleenean;

public class SwitchSection extends Section {

	static {
		Skript.registerSection("switch", SwitchSection.class, "switch %objects%");
	}

	@SuppressWarnings("null")
	private Expression<Object> objectsToSwitch;

	@SuppressWarnings("null")
	private Object[] values;

	@Override
	public boolean execute(Event e) {
		this.values = objectsToSwitch.getArray(e);
		return true;
	}

	private Object[] getValues() {
		return values;
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "switch " + objectsToSwitch.toString(e, debug);
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		objectsToSwitch = LiteralUtils.defendExpression(exprs[0]);
		return LiteralUtils.canInitSafely(objectsToSwitch);
	}

	public static class CaseSection extends Section {

		static {
			Skript.registerSection("case", CaseSection.class, "case %objects%");
		}

		@SuppressWarnings("null")
		private Expression<Object> objectsToMatch;

		@Override
		public boolean execute(Event e) {
			Object[] objectsToMatch = this.objectsToMatch.getArray(e);
			SwitchSection parent = (SwitchSection) getParent();
			assert parent != null;
			Object[] switchedValues = parent.getValues();
			if (switchedValues.length != objectsToMatch.length)
				return false;
			for (int i = 0; i < switchedValues.length; i++) {
				if (Comparators.compare(switchedValues[i], objectsToMatch[i]) != Comparator.Relation.EQUAL) {
					return false;
				}
			}
			return true;
		}

		@Override
		public String toString(@Nullable Event e, boolean debug) {
			return "case " + objectsToMatch.toString(e, debug);
		}

		@Override
		public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
			objectsToMatch = LiteralUtils.defendExpression(exprs[0]);
			return LiteralUtils.canInitSafely(objectsToMatch);
		}
	}

}
