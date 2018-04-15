package ch.njol.skript.lang.cache.debug;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.cache.ParsedCommand;
import ch.njol.skript.lang.cache.ParsedFunction;
import ch.njol.skript.lang.cache.ParsedScript;
import ch.njol.skript.lang.cache.ParsedTrigger;

public class DebugScript implements ParsedScript {

	@Override
	public ParsedCommand command() {
		Skript.info("Command");
		return new DebugCommand();
	}

	@Override
	public ParsedTrigger trigger() {
		Skript.info("Trigger");
		return new DebugTrigger();
	}

	@Override
	public ParsedFunction function() {
		Skript.info("Function");
		return new DebugFunction();
	}
	
}
