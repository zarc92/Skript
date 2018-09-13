package ch.njol.skript.lang.cache.load;

import ch.njol.skript.lang.Trigger;
import ch.njol.skript.lang.cache.BitCode;
import ch.njol.skript.lang.cache.ParsedTrigger;

public class ParsedTriggerLoader implements ParsedTrigger, LoadableElement<Trigger> {

	private BitCodeLoader event;
	private BitCodeLoader trigger;
	
	public ParsedTriggerLoader() {
		this.event = new BitCodeLoader();
		this.trigger = new BitCodeLoader();
	}
	
	@Override
	public BitCode event() {
		return event;
	}

	@Override
	public BitCode trigger() {
		return trigger;
	}

	@Override
	public Trigger load() {
		// TODO emit events in SkriptParser
		return null;
	}
	
}
