package ch.njol.skript.lang.cache;

public interface ParsedScript {
	
	ParsedCommand command();
	
	ParsedTrigger trigger();
	
	ParsedFunction function();
}
