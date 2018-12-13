package rb.compiler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import me.qmx.jitescript.JiteClass;
import rb.compiler.Builder.CompilationUnit;
import rb.compiler.Parser.Syntax;
import rb.compiler.Tokenizer.Token;

public class Main {
	private static final boolean INTERPRET = true;
	
	public static void main(String[] args) {
		String src = loadFile("src/main.sn");
		
		long beforeLex = System.currentTimeMillis();
		List<Token> tokens = new ArrayList<Token>();
		Lexer.run(src, tokens);
		long afterLex = System.currentTimeMillis();
		printTokens(tokens);
		
		long beforeParse = System.currentTimeMillis();
		Syntax syntax = new Syntax();
		syntax.name = "main";
		Parser.run(tokens, syntax);
		long afterParse = System.currentTimeMillis();
		
		long beforeBuild = System.currentTimeMillis();
		CompilationUnit unit = Builder.run(syntax);
		long afterBuild = System.currentTimeMillis();
		
		long lexTime = afterLex - beforeLex;
		long parseTime = afterParse - beforeParse;
		long buildTime = afterBuild - beforeBuild;
		long totalTime = lexTime + parseTime + buildTime;
		
		System.out.println("Compilation complete after " + totalTime + " ms:");
		System.out.println("Lexer:   " + lexTime + " ms");
		System.out.println("Parser:  " + parseTime + " ms");
		System.out.println("Builder: " + buildTime + " ms");
		System.out.println();
		
		if (INTERPRET) {
			interpretUnit(unit, syntax);
		} else {
			compileUnit(unit);
		}
	}
	
	private static void interpretUnit(CompilationUnit unit, Syntax syntax) {
		try {
			byte[] bytes = unit.toBytes();
			Class<?> c = new ClassLoader() {
				Class<?> define(byte[] bytes) {
					return defineClass(unit.getClassName(), bytes, 0, bytes.length);
				}
			}.define(bytes);
			c.getMethod("main", String[].class).invoke(null, (Object) new String[] {});
			
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		}
	}
	
	private static void compileUnit(JiteClass unit) {
		try {
			byte[] bytes = unit.toBytes();
			FileOutputStream out = new FileOutputStream(unit.getClassName() + ".class");
			out.write(bytes);
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static void printTokens(List<Token> tokens) {
		System.out.println("\n######## TOKEN STREAM ########\n\n");
		for (Token token : tokens) {
			String type = Lexer.getTypeStr(token.type);
			String empty = "";
			for (int i = 0; i < 20 - type.length(); i++) {
				empty += " ";
			}
			System.out.println(type + ":" + empty + token.val);
		}
	}
	
	private static byte[] loadClassBytes(String path) {
		try {
			return Files.readAllBytes(new File(path).toPath());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private static String loadFile(String path) {
		try {
			BufferedReader reader = new BufferedReader(new FileReader(new File(path)));
			StringBuilder result = new StringBuilder();
			String line = null;
			while ((line = reader.readLine()) != null) {
				result.append(line).append("\n");
			}
			reader.close();
			return result.toString();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}
