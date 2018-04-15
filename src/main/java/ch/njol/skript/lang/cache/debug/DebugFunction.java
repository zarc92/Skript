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
import ch.njol.skript.lang.cache.BitCode;
import ch.njol.skript.lang.cache.ParsedFunction;

public class DebugFunction implements ParsedFunction {

	@Override
	public void name(String name) {
		Skript.info("Name: " + name);
	}

	@Override
	public void argument(String name, Class<?> type, boolean single, boolean optional) {
		Skript.info("Argument: " + name + ", " + type + ", single: " + single + ", optional: " + optional);
	}

	@Override
	public BitCode trigger() {
		Skript.info("Trigger");
		return new DebugBitCode();
	}
	
}
