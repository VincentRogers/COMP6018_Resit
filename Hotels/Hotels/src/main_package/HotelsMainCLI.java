package main_package;

import java.util.Scanner;

public class HotelsMainCLI {

    // Display player information
    public static void displayPlayer(String prefix, Model.Player player) {
        System.out.println(
                prefix + player.number + " " + player.name +
                " Balance: " + player.getBalance() +
                " Position: " + player.getPosition());
    }

    // Display tile information
    public static void displayTile(int position, Model.Property property) {
        String description = "Tile " + position;
        if (property == null) {
            description += " is unoccupied";
        } else {
            Model.Player owner = property.getOwner();
            description +=
                " Property " + property.name +
                " Cost: " + property.cost +
                " Rating: " + property.getRating();
            if (owner != null) {
                description += " Owner: " + owner.name;
            } else {
                description += " (no owner)";
            }
        }
        System.out.println(description);
    }

    // Display current player status
    public static void displayStatus(Model model) {
        // Obtain the current player, position and property
        Model.Player currentPlayer = model.getCurrentPlayer();
        int position = currentPlayer.getPosition();
        Model.Property property = model.getProperty(position);

        // Describe the current player's name, balance and position
        System.out.println("===");
        displayPlayer("Current player: ", currentPlayer);

        // Display the current player's tile
        displayTile(position, model.getProperty(position));

        // Display any other players on the same tile
        for (int i = 0; i < model.nPlayers; i++) {
            Model.Player player = model.getPlayer(i + 1);
            if (player.getPosition() == position && player != currentPlayer) {
                displayPlayer("Other player: ", player);
            }
        }
    }

    // Main game loop - returns the winner of the game when over
    public static Model.Player mainLoop(Scanner entry, Model model) {
        while (true) {
            String command;

            Model.Player winner = null;

            boolean moved = false;

            do {
                displayStatus(model);

                // Prompt the user for a pre-move command
                if (model.cheatMode) {
                	System.out.print("Command (roll/setmove/players/tiles): ");
                }
                else {
                	System.out.print("Command (roll/players/tiles): ");
                }
                // Parse the command
                command = entry.nextLine();

                // Normal move - roll the dice
                if (command.equals("roll") || command.equals("")) {
                	model.movePlayer();
                    System.out.println("rolled a: " + model.diceRoll);
                    moved = true;

                // Move to specific tile for testing purposes
                } else if (command.equals("setmove")) {
                	if (model.cheatMode) {
                		System.out.print("position (must be within 12 tiles): ");
                    	int position = entry.nextInt();
                    	entry.nextLine();
                    	if (position < 0 || !model.accessibleTiles(position)) {
                    		System.out.println("Invalid position!");
                    	} else {
                        	model.movePlayer(position);
                        	moved = true;
                    	}
                	}
                	else {
                		System.out.println("Cheat mode not enabled.");
                	}

                // Display all the players
                } else if (command.equals("players")) {
                    for (int i = 0; i < model.nPlayers; i++) {
                        Model.Player player = model.getPlayer(i + 1);
                        displayPlayer("Player: ", player);
                    }

                // Display all the tiles
                } else if (command.equals("tiles")) {
                    for (int i = 0; i < Model.nTiles; i++) {
                        displayTile(i, model.getProperty(i));
                    }

                // Invalid command
                } else {
                    System.out.println("Invalid command!");
                }

            } while (!moved);

            winner = model.getWinner();
            if (winner != null) return winner;

            int action;
            do {
                // Display status
                displayStatus(model);

                // Default action is to end the turn
                action = Model.EndTurn;

                // Get the possible actions the player can take
                int actions = model.actions();

                // Only prompt if there are multiple possible actions
                if (actions != Model.EndTurn) {

                    // Describe the possible actions
                    String commands = "end";
                    if ((actions & Model.BuyProperty) != 0) {
                        commands += "/buy";
                    }
                    if ((actions & Model.UpgradeProperty) != 0) {
                        commands += "/upgrade";
                    }
                    System.out.print("Command (" + commands + "): ");

                    // Parse the command
                    command = entry.nextLine();
                    command.trim();

                    // End turn
                    if (command.equals("end") || command.equals("")) {
                        action = Model.EndTurn;

                    // Buy property
                    } else if (command.equals("buy") &&
                            (actions & Model.BuyProperty) != 0) {
                        action = Model.BuyProperty;

                    // Upgrade property
                    } else if (command.equals("upgrade") &&
                            (actions & Model.UpgradeProperty) != 0) {
                        action = Model.UpgradeProperty;

                    // Invalid command
                    } else {
                        System.out.println("Invalid command!");
                        action = 0;
                    }
                }

                // Perform the action
                if (action != 0) {
                    model.perform(action);
                }
            } while (action != Model.EndTurn);
        }
    }

    // Main program
    public static void main(String[] args) {
        System.out.println("CLI Version of Hotels.");

        Scanner entry = new Scanner(System.in);
        String command;
        Model model;

        {
            int nPlayers = 2;
            int startingBalance = 2000;

            System.out.print("Create custom game? [y/n]: ");
            command = entry.nextLine();
            command.trim();
            // Custom game settings
            if (command.equals("y") || command.equals("Y")) {
                System.out.print("Enter number of players: ");
                nPlayers = entry.nextInt();
                entry.nextLine();

                System.out.print("Enter starting balance: ");
                startingBalance = entry.nextInt();
                entry.nextLine();
                // Default game settings
            } else {
                System.out.println("Creating game with default settings.");
            }

            // Create model
            model = new Model(nPlayers);

            // Create players
            System.out.print("Create custom players? [y/n]: ");
            command = entry.nextLine();
            command.trim();

            // Custom player names
            if (command.equals("y") || command.equals("Y")) {
                for (int i = 0; i < nPlayers; i++) {
                    System.out.print("Enter player name: ");
                    String name = entry.nextLine();
                    name.trim();
                    System.out.print("Creating player: " + name);
                    model.addPlayer(i + 1, name, startingBalance);
                }
            // Default player names
            } else {
                for (int i = 0; i < nPlayers; i++) {
                    String name = "p" + (i + 1);
                    System.out.println("Creating player: " + name);
                    model.addPlayer(i + 1, name, startingBalance);
                }
            }
            
            System.out.print("Enable cheat mode? [y/n]: ");
            command = entry.nextLine();
            command.trim();
            if (command.equals("y") || command.equals("Y")) {
            	model.toggleCheatMode();
            }
        }

        Model.Player winner = mainLoop(entry, model);
 
        // We have a winner!
        for (int i = 0; i < model.nPlayers; i++) {
            Model.Player player = model.getPlayer(i + 1);
            displayPlayer("Player: ", player);
        }
        System.out.println("Winner is " + winner.name + "!");
    }
}
