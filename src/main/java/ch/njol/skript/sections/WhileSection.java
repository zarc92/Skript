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

import java.util.Collections;

import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Section;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.skript.lang.util.LyingItem;
import ch.njol.skript.lang.util.VoidItem;
import ch.njol.util.Kleenean;

public class WhileSection extends Section implements LyingItem {

	static {
		Skript.registerSection("while", WhileSection.class, "while <.+>");
	}

	@Nullable
	private TriggerItem trueNext;

	@Nullable
	private Condition condition;

	@Override
	public boolean execute(Event e) {
		throw new UnsupportedOperationException();
	}

	@Override
	@Nullable
	public TriggerItem walk(Event e) {
		assert condition != null;
		if (condition.check(e)) {
			debug(e, true);
			return first;
		} else {
			debug(e, false);
			return getTrueNext(e);
		}
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		if (condition == null)
			return "while loop";
		assert condition != null;
		return "while" + condition.toString(e, debug);
	}

	@Override
	public WhileSection setNext(@Nullable TriggerItem next) {
		trueNext = next;
		return this;
	}

	@Nullable
	@Override
	public TriggerItem getTrueNext(@Nullable Event e) {
		return trueNext;
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		String rawCond = parseResult.regexes.get(0).group();
		condition = Condition.parse(rawCond, "Can't understand this condition: " + rawCond);
		super.setNext(this);
		return condition != null;
	}

	@Override
	public void afterParse() {
		if (first == null)
			setTriggerItems(Collections.singletonList(new VoidItem()));
	}
}
