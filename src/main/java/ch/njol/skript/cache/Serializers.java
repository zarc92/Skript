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

import java.io.File;
import java.lang.reflect.Type;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

/**
 * Contains various GSON serializers for script caching.
 */
@NonNullByDefault(value = false)
public class Serializers {
	
	public static class FileSerializer implements JsonSerializer<File> {

		@Override
		public JsonElement serialize(File src, Type typeOfSrc, JsonSerializationContext context) {
			return new JsonPrimitive(src.getAbsolutePath());
		}
		
	}
	
	public static class FileDeserializer implements JsonDeserializer<File> {

		@Override
		public File deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
			return new File(json.getAsString());
		}
		
	}
}
