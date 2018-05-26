package ch.njol.skript.lang.cache.load;

/**
 * Element that can be loaded.
 */
public interface LoadableElement<T> {
	
	T load();
}
