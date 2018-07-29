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

import java.util.stream.IntStream;

import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Section;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.registrations.Comparators;
import ch.njol.skript.registrations.SectionBuilder;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.util.Kleenean;

public class SwitchSection extends Section {

	static {
		SectionBuilder.create()
				.name("switch")
				.source(SwitchSection.class)
				.children(CaseSection.class)
				.illegalChildError("A switch section may only contain case sections")
				.patterns("switch %objects%")
				.register();
	}

	@SuppressWarnings("null")
	private Expression<Object> objectsToSwitch;

	@SuppressWarnings("null")
	private Object[] switchedValues;
	private boolean hasMatched;

	@Override
	public boolean execute(Event e) {
		this.switchedValues = objectsToSwitch.getArray(e);
		return switchedValues.length != 0;
	}

	private Object[] getSwitchedValues() {
		return switchedValues;
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
			SectionBuilder.create()
					.name("case")
					.source(CaseSection.class)
					.parents(SwitchSection.class)
					.illegalPlacementError("A case section may only be used inside a switch section")
					.patterns("case %objects%", "default [case]")
					.register();
		}

		@SuppressWarnings("null")
		private Expression<Object> objectsToMatch;
		private boolean isDefaultCase;

		@Nullable
		private SwitchSection parent;

		@Override
		@SuppressWarnings("null")
		public boolean execute(Event e) {
			SwitchSection parent = (SwitchSection) getParent();
			assert parent != null;

			if (isDefaultCase)
				return !parent.hasMatched;

			Object[] objectsToMatch = this.objectsToMatch.getArray(e);
			Object[] switchedValues = parent.getSwitchedValues();

			return parent.hasMatched = (switchedValues.length != 0
					&& switchedValues.length == objectsToMatch.length
					&& IntStream.range(0, switchedValues.length)
						.allMatch(i -> Comparators.areEqual(switchedValues[i], objectsToMatch[i])));
		}

		@Override
		public String toString(@Nullable Event e, boolean debug) {
			return "case " + objectsToMatch.toString(e, debug);
		}

		@Override
		@SuppressWarnings({"unchecked", "null"})
		public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
			isDefaultCase = matchedPattern == 1;
			if (!isDefaultCase)
				objectsToMatch = LiteralUtils.defendExpression(exprs[0]);
			return isDefaultCase || LiteralUtils.canInitSafely(objectsToMatch);
		}

	}

}
