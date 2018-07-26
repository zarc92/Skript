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

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Section;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.skript.lang.Variable;
import ch.njol.skript.lang.util.LyingItem;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.util.Kleenean;

public class LoopSection extends Section implements LyingItem {

	static {
		Skript.registerSection("loop", LoopSection.class, "loop %objects%");
	}

	@SuppressWarnings("null")
	private Expression<Object> loopedExpr;


	@Nullable
	private TriggerItem actualNext;

	private Map<Event, Iterator<?>> iterators = new WeakHashMap<>();
	private Map<Event, Object> values = new WeakHashMap<>();

	@Nullable
	private Iterator<?> getIterator(Event e) {
		Iterator<?> iterator = iterators.get(e);
		if (iterator == null) {
			iterator = loopedExpr instanceof Variable<?> ?
					((Variable<?>) loopedExpr).variablesIterator(e)
					: loopedExpr.iterator(e);
			iterators.put(e, iterator);
		}
		return iterator;
	}

	@Override
	public boolean execute(Event e) {
		if (first == null) // if there's no code in the loop just continue
			return false;
		Iterator<?> iterator = getIterator(e);
		if (iterator == null || !iterator.hasNext()) // if there's nothing to loop just continue
			return false;
		values.put(e, iterator.next());
		return true;
	}

	@Nullable
	public TriggerItem walk(Event e) {
		if (execute(e)) {
			debug(e, true);
			return first;
 		} else {
			debug(e, false);
			iterators.remove(e);
			return actualNext;
		}
	}

	@Override
	@Nullable
	public TriggerItem getTrueNext() {
		return actualNext;
	}

	@Override
	public LoopSection setNext(@Nullable TriggerItem next) {
		actualNext = next;
		return this;
	}

	public boolean isLoopOf(String s) {
		Class<?> type = Classes.getClassFromUserInput(s);
		return (type != null && loopedExpr.getReturnType().isAssignableFrom(type))
						|| loopedExpr.isLoopOf(s);
	}

	/**
	 * Should only be called when the loop is running
	 *
	 * @return the currently looped object ("loop-value")
	 */
	@NonNull
	public Object getLoopedObject(Event e) {
		Object loopedObject = values.get(e);
		if (loopedObject == null)
			throw new IllegalStateException();
		return loopedObject;
	}

	/**
	 * Should only be called after the loop has been init
	 *
	 * @return the loop expression
	 */
	@NonNull
	public Expression<Object> getLoopedExpr() {
		if (loopedExpr == null)
			throw new IllegalStateException(); // something has gone pretty wrong if it was null by this point
		return loopedExpr;
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "loop " + loopedExpr.toString(e, debug);
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		loopedExpr = LiteralUtils.defendExpression(exprs[0]);
		if (loopedExpr.isSingle()) {
			Skript.error("Can't loop " + loopedExpr.toString(null, false) + " because it's only a single value");
			return false;
		}
		super.setNext(this);
		return LiteralUtils.canInitSafely(loopedExpr);
	}

	@Override
	public void beforeParse() {
		ScriptLoader.currentLoops.add(this);
	}

	@Override
	public void afterParse() {
		List<LoopSection> loops = ScriptLoader.currentLoops;
		loops.remove(loops.size() - 1);
	}

}