package com.craftinginterpreters.tool;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class GenerateAst {

    public static void main(String[] args) throws IOException {

        if (args.length != 1) {
            System.err.println("Usages: generateAst <output directory>");
            System.exit(64);
        }

        String outputDir = args[0];

        defineAst(outputDir, "Expr", Arrays.asList(
                "Binary     : Expr left, Token operator, Expr right",
                "Call       : Expr callee, Token paren, List<Expr> arguments",
                "Get        : Expr object, Token name",
                "Set        : Expr object, Token name, Expr value",
                "This       : Token keyword",
                "Super      : Token keyword, Token method",
                "Grouping   : Expr expression",
                "Literal    : Object value",
                "Logical    : Expr left, Token operator, Expr right",
                "Unary      : Token operator, Expr right",
                "Variable   : Token name",
                "AnonFun    : List<Token> params, List<Stmt> body",
                "Assign     : Token name, Expr value"));
        
        defineAst(outputDir, "Stmt", Arrays.asList(
            "Block      : List<Stmt> statements",
            "Class      : Token name, Expr.Variable superClass, List<Stmt.Function> methods",
            "Expression : Expr expression",
            "Function   : Token name, List<Token> params, List<Stmt> body",
            "Print      : Expr expression",
            "Var        : Token name, Expr initializer",
            "Return     : Token keyword, Expr value",
            "If         : Expr condition, Stmt thenBranch, Stmt elseBranch",
            "While      : Expr condition, Stmt body",
            "Break      : Token token"
        ));
    }

    private static void defineAst(
            String outputDir,
            String baseName,
            List<String> types)
            throws IOException {

        Path path = Paths.get(outputDir, baseName + ".java");
        PrintWriter writer = new PrintWriter(path.toString(), "UTF-8");

        writer.println("package com.craftinginterpreters.lox;");
        writer.println();
        writer.println("import java.util.List;");
        writer.println();
        writer.println("abstract class " + baseName + " {");

        defineVisitor(writer, baseName, types);

        for (String type : types) {
            var parts = type.split(":");
            String className = parts[0].trim();
            String fields = parts[1].trim();
            defineType(writer, baseName, className, fields);
        }

        writer.println();

        writer.println("    abstract <R> R accept(Visitor<R> visitor);");

        writer.println("}");
        writer.close();
    }

    private static void defineVisitor(PrintWriter writer, String baseName, List<String> types) {
        writer.println("    interface Visitor<R> {");
        for (String type : types) {
            String typeName = type.split(":")[0].trim();
            writer.println(
                    "        R visit" + typeName + baseName + "(" + typeName + " " + baseName.toLowerCase() + ");");
        }
        writer.println("        }");
    }

    private static void defineType(
            PrintWriter writer,
            String baseName,
            String className,
            String fieldList) {
        writer.println();
        writer.println("    static class " + className + " extends " + baseName + " {");
        writer.println("        " + className + "(" + fieldList + ") {");

        // Store parameters in fields
        String[] fields = fieldList.split(", ");
        for (String field : fields) {
            String name = field.split(" ")[1];
            writer.println("            this." + name + " = " + name + ";");
        }

        writer.println("        }");
        writer.println();

        // call visitor
        writer.println();
        writer.println("       @Override");
        writer.println("       <R> R accept(Visitor<R> visitor) {");
        writer.println("           return visitor.visit" + className + baseName + "(this);");
        writer.println("        }");
        writer.println();
        
        for (String field : fields) {
            writer.println("        final " + field + ";");
        }

        writer.println("    }");
    }
}
