package ch.njol.skript.lang.cache.debug;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.cache.BitCode;
import ch.njol.skript.lang.cache.ParsedTrigger;

public class DebugTrigger implements ParsedTrigger {

	@Override
	public BitCode event() {
		Skript.info("Event");
		return new DebugBitCode();
	}

	@Override
	public BitCode trigger() {
		Skript.info("Trigger");
		return new DebugBitCode();
	}
	
}
