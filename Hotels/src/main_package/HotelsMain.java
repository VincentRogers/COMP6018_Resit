package main_package;

public class HotelsMain {

    public static void main(String[] args) {
	// Parse command line arguments
	if (args.length > 2) {
	    System.out.println("Usage: HotelsMain [nPlayers]");
	    return;
	}
	int nPlayers = 2;
	if (args.length > 1) {
	    nPlayers = Integer.parseInt(args[0]);
	}
	if (nPlayers < 2 || nPlayers > 4) {
	    System.out.println("Invalid number of players (must be 2, 3 or 4)");
	    return;
	}

	// Create the model
	Model model = new Model(nPlayers);
	for (int i = 0; i < nPlayers; i++) {
	    String name = "p" + (i + 1);
	    model.addPlayer(i + 1, name, 2000);
	}

	// Create and display the view, attach the controller
	javax.swing.SwingUtilities.invokeLater(() -> {
	    createAndShowGUI(model);
	});
    }

    public static void createAndShowGUI(Model model) {
	View view = new View(model);
	Controller controller = new Controller(model, view);
	view.setVisible(true);
    }
}
