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
package ch.njol.skript.lang.util;

import java.util.function.Consumer;

import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.lang.TriggerItem;

/**
 * A trigger item that executes a {@link Consumer} when it is reached.
 *
 * TODO: find a better name for this
 */
public class FunctionalItem extends TriggerItem {

	private Consumer<Event> consumer;
	private String stringForm;

	public FunctionalItem(@NonNull Consumer<Event> consumer) {
		this("functional trigger item", consumer);
	}

	public FunctionalItem(@NonNull String stringForm, @NonNull Consumer<Event> consumer) {
		this.consumer = consumer;
		this.stringForm = stringForm;
	}

	@Override
	protected boolean run(Event e) {
		consumer.accept(e);
		return true;
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return stringForm;
	}

}
