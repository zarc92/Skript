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
package ch.njol.skript.lang.cache.debug;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.cache.ParsedCommand;
import ch.njol.skript.lang.cache.ParsedFunction;
import ch.njol.skript.lang.cache.ParsedScript;
import ch.njol.skript.lang.cache.ParsedTrigger;

public class DebugScript implements ParsedScript {

	@Override
	public ParsedCommand command() {
		Skript.info("Command");
		return new DebugCommand();
	}

	@Override
	public ParsedTrigger trigger() {
		Skript.info("Trigger");
		return new DebugTrigger();
	}

	@Override
	public ParsedFunction function() {
		Skript.info("Function");
		return new DebugFunction();
	}
	
}
