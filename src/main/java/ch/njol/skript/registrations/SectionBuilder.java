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
package ch.njol.skript.registrations;

import org.apache.commons.lang.ArrayUtils;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Section;
import ch.njol.skript.lang.SyntaxElementInfo;
import ch.njol.skript.lang.TriggerSection;
import ch.njol.util.Pair;

/**
 * A builder style registrar for sections.
 *
 * For simple sections (i.e. ones that do not have specific placement requirements),
 * you should use {@link ch.njol.skript.Skript#registerSection(String, Class, String...)}
 *
 * Example usage:
 * <code>
 *     SectionBuilder.create()
 *     		.name("switch")
 *     		.source(SwitchSection.class)
 *     		.patterns("switch %objects%")
 *     		.children(CaseSection.class)
 *     		.register();
 * </code>
 *
 */
public final class SectionBuilder {

	@Nullable
	private String name;

	@Nullable
	private Class<? extends Section> source;

	@Nullable
	private String[] patterns;

	@Nullable
	private Class<? extends TriggerSection>[] children;

	@Nullable
	private Section.Placement placement = Section.Placement.ANYWHERE;

	@Nullable
	private Class<? extends TriggerSection>[] parents;

	@Nullable
	private String illegalPlacementError;

	private SectionBuilder() {
	}

	private SectionBuilder(@NonNull String name) {
		this.name = name;
	}

	public static SectionBuilder create() {
		return new SectionBuilder();
	}

	public static SectionBuilder create(String name) {
		return new SectionBuilder(name);
	}

	/**
	 * Defines the name of this section used for errors unless a custom error
	 * is specified via {@link SectionBuilder#illegalPlacementError(String)}
	 */
	public SectionBuilder name(String name) {
		this.name = name;
		return this;
	}

	/**
	 * Defines the class to be instantiated when the patterns
	 * defined via {@link SectionBuilder#patterns} are matched.
	 */
	public final SectionBuilder source(Class<? extends Section> source) {
		this.source = source;
		return this;
	}

	/**
	 * Defines the patterns for this section
	 */
	public SectionBuilder patterns(String... patterns) {
		this.patterns = patterns;
		return this;
	}

	/**
	 * Defines the sections that this section is allowed to contain,
	 * if not called then everything will be allowed (including {@link ch.njol.skript.lang.TriggerItem}).
	 */
	@SafeVarargs
	public final SectionBuilder children(Class<? extends TriggerSection>... children) {
		this.children = children;
		return this;
	}

	/**
	 * Defines the sections that this section is allowed to be inside as defined
	 * by {@link SectionBuilder#placement(Section.Placement)}, if not called then
	 * this section can be used anywhere.
	 *
	 * @throws IllegalArgumentException if {@code parents} is null or empty
	 */
	@SafeVarargs
	public final SectionBuilder parents(Class<? extends TriggerSection>... parents) throws IllegalArgumentException {
		this.parents = parents;
		return placement(Section.Placement.DIRECT_CHILD);
	}

	/**
	 * Defines how this section must be placed relative to it's
	 * parents.
	 *
	 * @throws IllegalArgumentException if parents haven't been set to a
	 * non-null, non-empty array via {@link SectionBuilder#parents(Class[])}
	 * or if you attempt to use an invalid placement for the current parents.
	 */
	public SectionBuilder placement(Section.Placement placement) throws IllegalArgumentException {
		if (ArrayUtils.isEmpty(parents)) {
			throw new IllegalArgumentException("A section must have parents to use a placement");
		}

		if (placement != Section.Placement.CHILD && placement != Section.Placement.DIRECT_CHILD) {
			throw new IllegalArgumentException("This placement is not allowed when a section has parents");
		}

		this.placement = placement;

		return this;
	}

	/**
	 * Defines the error printed when the section is placed somewhere
	 * it shouldn't be as defined by {@link SectionBuilder#parents(Class[])}
	 * and the allowed children of another method.
	 */
	public SectionBuilder illegalPlacementError(String error) {
		this.illegalPlacementError = error;
		return this;
	}

	/**
	 * Registers this section.
	 *
	 * @throws IllegalStateException if required info about this section is missing
	 */
	@SuppressWarnings("unchecked")
	public void register() throws IllegalStateException {
		if (name == null) {
			throw new IllegalStateException("A section must have a name!");
		}

		if (source == null) {
			throw new IllegalStateException("A section must have an owning class!");
		}

		if (illegalPlacementError == null) {
			illegalPlacementError = "A " + name + " section may not be used here";
		}

		if (patterns == null) {
			patterns = new String[0];
		}

		// TODO: keep track of how many calls have been made to get the origin in normal usage
		String originClassPath = Thread.currentThread().getStackTrace()[2].getClassName();

		assert source != null;
		assert patterns != null;

		SyntaxElementInfo<? extends Section> info = new SyntaxElementInfo<>(patterns, source, originClassPath,
				new Pair<>("parent-sections", parents),
				new Pair<>("child-sections", children),
				new Pair<>("allowed-placement", placement),
				new Pair<>("placement-error", illegalPlacementError), new Pair<>("name", name));

		Skript.getSectionInfos().put(source, info);

		Skript.getSections().add(info);

	}

}
