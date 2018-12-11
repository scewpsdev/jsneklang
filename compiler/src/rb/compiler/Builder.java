package rb.compiler;

import static me.qmx.jitescript.util.CodegenUtils.*;
import static org.objectweb.asm.Opcodes.*;
import static rb.compiler.Parser.*;

import java.io.PrintStream;
import java.util.List;

import me.qmx.jitescript.CodeBlock;
import me.qmx.jitescript.JiteClass;
import rb.compiler.Parser.Expression;
import rb.compiler.Parser.ExpressionStatement;
import rb.compiler.Parser.FuncCallExpression;
import rb.compiler.Parser.Statement;
import rb.compiler.Parser.Syntax;

public class Builder {
	public static JiteClass run(Syntax syntax) {
		JiteClass unit = new CompilationUnit(syntax.name);
		
		unit.defineMethod("main", ACC_PUBLIC | ACC_STATIC, sig(void.class, String[].class), new CodeBlock() {
			{
				//getstatic(p(System.class), "out", ci(PrintStream.class));
				//ldc("hello world");
				//invokevirtual(p(PrintStream.class), "println", sig(void.class, Object.class));
				for (Statement statement : syntax.statements) {
					buildStatement(statement, this);
				}
				voidreturn();
			}
		});
		
		return unit;
	}
	
	private static void buildStatement(Statement statement, CodeBlock block) {
		switch (statement.getType()) {
		case STATEMENT_EXPR:
			ExpressionStatement s = (ExpressionStatement) statement;
			buildExpression(s.expr, block);
			block.pop(); // pop expression result
			break;
		}
	}
	
	private static void buildExpression(Expression expr, CodeBlock block) {
		switch (expr.getType()) {
		
		case EXPR_IDENTIFIER: {
			block.ldc("null");
			break;
		}
		
		case EXPR_STRING: {
			StringExpression e = (StringExpression) expr;
			block.ldc(e.val);
			break;
		}
		
		case EXPR_VAR_DECL: {
			block.ldc(0);
			break;
		}
		
		case EXPR_FUNC_CALL: {
			FuncCallExpression e = (FuncCallExpression) expr;
			block.getstatic(p(System.class), "out", ci(PrintStream.class));
			
			for (Expression arg : e.args) {
				buildExpression(arg, block);
			}
			
			block.invokevirtual(p(PrintStream.class), "println", sig(void.class, Object.class));
			block.ldc(0);
			break;
		}
		}
	}
	
	public static class CompilationUnit extends JiteClass {
		public CompilationUnit(String name) {
			super(name);
		}
	}
}