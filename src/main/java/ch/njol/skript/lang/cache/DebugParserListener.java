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

import ch.njol.skript.Skript;

public class DebugParserListener implements ParserListener {
	
	@Override
	public void initExpression(Class<?> type) {
		Skript.info("InitExpression: " + type);
	}
	
	@Override
	public void defaultExpr(Class<?> type) {
		Skript.info("Default: " + type);
	}
	
	@Override
	public void expressionList(Class<?> type) {
		Skript.info("ExpressionList: " + type);
	}
	
	@Override
	public void literalList(Class<?> type) {
		Skript.info("LiteralList: " + type);
	}
	
	@Override
	public void variable() {
		Skript.info("Variable");
	}
	
	@Override
	public void functionCall(String function) {
		Skript.info("FuncTionCall: " + function);
	}
	
	@Override
	public void variableString() {
		Skript.info("VariableString");
	}
	
	@Override
	public void stringLiteral(String str) {
		Skript.info("StringLiteral: " + str);
	}
	
	@Override
	public void unparsedLiteral(String unparsed) {
		Skript.info("Unparsed: " + unparsed);
	}

	@Override
	public void convertedExpression(Class<?> type) {
		Skript.info("ConvertedExpression: " + type);
	}

	@Override
	public void simpleLiteral(Object literal) {
		Skript.info("SimpleLiteral: " + literal);
	}
}
