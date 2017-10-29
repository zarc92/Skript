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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.StreamCorruptedException;
import java.net.NetworkInterface;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.bukkit.Bukkit;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.Gson;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAPIException;
import me.nallar.whocalled.WhoCalled;

/**
 * Manages Skript's script cache.
 */
public final class ScriptCache {
	
	private static final int META_LENGTH = 24;
	
	private Path cacheDir;
	private Path scriptDir;
	
	private byte[] secrets;
	
	/**
	 * Metadata about Skript's cache.
	 */
	private static class CacheMeta implements Serializable {
		
		public CacheMeta() {}

		/**
		 * When scripts were last cached.
		 */
		Map<String, Long> lastCached = new HashMap<>();
		
		Map<String, Byte> xorKeys = new HashMap<>();
		
		Map<String, Long> xorHashes = new HashMap<>();
		
		Map<String, Long> cacheSizes = new HashMap<>();
	}
	
	private CacheMeta meta;
	
	private Gson gson;
	
	/**
	 * Initializes a new script cache. Only for internal usage.
	 * @throws IOException If something went wrong.
	 */
	public ScriptCache(Path cacheDir, Path scriptDir) throws IOException {
		if (WhoCalled.$.getCallingClass() != ScriptLoader.class) {
			throw new SkriptAPIException("script cache is only meant for internal usage, for now");
		}
		gson = new Gson();
		
		this.scriptDir = scriptDir;
		this.cacheDir = cacheDir;
		
		String secretStr = Skript.getVersion().toString() + Skript.getMinecraftVersion().toString()
				+ NetworkInterface.getByIndex(0).getHardwareAddress();
		this.secrets = secretStr.getBytes(StandardCharsets.UTF_8);
		
		Path metaFile = cacheDir.resolve("meta.bin");
		if (Files.exists(metaFile)) {
			byte[] bytes = Files.readAllBytes(metaFile);
			int secret = 0;
			
			for (int i = 0; i < bytes.length; i++) {
				bytes[i] = (byte) (bytes[i] ^ secrets[secret]);
				secret++;
				if (secret == secrets.length)
					secret = 0;
			}
			
			try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes))) {
				meta = (CacheMeta) ois.readObject();
			} catch (ClassNotFoundException e) {
				throw new RuntimeException(e);
			} catch (StreamCorruptedException | InvalidClassException e) {
				// Secret didn't match or file was just plain corrupted
				meta = new CacheMeta();
				Files.delete(metaFile);
			}
		} else {
			meta = new CacheMeta();
		}
	}
	
	public void saveMeta() throws IOException {
		Path metaFile = cacheDir.resolve("meta.bin");
		Files.createFile(metaFile);
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
			oos.writeObject(meta);
		}
		
		byte[] bytes = bos.toByteArray();
		int secret = 0;
		for (int i = 0; i < bytes.length; i++) {
			bytes[i] = (byte) (bytes[i] ^ secrets[secret]);
			secret++;
			if (secret == secrets.length)
				secret = 0;
		}
		
		try (FileOutputStream fos = new FileOutputStream(metaFile.toFile())) {
			fos.write(bytes);
		}
	}
	
	public void save(CachedScript script, String name, int version) throws IOException {
		String json = script.serialize(gson);
		byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
		
		ByteArrayOutputStream readyStream = new ByteArrayOutputStream();
		try (GZIPOutputStream gzip = new GZIPOutputStream(readyStream)) {
			gzip.write(bytes);
		}
		byte[] ready = readyStream.toByteArray();
		
		Random rng = new Random();
		byte xorKey = (byte) (rng.nextInt(256) - 128);
		long xorHash = 1;
		
		for (int i = 0; i < bytes.length; i++) {
			ready[i] = (byte) (ready[i] ^ xorKey);
			xorHash = 31 * xorHash + ready[i];
		}
		
		// Delete old file if exists
		Files.deleteIfExists(cacheDir.resolve(name));
		
		try (FileOutputStream fos = new FileOutputStream(cacheDir.resolve(name).toFile())) {
			fos.write(ready);
		}
		
		// Save some metadata
		meta.lastCached.put(name, System.currentTimeMillis());
		meta.xorKeys.put(name, xorKey);
		meta.xorHashes.put(name, xorHash);
		meta.cacheSizes.put(name, (long) ready.length);
	}
	
	@Nullable
	public CachedScript load(String name) throws IOException {
		long lastCached = meta.lastCached.get(name);
		long lastModified = Files.getLastModifiedTime(scriptDir.resolve(name)).toMillis();
		
		// Check if cache is still valid
		if (lastCached < lastModified) {
			return null;
		}
		
		// Load cache file from disk to memory
		long cacheSize = Files.size(cacheDir.resolve(name));
		if (cacheSize != meta.cacheSizes.get(name)) { // File has been messed with
			return null;
		}
		
		// Now, use FIS to read data
		byte[] bytes = new byte[(int) cacheSize];
		try (FileInputStream fis = new FileInputStream(scriptDir.resolve(name).toFile())) {
			fis.read(bytes);
		}
		
		byte xorKey = meta.xorKeys.get(name); // Retrieve XOR key to open file
		long xorHash = 1;
		
		// Decrypt and calculate hash
		for (int i = 0; i < bytes.length; i++) {
			bytes[i] = (byte) (bytes[i] ^ xorKey);
			xorHash = 31 * xorHash + bytes[i];
		}
		
		// Hash MUST match, or file won't be used
		if (xorHash != meta.xorHashes.get(name)) {
			return null;
		}
		
		// GZIP decompress
		byte[] ready = new byte[bytes.length];
		try (GZIPInputStream gzip = new GZIPInputStream(new ByteArrayInputStream(bytes))) {
			gzip.read(ready);
		}
		
		CachedScript script = gson.fromJson(new String(ready, StandardCharsets.UTF_8), CachedScript.class);
		return script;
	}
	
}
