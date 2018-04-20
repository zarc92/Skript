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
package ch.njol.skript.lang.cache;

import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import ch.njol.skript.lang.SyntaxElement;

public interface BitCode {
	
	<T extends SyntaxElement> void initElement(Class<T> type, ParseResult parseResult, int matchedPattern, Kleenean isDelayed);
	
	void defaultExpr(Class<?> type);
	
	<T> void expressionList(Class<T> type, int size, boolean and);
	
	<T> void literalList(Class<T> type, int size, boolean and);
	
	void variable(Class<?>[] types, boolean local, boolean list);
	
	void functionCall(String function);
	
	void variableString(int size);
	
	void stringLiteral(String str);
	
	void unparsedLiteral(String unparsed);
	
	void convertedExpression(Class<?> type);
	
	void simpleLiteral(Object literal);

}
