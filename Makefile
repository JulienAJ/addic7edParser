JAVAC=javac
JAVA=java
JAR=jar cfm
CLASSDIR=bin/
SRCDIR=src/
LIBSDIR=libs/
NAME=addic7edParser
JSOUPJAR=jsoup-1.8.3.jar
COMMONSCLIJAR=commons-cli-1.2.jar
MANIFEST=MANIFEST.MF
JAVAEXT=.java
JAREXT=.jar
CLASSEXT=.class

all: $(NAME)$(JAREXT)

source: $(SRCDIR)*$(JAVAEXT)
	$(JAVAC) -classpath $(LIBSDIR)$(JSOUPJAR):$(LIBSDIR)$(COMMONSCLIJAR):$(CLASSDIR):. -d $(CLASSDIR) $^

$(NAME)$(JAREXT):source
	$(JAR) $@ $(MANIFEST) -C $(LIBSDIR) . -C $(CLASSDIR) .

clean:
	@rm -fr $(CLASSDIR)*

mrproper: clean
	@rm -f $(NAME)$(JAREXT)
