package main_package;

// Initializes mouse click handlers

public class Controller {
    private Model model;
    private View view;
    private boolean firstTurn;

    public Controller(Model model, View view) {
        this.model = model;
        this.view = view;
        this.firstTurn = true;

        // End Turn 
        View.ClickHandler nextTurnHandler = (button) -> {
            this.model.perform(Model.EndTurn);
        };
        view.handleClick(View.EndTurn, nextTurnHandler);
        
        // Roll Die
        view.handleClick(View.RollDie, (button) -> {
        	this.model.movePlayer();
        });

        // Buy property button
        view.handleClick(View.BuyProperty, (button) -> {
            this.model.perform(Model.BuyProperty);
        });

        // Upgrade property button
        view.handleClick(View.UpgradeProperty, (button) -> {
            this.model.perform(Model.UpgradeProperty);
        });
        
        // Cheat mode button
        view.handleClick(View.CheatMode, (button) -> {
            this.model.toggleCheatMode();
        });
        
        // Set Move button
        view.handleClick(View.SetMove, (button) -> {
        	int entry = this.view.setMove(this.model);
        	if (entry == -1) {
        		entry = this.model.getCurrentPlayer().getPosition();
        	}
        	System.out.println(entry);
        	this.model.movePlayer(entry);
        });
    }
}
