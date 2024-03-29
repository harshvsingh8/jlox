##
# source directory
##
SRC_DIR := com/craftinginterpreters/lox
##
# output directory
##
OUT_DIR := out

##
# psources
##
## SRCS := $(wildcard $(SRC_DIR)/*.java)
SRCS := $(SRC_DIR)/TokenType.java \
	$(SRC_DIR)/Token.java \
	$(SRC_DIR)/Scanner.java \
	$(SRC_DIR)/Lox.java \
	$(SRC_DIR)/Expr.java \
	$(SRC_DIR)/AstPrinter.java \
	$(SRC_DIR)/Parser.java \
	$(SRC_DIR)/Interpreter.java \
	$(SRC_DIR)/RuntimeError.java \
	$(SRC_DIR)/Stmt.java \
	$(SRC_DIR)/Environment.java \
	$(SRC_DIR)/LoxCallable.java \
	$(SRC_DIR)/LoxFunction.java \
	$(SRC_DIR)/Return.java \
	$(SRC_DIR)/Globals.java \
	$(SRC_DIR)/Resolver.java \
	$(SRC_DIR)/LoxClass.java \
	$(SRC_DIR)/LoxInstance.java 

##
# classes
## 
CLS := $(SRCS:$(SRC_DIR)/%.java=$(OUT_DIR)/%.class)

##
# compiler and compiler flags
##
JC := javac
JCFLAGS := -d $(OUT_DIR)/ -cp $(OUT_DIR)
JVM= java

##
# suffixes
##
.SUFFIXES: .java

##
# targets that do not produce output files
##
.PHONY: all clean

##
# default target(s)
##
build: 
	$(JC) $(JCFLAGS) $(SRCS)

##
# default target(s)
##
default_all: $(CLS)

$(CLS): $(OUT_DIR)/%.class: $(SRC_DIR)/%.java
	$(JC) $(JCFLAGS) $<

##
# clean up any output files
##
clean:
	rm $(OUT_DIR)/*.class

##
# Genertate Ast tool
##
build_gast:
	$(JC) $(JCFLAGS) com/craftinginterpreters/tool/GenerateAst.java

##
# Run gast
##
run_gast:
	$(JVM) -cp $(OUT_DIR) com.craftinginterpreters.tool.GenerateAst $(SRC_DIR)

##
# Run JLox
##
run:
	$(JVM) -cp $(OUT_DIR) com.craftinginterpreters.lox.Lox $(src)
