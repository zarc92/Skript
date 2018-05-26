package ch.njol.skript.lang.cache.load;

import java.util.ArrayList;
import java.util.List;

import ch.njol.skript.lang.cache.ParsedCommand;
import ch.njol.skript.lang.cache.ParsedFunction;
import ch.njol.skript.lang.cache.ParsedScript;
import ch.njol.skript.lang.cache.ParsedTrigger;

public class ParsedScriptLoader implements ParsedScript, LoadableElement<List<LoadableElement<?>>> {

	private List<LoadableElement<?>> elements;
	
	public ParsedScriptLoader() {
		this.elements = new ArrayList<>();
	}
	
	@Override
	public ParsedCommand command() {
		ParsedCommandLoader command = new ParsedCommandLoader();
		elements.add(command);
		return command;
	}

	@Override
	public ParsedTrigger trigger() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ParsedFunction function() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<LoadableElement<?>> load() {
		return elements;
	}
	
}
