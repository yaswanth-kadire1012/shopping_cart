# Makefile for JDBC Shopping System

JC = javac
JVM = java
JFLAGS = -g
CP = -cp ".:lib/*:src"
RUN_CP = -cp ".:lib/*:out"

SRC_DIR = src
OUT_DIR = out
LIB_DIR = lib

# Find all Java files recursively
SOURCES = $(shell find $(SRC_DIR) -name "*.java")

all: clean compile

compile:
	mkdir -p $(OUT_DIR)
	mkdir -p $(LIB_DIR)
	# Assuming mysql-connector is in lib/, if not we will copy it from root.
	cp mysql-connector-j-9.6.0.jar $(LIB_DIR)/ 2>/dev/null || :
	$(JC) $(JFLAGS) -d $(OUT_DIR) $(CP) $(SOURCES)

run: compile
	$(JVM) $(RUN_CP) Main

clean:
	rm -rf $(OUT_DIR)/*

.PHONY: all compile run clean
