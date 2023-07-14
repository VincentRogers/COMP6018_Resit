package main_package;

import java.util.Observable;

/// Responsible for UI elements

import java.util.Observer;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class View extends JFrame implements Observer {

    // Button numbers
    public final static int RollDie = 0;
    public final static int EndTurn = 1;
    public final static int BuyProperty = 2;
    public final static int UpgradeProperty = 3;

    // Tile in the view
    public static class Tile {
        public final int position;              // Position of tile in Model
        public final JPanel panel;              // Panel for tile
        public final JLabel propertyLabel;      // Label displaying property
        public final JLabel playersLabel;       // Label displaying players

        public Tile(int position) {
            this.position = position;
            this.panel = new JPanel();
            this.panel.setLayout(
                    new BoxLayout(this.panel, BoxLayout.PAGE_AXIS));
            this.panel.add(this.propertyLabel = new JLabel(" "));
            this.panel.add(this.playersLabel = new JLabel(" "));
        }

        void refresh(Model model) {
            Model.Property property = model.getProperty(this.position);
            {
                if (property != null)  {
                    Model.Player owner = property.getOwner();
                    String info =
                        property.name +
                        " " + property.cost +
                        " " + property.getRating() + "*";
                    if (owner != null) {
                        info += " [" + owner.number + "]";
                    } else {
                        info += " []";
                    }
                    this.propertyLabel.setText(info);
                } else {
                    this.propertyLabel.setText("          ");
                }
            }
            {
                String info = "";
                for (int i = 0; i < model.nPlayers; i++) {
                    Model.Player player = model.getPlayer(i + 1);
                    if (player.getPosition() == this.position) {
                        if (!info.equals("")) {
                            info += " ";
                        }
                        info += player.number;
                    }
                }
                if (info.equals("")) {
                    info = " ";
                }
                this.playersLabel.setText(info);
            }
        }
    }

    private Tile[] tiles;               // View tiles, indexed by position
    private JLabel[] players;           // Player JLabels
    private JButton[] buttons;          // Action JButtons
    private JLabel playerTurn;			// Current player turn

    public View(Model model) {
        assert Model.nTiles == 40: "Invalid model nTiles";

        setTitle("Hotels Board");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Create the tile panels array
        this.tiles = new Tile[40];
 
        // Create the player panels array
        this.players = new JLabel[4];
        
        // Create the button panels array
        this.buttons = new JButton[4];
        
        // Current player turn
        this.playerTurn = new JLabel(model.getCurrentPlayer().name);
 
        // Create the board and tiles using a GridBagLayout
        JPanel boardPanel = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();

        // Set up the constraints
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.fill = GridBagConstraints.BOTH;
        constraints.ipadx = 8;
        constraints.ipady = 8;

        // Draw the top row
        for (int i = 0; i < 11; i++) {
            constraints.gridx = i;
            boardPanel.add((this.tiles[20 + i] = new Tile(20 + i)).panel,
                    constraints);
        }

        // Draw the left column
        constraints.gridx = 0;
        for (int i = 0; i < 9; i++) {
            constraints.gridy = i + 1;
            boardPanel.add((this.tiles[19 - i] = new Tile(19 - i)).panel,
                    constraints);
        }
        
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        constraints.gridy = 1;
        constraints.gridx = 5;
        boardPanel.add(this.playerTurn, constraints);
        
        // Draw the player info and buttons in the middle
        constraints.gridwidth = 2;
        constraints.gridheight = 2;
        constraints.gridy = 3;
        for (int i = 0; i < 4; i++) {
            constraints.gridx = i * 2 + 1;
            boardPanel.add(this.players[i] = new JLabel(), constraints);
        }
        
        constraints.gridy = 6;
        constraints.gridheight = 1;
        {
            String[] buttonNames = new String[4];
            buttonNames[RollDie] = "Roll Die";
            buttonNames[EndTurn] = "End Turn";
            buttonNames[BuyProperty] = "Buy";
            buttonNames[UpgradeProperty] = "Upgrade";
            for (int i = 0; i < 4; i++) {
                constraints.gridx = i * 2 + 1;
                boardPanel.add(this.buttons[i] = new JButton(buttonNames[i]),
                        constraints);
            }
        }
 
        // Draw the right column
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        constraints.gridx = 10;
        for (int i = 0; i < 9; i++) {
            constraints.gridy = i + 1;
            boardPanel.add((this.tiles[31 + i] = new Tile(31 + i)).panel,
                    constraints);
        }

        // Draw the bottom row
        constraints.gridy = 10;
        for (int i = 0; i < 11; i++) {
            constraints.gridx = i;
            boardPanel.add((this.tiles[10 - i] = new Tile(10 - i)).panel,
                    constraints);
        }

        for (int i = 0; i < 40; i++) {
            this.tiles[i].panel.setBorder(
                    BorderFactory.createLineBorder(Color.BLACK));
            this.tiles[i].refresh(model);
        }
        
        getContentPane().add(boardPanel);

        refresh(model);

        // Observe changes in the model
        model.addObserver(this);

        pack();
        setLocationRelativeTo(null);
    }

    @Override
    public void update(Observable object, Object arg) {
        if (object instanceof Model) {
            Model model = (Model)object;
            refresh(model);
        }
    }

    public void refresh(Model model) {
        // Update tiles
        model.allChangedTiles((position) -> {
            this.tiles[position].refresh(model);
        });
        
        // Update player turn
        String playerTurn = model.getCurrentPlayer().name;
        JLabel playerLabel = this.playerTurn;
        playerLabel.setText(playerTurn + "'s turn.");
        
        // Update players
        Model.Player winner = model.getWinner();
        for (int i = 0; i < model.nPlayers; i++) {
            Model.Player player = model.getPlayer(i + 1);
            JLabel label = this.players[player.number - 1];
            label.setText(
                player.number + " " + player.name + " " + player.getBalance());
            if (player == winner) {
                label.setForeground(Color.RED);
            }
        }

        // Update actions
        int actions = model.actions();
        this.buttons[0].setEnabled((actions & Model.RollDie) != 0);
        this.buttons[1].setEnabled((actions & Model.EndTurn) != 0);
        this.buttons[2].setEnabled((actions & Model.BuyProperty) != 0);
        this.buttons[3].setEnabled((actions & Model.UpgradeProperty) != 0);
    }

    // controller interface
    interface ClickHandler {
        void process(int button);
    }
    private static class ButtonListener implements ActionListener {
        private final int button;
        private final ClickHandler handler;

        ButtonListener(int button, ClickHandler handler) {
            this.button = button;
            this.handler = handler;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            this.handler.process(this.button);
        }
    }
    public void handleClick(int button, ClickHandler handler) {
        assert button >= 0 && button < 4: "Invalid button";
        this.buttons[button].addActionListener(
                new ButtonListener(button, handler));
    }
}
