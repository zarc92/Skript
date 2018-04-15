package ch.njol.skript.lang.cache;

public interface ParsedSignature {
	
	void name(String name);
	
	void argument(String name, Class<?> type, boolean single, boolean optional);
}
