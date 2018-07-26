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
package ch.njol.skript.expressions;

import java.lang.reflect.Array;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.Variable;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.log.ErrorQuality;
import ch.njol.skript.sections.LoopSection;
import ch.njol.skript.util.Utils;
import ch.njol.util.Kleenean;
import ch.njol.util.Pair;
import ch.njol.util.StringUtils;

/**
 * Used to access a loop's current value.
 */
@Name("Loop value")
@Description("The currently looped value.")
@Examples({"# countdown:",
		"loop 10 times:",
		"	message \"%11 - loop-number%\"",
		"	wait a second",
		"# generate a 10x10 floor made of randomly coloured wool below the player:",
		"loop blocks from the block below the player to the block 10 east of the block below the player:",
		"	loop blocks from the loop-block to the block 10 north of the loop-block:",
		"		set loop-block-2 to any wool"})
@Since("1.0")
public class ExprLoopValue extends SimpleExpression<Object> {

	static {
		Skript.registerExpression(ExprLoopValue.class, Object.class, ExpressionType.SIMPLE, "[the] loop-<.+>");
	}
	
	@SuppressWarnings("null")
	private String loopType;
	
	@SuppressWarnings("null")
	private LoopSection loop;

	private boolean isVariableLoop;

	private boolean isIndex;
	
	@Override
	public boolean init(final Expression<?>[] vars, final int matchedPattern, final Kleenean isDelayed, final ParseResult parser) {
		loopType = parser.regexes.get(0).group();

		final Matcher m = Pattern.compile("^(.+)-(\\d+)$").matcher(loopType);

		int specificLoop = -1;
		if (m.matches()) {
			loopType = "" + m.group(1);
			specificLoop = Utils.parseInt("" + m.group(2));
			if (specificLoop <= 0) {
				Skript.error("The loop number may not be less than one");
				return false;
			}
			specificLoop--; // human readable -> index
		}

		if (ScriptLoader.currentLoops.isEmpty()) {
			Skript.error("loop-" + loopType + " may only be used in loops");
			return false;
		}


		List<LoopSection> matchingLoops = ScriptLoader.currentLoops.stream()
				.filter(l -> l.isLoopOf(loopType))
				.collect(Collectors.toList());

		if (matchingLoops.isEmpty()) {
			Skript.error("There is no loop that matches 'loop-" + loopType + "'", ErrorQuality.SEMANTIC_ERROR);
			return false;
		}

		if (matchingLoops.size() == 1 && specificLoop != -1) {
			Skript.error("There is only one matching loop, use 'loop-" + loopType + "' instead");
			return false;
		}

		if (matchingLoops.size() != 1 && specificLoop == -1) {
			Skript.error("There are multiple loops that match 'loop-" + loopType + "'. " +
					"Specify which loop-" + loopType + " you want like 'loop-" + loopType + "-1'");
			return false;
		}

		try {
			this.loop = matchingLoops.get(specificLoop == -1 ? 0 : specificLoop);
		} catch (IndexOutOfBoundsException e) {
			Skript.error("There is no " + StringUtils.fancyOrderNumber(specificLoop) + " loop");
			return false;
		}


		if (loop.getLoopedExpr() instanceof Variable<?>) {
			isVariableLoop = true;
			isIndex = ((Variable<?>) loop.getLoopedExpr()).isIndexLoop(loopType);
		}

		return true;
	}
	
	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<?> getReturnType() {
		return isIndex ? String.class : loop.getLoopedExpr().getReturnType();
	}
	
	@Override
	@Nullable
	@SuppressWarnings("unchecked")
	protected Object[] get(final Event e) {
		Object[] value = (Object[]) Array.newInstance(getReturnType(), 1);
		Object rawValue = loop.getLoopedObject(e);
		if (isVariableLoop) {
			Pair<String, Object> loopedVar = (Pair<String, Object>) rawValue;
			value[0] = isIndex ? loopedVar.getFirst() : loopedVar.getSecond();
		} else {
			value[0] = rawValue;
		}
		return value;
	}

	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		return "loop-" + loopType;
	}
	
}
