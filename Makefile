JAVAC=javac
JAVA=java
JAR=jar cfm
CLASSDIR=bin/
SRCDIR=src/
NAME=addic7edParser
JSOUPJAR=jsoup-1.8.3.jar
COMMONSCLIJAR=commons-cli-1.2.jar
MANIFEST=MANIFEST.MF
JAVAEXT=.java
JAREXT=.jar
CLASSEXT=.class

all: $(NAME)$(JAREXT)

source: $(SRCDIR)*$(JAVAEXT)
	$(JAVAC) -classpath $(JSOUPJAR):$(COMMONSCLIJAR):$(CLASSDIR):. -d $(CLASSDIR) $^

$(NAME)$(JAREXT):source
	$(JAR) $@ $(MANIFEST) $(COMMONSCLIJAR) $(JSOUPJAR) -C $(CLASSDIR) .

clean:
	@rm -fr $(CLASSDIR)*

mrproper: clean
	@rm -f $(NAME)$(JAREXT)
