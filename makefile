##
# source directory
##
SRC_DIR := com/craftinginterpreters/lox

##
# output directory
##
OUT_DIR := out

##
# sources
##
## SRCS := $(wildcard $(SRC_DIR)/*.java)
SRCS := $(SRC_DIR)/TokenType.java \
	$(SRC_DIR)/Token.java \
	$(SRC_DIR)/Scanner.java \
	$(SRC_DIR)/Lox.java

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
# Run JLox
##
run:
	$(JVM) -cp $(OUT_DIR) com.craftinginterpreters.lox.Lox