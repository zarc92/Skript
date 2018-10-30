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
package ch.njol.skript.aliases;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAddon;
import ch.njol.skript.SkriptCommand;
import ch.njol.skript.SkriptConfig;
import ch.njol.skript.config.Config;
import ch.njol.skript.config.EntryNode;
import ch.njol.skript.config.Node;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.config.validate.SectionValidator;
import ch.njol.skript.localization.ArgsMessage;
import ch.njol.skript.localization.Language;
import ch.njol.skript.localization.Message;
import ch.njol.skript.localization.Noun;
import ch.njol.skript.localization.RegexMessage;
import ch.njol.skript.log.BlockingLogHandler;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.skript.util.EnchantmentType;
import ch.njol.skript.util.PotionEffectUtils;
import ch.njol.skript.util.Utils;
import ch.njol.skript.util.Version;
import ch.njol.util.NonNullPair;
import ch.njol.util.Setter;

@SuppressWarnings("deprecation")
public abstract class Aliases {
		
	private final static AliasesProvider provider = createProvider();
	private final static AliasesParser parser = createParser(provider);
	
	@Nullable
	private static ItemType getAlias_i(final String s) {
		final ItemType t = ScriptLoader.getScriptAliases().get(s);
		if (t != null)
			return t;
		return provider.getAlias(s);
	}
	
	/**
	 * Creates an aliases provider with Skript's default configuration.
	 * @return Aliases provider.
	 */
	private static AliasesProvider createProvider() {
		return new AliasesProvider();
	}
	
	/**
	 * Creates an aliases parser with Skript's default configuration.
	 * @return Aliases parser.
	 */
	private static AliasesParser createParser(AliasesProvider provider) {
		AliasesParser parser = new AliasesParser(provider);
		
		// Register standard conditions
		parser.registerCondition("minecraft version", (str) -> {
			int orNewer = str.indexOf("or newer"); // For example: 1.12 or newer
			if (orNewer != -1) {
				@SuppressWarnings("null")
				Version ver = new Version(str.substring(0, orNewer - 1));
				return Skript.getMinecraftVersion().compareTo(ver) >= 0;
			}
			
			int orOlder = str.indexOf("or older"); // For example: 1.11 or older
			if (orOlder != -1) {
				@SuppressWarnings("null")
				Version ver = new Version(str.substring(0, orOlder - 1));
				return Skript.getMinecraftVersion().compareTo(ver) <= 0;
			}
			
			int to = str.indexOf("to"); // For example: 1.11 to 1.12
			if (to != -1) {
				@SuppressWarnings("null")
				Version first = new Version(str.substring(0, to - 1));
				@SuppressWarnings("null")
				Version second = new Version(str.substring(to + 3));
				Version current = Skript.getMinecraftVersion();
				return current.compareTo(first) >= 0 && current.compareTo(second) <= 0;
			}
			
			return Skript.getMinecraftVersion().equals(new Version(str));
		});
		
		return parser;
	}

	static String itemSingular = "item";
	static String itemPlural = "items";
	@Nullable
	static String itemGender = null;
	static String blockSingular = "block";
	static String blockPlural = "blocks";
	@Nullable
	static String blockGender = null;
	
	// this is not an alias!
	private final static ItemType everything = new ItemType();
	static {
		everything.setAll(true);
		ItemData all = new ItemData(Material.AIR);
		all.isAnything = true;
		everything.add(all);
	}
	
	private final static Message m_missing_aliases = new Message("aliases.missing aliases");
	private final static Message m_empty_string = new Message("aliases.empty string");
	private final static ArgsMessage m_invalid_item_type = new ArgsMessage("aliases.invalid item type");
	private final static ArgsMessage m_loaded_x_aliases_from = new ArgsMessage("aliases.loaded x aliases from");
	private final static ArgsMessage m_loaded_x_aliases = new ArgsMessage("aliases.loaded x aliases");
	private final static Message m_outside_section = new Message("aliases.outside section");
	
	/**
	 * Concatenates parts of an alias's name. This currently 'lowercases' the first character of any part if there's no space in front of it. It also replaces double spaces with a
	 * single one and trims the resulting string.
	 * 
	 * @param parts
	 */
	static String concatenate(final String... parts) {
		assert parts.length >= 2;
		final StringBuilder b = new StringBuilder();
		for (int i = 0; i < parts.length; i++) {
			if (parts[i].isEmpty())
				continue;
			if (b.length() == 0) {
				b.append(parts[i]);
				continue;
			}
			final char c = parts[i].charAt(0);
			if (Character.isUpperCase(c) && b.charAt(b.length() - 1) != ' ') {
				b.append(Character.toLowerCase(c) + parts[i].substring(1));
			} else {
				b.append(parts[i]);
			}
		}
		return "" + b.toString().replace("  ", " ").trim();
	}
	
	@SuppressWarnings("null")
	private final static Pattern numberWordPattern = Pattern.compile("\\d+\\s+.+");
	
	public static final String getMaterialName(ItemData type, boolean plural) {
		MaterialName name = provider.getMaterialName(type.aliasCopy());
		if (name == null) {
			return "" + type.type;
		}
		return name.toString(plural);
	}
	
	/**
	 * @return The ietm's gender or -1 if no name is found
	 */
	public final static int getGender(ItemData item) {
		final MaterialName n = provider.getMaterialName(item);
		if (n != null)
			return n.gender;
		return -1;
	}
	
	/**
	 * @return how many ids are missing an alias, including the 'any id' (-1)
	 */
	static int addMissingMaterialNames() {
		int r = 0;
		StringBuilder missing = new StringBuilder(m_missing_aliases + " ");
		for (final Material m : Material.values()) {
			assert m != null;
			ItemData data = new ItemData(m);
			if (provider.getMaterialName(data) == null) { // Material name is missing
				provider.setMaterialName(data, new MaterialName(m, "" + m.toString().toLowerCase().replace('_', ' '), "" + m.toString().toLowerCase().replace('_', ' '), 0));
				missing.append(m.getId() + ", ");
				r++;
			}
		}
		if (r > 0) // Give a warning about missing aliases we just worked around
			Skript.warning("" + missing.substring(0, missing.length() - 2));
		return r;
	}
	
	/**
	 * Parses an ItemType to be used as an alias, i.e. it doesn't parse 'all'/'every' and the amount.
	 * 
	 * @param s mixed case string
	 * @return A new ItemType representing the given value
	 */
	@Nullable
	public static ItemType parseAlias(final String s) {
		if (s.isEmpty()) {
			Skript.error(m_empty_string.toString());
			return null;
		}
		if (s.equals("*"))
			return everything;
		
		final ItemType t = new ItemType();
		
		final String[] types = s.split("\\s*,\\s*");
		for (final String type : types) {
			if (type == null || parseType(type, t, true) == null)
				return null;
		}
		
		return t;
	}
	
	private final static RegexMessage p_any = new RegexMessage("aliases.any", "", " (.+)", Pattern.CASE_INSENSITIVE);
	private final static Message m_any = new Message("aliases.any-skp");
	private final static RegexMessage p_every = new RegexMessage("aliases.every", "", " (.+)", Pattern.CASE_INSENSITIVE);
	private final static RegexMessage p_of_every = new RegexMessage("aliases.of every", "(\\d+) ", " (.+)", Pattern.CASE_INSENSITIVE);
	private final static RegexMessage p_of = new RegexMessage("aliases.of", "(\\d+) (?:", " )?(.+)", Pattern.CASE_INSENSITIVE);
	
	/**
	 * Parses an ItemType.
	 * <p>
	 * Prints errors.
	 * 
	 * @param s
	 * @return The parsed ItemType or null if the input is invalid.
	 */
	@Nullable
	public static ItemType parseItemType(String s) {
		if (s.isEmpty())
			return null;
		s = "" + s.trim();
		
		final ItemType t = new ItemType();
		
		Matcher m;
		if ((m = p_of_every.matcher(s)).matches()) {
			t.setAmount(Utils.parseInt("" + m.group(1)));
			t.setAll(true);
			s = "" + m.group(m.groupCount());
		} else if ((m = p_of.matcher(s)).matches()) {
			t.setAmount(Utils.parseInt("" + m.group(1)));
			s = "" + m.group(m.groupCount());
		} else if ((m = p_every.matcher(s)).matches()) {
			t.setAll(true);
			s = "" + m.group(m.groupCount());
		} else {
			final int l = s.length();
			s = Noun.stripIndefiniteArticle(s);
			if (s.length() != l) // had indefinite article
				t.setAmount(1);
		}
		
		final String lc = s.toLowerCase();
		final String of = Language.getSpaced("enchantments.of").toLowerCase();
		int c = -1;
		outer: while ((c = lc.indexOf(of, c + 1)) != -1) {
			final ItemType t2 = t.clone();
			final BlockingLogHandler log = SkriptLogger.startLogHandler(new BlockingLogHandler());
			try {
				if (parseType("" + s.substring(0, c), t2, false) == null)
					continue;
			} finally {
				log.stop();
			}
			if (t2.numTypes() == 0)
				continue;
			final String[] enchs = lc.substring(c + of.length()).split("\\s*(,|" + Pattern.quote(Language.get("and")) + ")\\s*");
			for (final String ench : enchs) {
				final EnchantmentType e = EnchantmentType.parse("" + ench);
				if (e == null)
					continue outer;
				t2.addEnchantments(e);
			}
			return t2;
		}
		
		if (parseType(s, t, false) == null)
			return null;
		
		if (t.numTypes() == 0)
			return null;
		
		return t;
	}
	
	/**
	 * Prints errors.
	 * 
	 * @param s The string holding the type, can be either a number or an alias, plus an optional data part. Case does not matter.
	 * @param t The ItemType to add the parsed ItemData(s) to (i.e. this ItemType will be modified)
	 * @param isAlias Whether this type is parsed for an alias.
	 * @return The given item type or null if the input couldn't be parsed.
	 */
	@Nullable
	private static ItemType parseType(final String s, final ItemType t, final boolean isAlias) {
		ItemType i;
		final String type = s;
		if (type.isEmpty()) {
			t.add(new ItemData(Material.AIR));
			return t;
		} else if (type.matches("\\d+")) {
			Skript.error("Numeric ids are not supported anymore.");
			return null;
		} else if ((i = getAlias(type)) != null) {
			for (ItemData d : i) {
				d = d.clone();
				t.add(d);
			}
			return t;
		}
		if (isAlias)
			Skript.error(m_invalid_item_type.toString(s));
		return null;
	}
	
	/**
	 * Gets an alias from the aliases defined in the config.
	 * 
	 * @param s The alias to get, case does not matter
	 * @return A copy of the ItemType represented by the given alias or null if no such alias exists.
	 */
	@Nullable
	private static ItemType getAlias(final String s) {
		ItemType i;
		String lc = "" + s.toLowerCase();
		final Matcher m = p_any.matcher(lc);
		if (m.matches()) {
			lc = "" + m.group(m.groupCount());
		}
		if ((i = getAlias_i(lc)) != null)
			return i.clone();
		boolean b;
		if ((b = lc.endsWith(" " + blockSingular)) || lc.endsWith(" " + blockPlural)) {
			if ((i = getAlias_i("" + s.substring(0, s.length() - (b ? blockSingular.length() : blockPlural.length()) - 1))) != null) {
				i = i.clone();
				for (int j = 0; j < i.numTypes(); j++) {
					final ItemData d = i.getTypes().get(j);
					if (d.getType().isBlock()) {
						i.remove(d);
						j--;
					}
				}
				if (i.getTypes().isEmpty())
					return null;
				return i;
			}
		} else if ((b = lc.endsWith(" " + itemSingular)) || lc.endsWith(" " + itemPlural)) {
			if ((i = getAlias_i("" + s.substring(0, s.length() - (b ? itemSingular.length() : itemPlural.length()) - 1))) != null) {
				for (int j = 0; j < i.numTypes(); j++) {
					final ItemData d = i.getTypes().get(j);
					if (!d.isAnything && d.getType().isBlock()) {
						i.remove(d);
						j--;
					}
				}
				if (i.getTypes().isEmpty())
					return null;
				return i;
			}
		}
		return null;
	}
	
	/**
	 * Clears aliases. Make sure to load them after this!
	 */
	public static void clear() {
		provider.clearAliases();
	}
	
	/**
	 * Loads aliases from Skript's standard locations.
	 * Exceptions will be logged, but not thrown.
	 */
	public static void load() {
		try {
			long start = System.currentTimeMillis();
			loadInternal();
			Skript.info("Loaded " + provider.getAliasCount() + " aliases in " + (System.currentTimeMillis() - start) + "ms");
		} catch (IOException e) {
			Skript.exception(e);
		}
	}
	
	private static void loadInternal() throws IOException {
		Path dataFolder = Skript.getInstance().getDataFolder().toPath();
		
		// Load aliases.zip OR aliases from jar (never both)
		Path zipPath = dataFolder.resolve("aliases-english.zip");
		if (!SkriptConfig.loadDefaultAliases.value()) {
			// Or do nothing, if user requested that default aliases are not loaded
		} else if (Files.exists(zipPath)) { // Load if it exists
			try (FileSystem zipFs = FileSystems.newFileSystem(zipPath, Skript.class.getClassLoader())) {
				assert zipFs != null; // It better not be...
				Path aliasesPath = zipFs.getPath("/");
				assert aliasesPath != null;
				loadDirectory(aliasesPath);
			}
		} else { // Fall back to jar loading
			try {
				URI jarUri = Skript.class.getProtectionDomain().getCodeSource().getLocation().toURI();
				try (FileSystem zipFs = FileSystems.newFileSystem(Paths.get(jarUri), Skript.class.getClassLoader())) {
					assert zipFs != null;
					Path aliasesPath = zipFs.getPath("/", "aliases-english");
					assert aliasesPath != null;
					loadDirectory(aliasesPath);
				}
			} catch (URISyntaxException e) {
				assert false;
			}
			
		}
		
		// Load everything from aliases folder (user aliases)
		Path aliasesFolder = dataFolder.resolve("aliases");
		if (Files.exists(aliasesFolder)) {
			assert aliasesFolder != null;
			loadDirectory(aliasesFolder);
		}
		
		// Update tracked item types
		for (Map.Entry<String, ItemType> entry : trackedTypes.entrySet()) {
			@SuppressWarnings("null") // No null keys in this map
			ItemType type = parseItemType(entry.getKey());
			if (type == null)
				Skript.warning("Alias '" + entry.getKey() + "' is required by Skript, but does not exist anymore. "
						+ "Make sure to fix this before restarting the server.");
			else
				entry.getValue().setTo(type);
		}
	}
	
	/**
	 * Loads aliases from given directory.
	 * @param dir Directory of aliases.
	 * @throws IOException If something goes wrong with loading.
	 */
	public static void loadDirectory(Path dir) throws IOException {
		try {
			Files.list(dir).sorted().forEach((f) -> {
				assert f != null;
				try {
					String name = f.getFileName().toString();
					if (Files.isDirectory(f) && !name.startsWith("."))
						loadDirectory(f);
					else if (name.endsWith(".sk"))
						load(f);
				} catch (IOException e) {
					throw new UncheckedIOException(e);
				}
			});
		} catch (UncheckedIOException e) {
			throw e.getCause();
		}
	}
	
	/**
	 * Loads aliases from given path.
	 * @param f Path of alias file.
	 * @throws IOException If something goes wrong with loading.
	 */
	public static void load(Path f) throws IOException {
		Config config = new Config(f, false, false, "=");
		load(config);
	}
	
	/**
	 * Loads aliases from configuration.
	 * @param config Configuration containing the aliases.
	 */
	public static void load(Config config) {
		for (Node n : config.getMainNode()) {
			if (!(n instanceof SectionNode)) {
				Skript.error(m_outside_section.toString());
				continue;
			}
			
			parser.load((SectionNode) n);
		}
	}
	
	/**
	 * Checks if the first parameter is supertype of second.
	 * @param first First item data.
	 * @param second Second item data.
	 * @return If first is supertype of second.
	 */
	public static boolean isSupertypeOf(ItemData first, ItemData second) {
		Set<ItemData> subtypes = provider.getSubtypes(first);
		if (subtypes != null)
			return subtypes.contains(second);
		return false;
	}

	/**
	 * Gets a Vanilla Minecraft material id for given item data.
	 * @param data Item data.
	 * @return Minecraft item id.
	 */
	@Nullable
	public static String getMinecraftId(ItemData data) {
		return provider.getMinecraftId(data.aliasCopy());
	}
	
	private static final Map<String, ItemType> trackedTypes = new HashMap<>();
	
	/**
	 * Gets an item type that matches the given name.
	 * If it doesn't exist, an exception is thrown instead.
	 * 
	 * <p>Item types provided by this method are updated when aliases are
	 * reloaded. However, this also means they are tracked by aliases system
	 * and NOT necessarily garbage-collected.
	 * @param name Name of item to search from aliases.
	 * @return An item.
	 * @throws IllegalArgumentException When item is not found.
	 */
	public static ItemType javaItemType(String name) {
		ItemType type = parseItemType(name);
		if (type == null)
			throw new IllegalArgumentException("type " + name + " not found");
		trackedTypes.put(name, type);
		return type;
	}
	
	/**
	 * Creates an aliases provider to be used by given addon. It can be used to
	 * register aliases and variations to be used in scripts.
	 * @param addon Skript addon.
	 * @return Aliases provider.
	 */
	public static AliasesProvider getAddonProvider(@Nullable SkriptAddon addon) {
		if (addon == null) {
			throw new IllegalArgumentException("addon needed");
		}
		
		// TODO in future, maybe record and allow unloading addon-provided aliases?
		return provider; // For now, just allow loading aliases easily
	}
}
