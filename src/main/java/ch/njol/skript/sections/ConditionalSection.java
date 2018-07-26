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

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.Skript;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Section;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.skript.lang.util.FunctionalItem;
import ch.njol.skript.lang.util.LyingItem;
import ch.njol.util.Kleenean;

public class ConditionalSection extends Section implements LyingItem {

	public enum ConditionType {
		IF("if"),

		ElSE_IF("else if"),

		ELSE("else");

		private String friendlyName;

		ConditionType(String friendlyName) {
			this.friendlyName = friendlyName;
		}

		public String toString() {
			return friendlyName;
		}

	}

	@Nullable
	private ConditionType type;

	@Nullable
	private Condition condition;

	static {
		Skript.registerSection("condition", ConditionalSection.class,
				"if <.+>",
				"else if <.+>",
				"else"
		);
	}

	@Override
	@SuppressWarnings({"null", "ConstantConditions"}) // ecj will complain the condition may be null when it won't be
	public boolean execute(Event e) {
		if (type == ConditionType.ELSE || condition.check(e)) {
			assert last != null;
			last.setNext(new FunctionalItem(e2 ->  TriggerItem.walkIfNonNull(getTrueNext(), e2)));
			TriggerItem.walkIfNonNull(first, e);
		} else {
			TriggerItem.walkIfNonNull(getNext(), e);
		}
		return false;
	}

	@Nullable
	@Override
	public TriggerItem walk(Event e) {
		execute(e);
		return null;
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		assert type != null;
		switch (type) {
			case IF:
				assert condition != null;
				return "if " + condition.toString(e, debug);
			case ElSE_IF:
				assert condition != null;
				return "else if " + condition.toString(e, debug);
			case ELSE:
				return "else";
		}
		return "conditional";
	}

	/**
	 * @return the next trigger item that isn't part of this conditional
	 */
	@Nullable
	public TriggerItem getTrueNext() {
		TriggerItem next = getNext();
		while (next instanceof ConditionalSection && ((ConditionalSection) next).getType() != ConditionType.IF) {
			next = next.getNext();
		}
		return next;
	}

	@Nullable
	public ConditionType getType() {
		return type;
	}

	/**
	 * @return whether or not this section is accepting elses/else ifs after it
	 */
	private boolean isAcceptingElse() {
		return type == ConditionType.IF || type == ConditionType.ElSE_IF;
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		type = ConditionType.values()[matchedPattern];

		if (type == ConditionType.ELSE || type == ConditionType.ElSE_IF) {
			Section previousSection = ScriptLoader.getPreviousSection();
			if (!(previousSection instanceof ConditionalSection
					&& ((ConditionalSection) previousSection).isAcceptingElse())) {
				Skript.error("An '" + type + "' section must come directly " +
						"after an '" + ConditionType.IF + "' or '" + ConditionType.ElSE_IF + "' section");
				return false;
			}
		}

		if (type == ConditionType.IF || type == ConditionType.ElSE_IF) {
			String rawCond = parseResult.regexes.get(0).group();
			condition = Condition.parse(rawCond, "Can't understand this condition: " + rawCond);
			return condition != null;
		}

		return true;
	}
}
