SRCDIR := src
BINDIR := bin

JAVAC := javac
JFLAGS := -d $(BINDIR) -cp $(BINDIR)

$(BINDIR)/%.class: $(SRCDIR)/%.java
	@mkdir -p $(@D)
	$(JAVAC) $(JFLAGS) $<

all: \
	$(BINDIR)/main_package/HotelsMainCLI.class \
	$(BINDIR)/main_package/HotelsMain.class

$(SRCDIR)/main_package/HotelsMainCLI.java: $(BINDIR)/main_package/Model.class

$(SRCDIR)/main_package/HotelsMain.java: \
	$(BINDIR)/main_package/Model.class \
	$(BINDIR)/main_package/View.class \
	$(BINDIR)/main_package/Controller.class
