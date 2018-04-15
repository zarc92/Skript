package ch.njol.skript.lang.cache.debug;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.cache.BitCode;
import ch.njol.skript.lang.cache.ParsedFunction;

public class DebugFunction implements ParsedFunction {

	@Override
	public void name(String name) {
		Skript.info("Name: " + name);
	}

	@Override
	public void argument(String name, Class<?> type, boolean single, boolean optional) {
		Skript.info("Argument: " + name + ", " + type + ", single: " + single + ", optional: " + optional);
	}

	@Override
	public BitCode trigger() {
		Skript.info("Trigger");
		return new DebugBitCode();
	}
	
}
