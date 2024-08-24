JFLAGS = -g
JC = javac

.SUFFIXES: .java .class

.java.class:
	$(JC) $(JFLAGS) $*.java

CLASSES = \
		  scanner/Scanner.java \
		  parser/Parser.java \
		  CSE_Machine/CSEMachine.java \
		  myrpal.java

default: classes

classes: $(CLASSES:.java=.class)


clean:
	rm -f CSE_Machine/*.class
	rm -f PARSER/*.class
	rm -f SCANNER/*.class
	rm -f myrpal.class


run:
ifeq ($(AST),true)
	java myrpal -ast test_programs/rpal_test
else
	java myrpal test_programs/rpal_test
endif

