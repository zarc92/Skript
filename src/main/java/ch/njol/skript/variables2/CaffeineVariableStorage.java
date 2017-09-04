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

package ch.njol.skript.variables2;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;

import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;

/**
 * Variable storage implementation based on Caffeine library.
 */
public class CaffeineVariableStorage implements VariableStorage {
	
	private LoadingCache<String, Object> cache;
	
	class Loader implements CacheLoader<String, Object> {

		@Override
		public Object load(String key) throws Exception {
			// TODO Auto-generated method stub
			return null;
		}

		
	}
	
	public CaffeineVariableStorage(long maxSize) {
		cache = Caffeine.newBuilder()
				.maximumSize(maxSize)
				.executor(new ForkJoinPool()) // Use separate pool to not starve common one
				.build(new Loader());
	}
	
	@Override
	@Nullable
	public Object getVariable(String name, @Nullable Event e, boolean local) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setVariable(String name, @Nullable Event e, boolean local, @Nullable Object value) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void flush() {
		// TODO Auto-generated method stub
		
	}
	
}
