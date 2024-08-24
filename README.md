# RPAL Interpreter

## Project Overview

This project is a lexical analyzer, parser, and interpreter for the RPAL programming language, implemented in Java as part of a semester project. The project includes the following components:

**Lexical Analyzer:** Scans the input RPAL program to generate tokens based on the lexical rules specified in the RPAL_Lex.pdf.

**Parser:** Builds an Abstract Syntax Tree (AST) from the tokens, following the grammar rules outlined in the RPAL_Grammar.pdf.

**AST to ST Conversion:** Converts the AST into a Standardized Tree (ST) as part of the program's execution.

**CSE Machine:** Executes the ST using a Control Stack Environment (CSE) machine to produce the final output.

## Features

**AST Generation:** The parser generates a detailed Abstract Syntax Tree (AST) from the input program.

**AST to ST Conversion:** The AST is transformed into a Standardized Tree (ST) before execution.

**CSE Machine Execution:** The ST is executed using a CSE machine to produce the final output.

**Command-Line Interface:** The program can be run from the command line, with options for different outputs.

#Instructions

To run the interpreter, open terminal from root directory give following commands

I. "make" or "javac myrpal.java" : compile project\
II. "make run" or "java myrpal test_programs/rpal_test" : run interpreter with rpal_test file\
III. to get the AST "make run AST=true" or "java myrpal -ast test_programs/rpal_test" : run interpreter to get ast
III. "make clean" : remove all class files before recompiling

test.rpal and rpal_test files contains same program, test.rpal file was used to verify the functionality of the program
