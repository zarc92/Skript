package ch.njol.skript.lang.cache.load;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Queue;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionList;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.LiteralList;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxElement;
import ch.njol.skript.lang.Variable;
import ch.njol.skript.lang.VariableString;
import ch.njol.skript.lang.cache.BitCode;
import ch.njol.skript.registrations.Classes;
import ch.njol.util.Kleenean;

public class BitCodeLoader implements BitCode, LoadableElement<Deque<Expression<?>>> {
	
	private Deque<Expression<?>> stack;

	public BitCodeLoader() {
		stack = new ArrayDeque<>();
	}
	
	@Override
	public <T extends SyntaxElement> void initElement(Class<T> type, ParseResult parseResult, int matchedPattern, Kleenean isDelayed) {
		// Create instance of element we will use
		T element = null;
		try {
			element = type.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			throw new Error(e);
		}
		
		// Take exprs from the stack
		Expression<?>[] exprs = new Expression<?>[parseResult.exprs.length];
		for (int i = 0; i < parseResult.exprs.length; i++) {
			exprs[i] = stack.pop();
		}
		
		// Initialize element and push to stack
		element.init(exprs, matchedPattern, isDelayed, parseResult);
		stack.push((Expression<?>) element);
	}

	@Override
	public void defaultExpr(Class<?> type) {
		ClassInfo<?> info = Classes.getExactClassInfo(type);
		assert info != null; // Unless bitcode is corrupted?
		stack.push(info.getDefaultExpression());
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> void expressionList(Class<T> type, int size, boolean and) {
		Expression<? extends T>[] exprs = new Expression[size];
		for (int i = 0; i < size; i++) {
			exprs[i] = (Expression<? extends T>) stack.pop();
		}
		
		ExpressionList<T> list = new ExpressionList<>(exprs, type, and);
		stack.push(list);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> void literalList(Class<T> type, int size, boolean and) {
		Literal<? extends T>[] exprs = new Literal[size];
		for (int i = 0; i < size; i++) {
			exprs[i] = (Literal<? extends T>) stack.pop();
		}
		
		LiteralList<T> list = new LiteralList<>(exprs, type, and);
		stack.push(list);
	}

	@Override
	public void variable(Class<?>[] types, boolean local, boolean list) {
		@SuppressWarnings("null")
		Variable<?> variable = Variable.newInstance((VariableString) stack.pop(), types, local, list);
		stack.push(variable);
	}

	@Override
	public void functionCall(String function) {
		// TODO implement emitting functions from parser
	}

	@Override
	public void variableString(int size) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void stringLiteral(String str) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void unparsedLiteral(String unparsed) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void convertedExpression(Class<?> type) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void simpleLiteral(Object literal) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Deque<Expression<?>> load() {
		return stack;
	}
	
}
