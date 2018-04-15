package ch.njol.skript.lang.cache.debug;

import java.util.List;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.cache.BitCode;
import ch.njol.skript.lang.cache.ParsedCommand;
import ch.njol.skript.util.Timespan;

public class DebugCommand implements ParsedCommand {

	@Override
	public void name(String name) {
		Skript.info("Name: " + name);
	}

	@Override
	public void argument(String name, Class<?> type, boolean single, boolean forceOptional) {
		Skript.info("Argument: " + name + ", " + type + ", single: " + single + ", forceOptional: " + forceOptional);
	}

	@Override
	public void usage(String usage) {
		Skript.info("Usage: " + usage);
	}

	@Override
	public void description(String desc) {
		Skript.info("Description: " + desc);
	}

	@Override
	public void aliases(List<String> aliases) {
		Skript.info("Aliases: " + aliases);
	}

	@Override
	public void executableBy(int executable) {
		Skript.info("ExecutableBy: " + executable);
	}

	@Override
	public void cooldown(Timespan time) {
		Skript.info("Cooldown: " + time);
	}

	@Override
	public BitCode cooldownMessage() {
		Skript.info("CooldownMessage");
		return new DebugBitCode();
	}

	@Override
	public void cooldownBypass(String bypass) {
		Skript.info("CooldownBypass: " + bypass);
	}

	@Override
	public BitCode cooldownStorage() {
		Skript.info("CooldownStorage");
		return new DebugBitCode();
	}

	@Override
	public BitCode trigger() {
		Skript.info("Trigger");
		return new DebugBitCode();
	}
	
}
