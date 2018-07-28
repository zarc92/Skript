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
package ch.njol.skript.effects;

import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.skript.lang.TriggerSection;
import ch.njol.skript.sections.LoopSection;
import ch.njol.skript.sections.WhileSection;
import ch.njol.util.Kleenean;
import ch.njol.util.StringUtils;

@Name("Continue")
@Description("Skips the value currently being looped, moving on to the next value if it exists.")
@Examples("loop all players:\n" +
		"\tif loop-value does not have permission \"moderator\":\n" +
		"\t\tcontinue # filter out non moderators\n" +
		"\tbroadcast \"%loop-player% is a moderator!\" # only moderators get broadcast")
@Since("2.2-dev37")
public class EffContinue extends Effect {

	static {
		Skript.registerEffect(EffContinue.class,
				"continue [loop]",
				"continue loop <\\d+>"
		);
	}

	@SuppressWarnings("null")
	private TriggerSection loop;

	@Override
	protected void execute(Event e) {
		throw new UnsupportedOperationException();
	}

	@Nullable
	@Override
	protected TriggerItem walk(Event e) {
		return loop;
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "continue";
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		List<TriggerSection> loops = ScriptLoader.currentSections.stream()
				.filter(s -> s instanceof LoopSection || s instanceof WhileSection)
				.collect(Collectors.toList());

		if (loops.isEmpty()) {
			Skript.error("Continue may only be used in loops");
			return false;
		}

		if (matchedPattern == 1 && loops.size() == 1) {
			Skript.error("There is only one loop to continue, use 'continue loop' instead");
			return false;
		}

		int specificLoop = loops.size() - 1;
		if (matchedPattern == 1) {
			specificLoop = Integer.parseInt(parseResult.regexes.get(0).group());
			if (specificLoop <= 0) {
				Skript.error("The loop number may not be less than one");
				return false;
			}
			specificLoop--; // human readable -> index
		}

		try {
			loop = loops.get(specificLoop);
		} catch (IndexOutOfBoundsException e) {
			Skript.error("There is no " + StringUtils.fancyOrderNumber(specificLoop) + " loop");
			return false;
		}

		return true;
	}

}
