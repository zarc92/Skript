/*
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
 * Copyright 2011-2016 Peter Güttinger and contributors
 * 
 */

package ch.njol.skript.cache;

import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;

import ch.njol.skript.ScriptLoader.ParsedEventData;
import ch.njol.skript.command.ScriptCommand;
import ch.njol.skript.lang.function.Function;

/**
 * Cached script.
 */
public class CachedScript {
	
	public List<ScriptCommand> commands;
	
	public List<Function<?>> functions;
	
	public List<ParsedEventData> events;
	
	public CachedScript() {
		commands = new ArrayList<>();
		functions = new ArrayList<>();
		events = new ArrayList<>();
	}
	
	@SuppressWarnings("null")
	public String serialize(Gson gson) {
		return gson.toJson(this);
	}
}
