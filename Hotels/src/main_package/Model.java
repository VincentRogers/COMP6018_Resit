package main_package;

// Responsible for logic

import java.util.Observable;
import java.lang.ref.WeakReference;
import java.util.concurrent.ThreadLocalRandom;

public class Model extends Observable {

    // Player actions, encoded as constants that are bits in a bitfield
    // Note: an enumeration is not used because Java enums are not
    // easily composed into bitmaps
    public static final int EndTurn = 1;
    public static final int BuyProperty = 2;
    public static final int UpgradeProperty = 4;
    public static final int RollDie = 8;
    
    // Various game constants
    public static final int nTiles = 40;
    public static final int nGroups = 8;

    // Player encapsulates the state of each player
    public static class Player {
        public final int number;  // Player Number
        public final String name; // Player Name
        private int balance;      // Player Balance
        private int position;     // Player Position
        private boolean hasMoved; // has the Player moved
        
        public Player(int number, String name, int balance) {
            this.number = number;
            this.name = name;
            this.balance = balance;
            // Player position always starts on Go tile, which is tile 0
            this.position = 0;
            this.hasMoved = false;
        }

        // Obtain current balance
        public int getBalance() { return balance; }

        // Pay rent / purchase / upgrade
        public void pay(int price) { balance -= price; }

        // Earn rent
        public void earn(int price) { balance += price; }

        // Obtain current position
        public int getPosition() { return position; }

        // Move player to position
        public void move(int position) { this.position = position; }

        // Returns true if the player is not bankrupt
        public boolean active() { return balance > 0; }
    }

    // Group encapsulates a property group
    public static class Group {
        // properties in group
        private Object[] properties;

        public Group() { properties = new Object[3]; }

        // add a property to the group
        public void add(Object property, int number) {
            assert number >= 1 && number <= 3:
                "Invalid property number " + number;
            properties[number - 1] = property;
        }
        // get a property within the group
        public Object property(int number) {
            assert number >= 1 && number <= 3:
                "Invalid property number " + number;
            return properties[number - 1];
        }
    }

    // Property encapsulates a property on a tile
    public static class Property {
        public final String name; // Property name
        public final WeakReference<Group> group; // Property groups, A, B, etc.
        public final int number;  // Property Number
        public final int cost;    // Property Price to Buy
        private Player owner;     // Property Owner
        private int rating;       // Rating 0 - 5

        public Property(String name, Group group, int number, int cost) {
            this.name = name;
            this.group = new WeakReference<>(group);
            this.number = number;
            this.cost = cost;
            this.owner = null;
            this.rating = 0;

            group.add(this, number);
        }

        public Player getOwner() { return owner; } // get the current owner
        public int getRating() { return rating; } // get the current rating

        // own the property
        public void own(Player player) {
            assert this.owner == null:
                "Invalid attempt to buy the same property twice";
            this.owner = player;
        }

        // upgrade the property
        public void upgrade() {
            assert this.rating < 5:
                "Invalid attempt to upgrade a property above 5 stars";
            ++this.rating;
        }
    }

    public final int nPlayers;   // number of players
    private int activePlayers;   // number of active players
    private int currentPlayer;   // currently active player
    private int winner;          // winner of the game (-1 means no winner)
    private Player[] players;    // array of nPlayers players
    private Group[] groups;      // array of nGroups property groups
    private Property[] tiles;    // array of nTiles tiles
    private int[] changedTiles;  // changed tiles
    public boolean cheatMode;	 // if player has access to cheat move
    public int diceRoll;		 // the die roll
    
    // Get the group given a group name
    public Group group(char group) {
        assert group >= 'A' && group <= 'H':
            "Invalid property group " + group;
        return groups[(int)group - (int)'A'];
    }

    // Initialize the model
    public Model(int nPlayers) {
        // Initialize players
        assert nPlayers >= 2 && nPlayers < 4:
            "Invalid number of players " + nPlayers;
        this.nPlayers = nPlayers;
        this.activePlayers = nPlayers;
        this.currentPlayer = 0;
        this.winner = -1;
        this.players = new Player[nPlayers];
        this.cheatMode = false;
        this.diceRoll = -1;
        
        // Initialize groups
        this.groups = new Group[nGroups];

        for (int i = 0; i < nGroups; i++) {
            this.groups[i] = new Group();
        }

        // Build board and properties
        assert nTiles == 40: "Invalid nTiles " + nTiles;
        this.tiles = new Property[nTiles];
        this.changedTiles = new int[2];
        this.changedTiles[0] = -1;
        this.changedTiles[1] = -1;

        this.tiles[1] = new Property("A1", group('A'), 1, 50);
        this.tiles[3] = new Property("A2", group('A'), 2, 50);
        this.tiles[4] = new Property("A3", group('A'), 3, 70);

        this.tiles[6] = new Property("B1", group('B'), 1, 100);
        this.tiles[8] = new Property("B2", group('B'), 2, 100);
        this.tiles[9] = new Property("B3", group('B'), 3, 120);

        this.tiles[11] = new Property("C1", group('C'), 1, 150);
        this.tiles[13] = new Property("C2", group('C'), 2, 150);
        this.tiles[14] = new Property("C3", group('C'), 3, 170);

        this.tiles[16] = new Property("D1", group('D'), 1, 200);
        this.tiles[18] = new Property("D2", group('D'), 2, 200);
        this.tiles[19] = new Property("D3", group('D'), 3, 220);

        this.tiles[21] = new Property("E1", group('E'), 1, 250);
        this.tiles[23] = new Property("E2", group('E'), 2, 250);
        this.tiles[24] = new Property("E3", group('E'), 3, 270);

        this.tiles[26] = new Property("F1", group('F'), 1, 300);
        this.tiles[28] = new Property("F2", group('F'), 2, 300);
        this.tiles[29] = new Property("F3", group('F'), 3, 320);

        this.tiles[31] = new Property("G1", group('G'), 1, 350);
        this.tiles[33] = new Property("G2", group('G'), 2, 350);
        this.tiles[34] = new Property("G3", group('G'), 3, 370);

        this.tiles[36] = new Property("H1", group('H'), 1, 400);
        this.tiles[38] = new Property("H2", group('H'), 2, 400);
        this.tiles[39] = new Property("H3", group('H'), 3, 420);
    }

    // Add Player
    public void addPlayer(int number, String name, int balance) {
        assert number >= 1 && number <= this.nPlayers:
            "Invalid player number " + number;
        assert balance > 0: "Invalid balance " + balance;
        players[number - 1] = new Player(number, name, balance);
    }

    // Get current player
    public Player getCurrentPlayer() {
        return this.players[currentPlayer];
    }

    // Get player by number
    public Player getPlayer(int number) {
        assert number >= 1 && number <= this.nPlayers:
            "Invalid player number " + number;
        return this.players[number - 1];
    }

    // Get property at tile position
    public Property getProperty(int position) {
        assert position >= 0 && position < this.nTiles:
            "Invalid tile position";
        return this.tiles[position];
    }

    // Handle changed tiles
    interface TileVisitor {
        void process(int position);
    }
    public void allChangedTiles(TileVisitor lambda) {
        for (int i = 0; i < 2; i++) {
            if (changedTiles[i] != -1) {
                lambda.process(changedTiles[i]);
                changedTiles[i] = -1;
            }
        }
    }

    // Calculate the price for a player to stay at a property
    // Star Rating goes up every time the hotel is upgraded.
    // Upgrade cost is 50% of the original purchase price
    // If owned by the other player, the overnight fee is 10% of the
    //   purchase price * the square of the star rating
    // If the paying player owns either (or both) of the other two hotels
    //   in the same group, the price is halved
    // If the owning player owns both of the other two hotels in the same letter
    //   group, the price is doubled
    private static int calcPrice(Property property, Player player) {
        // If the tile is unoccupied, no rent is due
        if (property == null) {
            return 0;
        }
        Player owner = property.getOwner();
        // If the property is not owned, or the player is the owner,
        // the price is zero
        if (owner == null || owner == player) {
            return 0;
        }
        // The price is 10% of the purchase price * square of the rating
        int rating = property.getRating();
        int price = (property.cost / 10) * rating * rating;
        // Get the owners of the other properties in the same group
        Player other1 = null, other2 = null;
        Group group = property.group.get();
        assert group != null: "Invalid property group";
        assert property.number >= 1 && property.number <= 3:
            "Invalid property number";
        switch (property.number) {
            case 1:
                other1 = ((Property)group.property(2)).owner;
                other2 = ((Property)group.property(3)).owner;
                break;
            case 2:
                other1 = ((Property)group.property(1)).owner;
                other2 = ((Property)group.property(3)).owner;
                break;
            case 3:
                other1 = ((Property)group.property(1)).owner;
                other2 = ((Property)group.property(2)).owner;
                break;
        }
        // Double the price if all same owner
        if (owner == other1 && owner == other2) {
            price *= 2;
        // Half if the player owns another property within the same group
        } else if (player == other1 || player == other2) {
            price /= 2;
        }
        return price;
    }

    // Roll Die
    private int rollDie() {
    	diceRoll = ThreadLocalRandom.current().nextInt(1, 12 + 1); 
        return diceRoll;
    }
    
    public boolean accessibleTiles(int desired_position) {
    	Player player = this.players[currentPlayer];
    	int current_position = player.position;
    	int potential_position = current_position;
    	for(int i = 0; i != 13; i++) {
    		potential_position = current_position + i;
    		if (potential_position > tiles.length) {
    			potential_position = 0;
    		}
    		if (desired_position == potential_position) {
    			return true;
    		}
    	}
    	return false;
    }
    
    // Toggle cheat mode
    public void toggleCheatMode() {
    	cheatMode = !cheatMode;
    	setChanged();
        notifyObservers();
    }
    
    // Move player
    public void movePlayer() {
        Player player = this.players[currentPlayer];

        int position = player.getPosition() + rollDie();
        if (position >= nTiles) {
            position -= nTiles;
        }
        movePlayer(position);
    }

    // Move player. If the game is won return the winner, or null otherwise
    public void movePlayer(int position) {
        Player player = this.players[currentPlayer];

        // Remember which tiles are changing
        this.changedTiles[0] = player.getPosition();
        this.changedTiles[1] = position;

        // Move the player
        player.move(position);
        Property property = this.tiles[position];

        // Pay any rent due
        int rent = calcPrice(property, player);
        if (rent > 0) {
            player.pay(rent);
            property.getOwner().earn(rent);
        }

        // Update active players if the player went bankrupt
        if (!player.active()) {
            --activePlayers;

            // If only one player is left, they are the winner
            if (activePlayers == 1) {
                for (int i = 0; i < nPlayers; i++) {
                    if (this.players[i].active()) {
                        this.winner = i;
                        break;
                    }
                }
                assert this.winner != -1: "Invalid active players count";
            }
        }
        this.players[currentPlayer].hasMoved = true;
        
        // Refresh view
        setChanged();
        notifyObservers();
    }

    // Get winning player, or -1 if none yet
    public Player getWinner() {
        if (this.winner == -1) {
            return null;
        }
        assert this.winner >= 0 && this.winner < this.nPlayers:
            "Invalid winner";
        return this.players[this.winner];
    }

    // Return a bitfield representing which player actions are available
    public int actions() {
        Player player = this.players[currentPlayer];

        // If there is a winner, the game is over
        if (this.activePlayers == 1) {
            return 0;
        }

        // If the player is bankrupt, end the turn
        if (!player.active()) {
            return EndTurn;
        }

        Property property = this.tiles[player.getPosition()];

        // If no property on the current tile and the player has moved, end the turn
        if (property == null && player.hasMoved) { 
            return EndTurn;
        }
        
        // If no property on the current tile and the player has NOT moved, roll die
        if (property == null && !player.hasMoved) {
        	return RollDie;
        }

        Player owner = property.getOwner();

        // If the property is owned by another player, end the turn
        if (owner != null && owner != player && player.hasMoved) {
            return EndTurn;
        }

        // The property is not owned by anyone AND has moved
        if (owner == null && player.hasMoved) {
            // If the player can't afford the property, don't permit buying it
            if (player.getBalance() < property.cost) {
                return EndTurn;
            }
            return EndTurn + BuyProperty;
        }

        // The property is owned by the player; check if the rating is < 5
        if (property.getRating() < 5 && player.hasMoved) {
            // If the player can't afford the upgrade, don't permit it
            if (player.getBalance() < (property.cost / 2)) {
                return EndTurn;
            }
            return EndTurn + UpgradeProperty;
        }

        // The property is owned by the player and already 5 stars
        if (player.hasMoved) {
        	return EndTurn;
        }
        
        // The player has not moved yet
        return RollDie;
    }

    // Perform a player action within a turn
    public void perform(int action) {
        assert activePlayers >= 2: "Invalid active player count";

        assert
        	action == RollDie ||
            action == EndTurn ||
            action == BuyProperty ||
            action == UpgradeProperty:
            "Invalid action " + action;

        Player player = players[currentPlayer];
        int position = player.getPosition();
        Property property = this.tiles[position];

        this.changedTiles[0] = -1;
        this.changedTiles[1] = -1;

        switch (action) {
        	case RollDie:
        		assert !player.hasMoved:
        			"Player has already moved";
        		movePlayer();
        		break;
        		
            case EndTurn:               // End turn
                do {
                	this.players[currentPlayer].hasMoved = false;
                    ++currentPlayer;
                    if (currentPlayer >= nPlayers) {
                        currentPlayer = 0;
                    }
                } while (!players[currentPlayer].active());
                break;

            case BuyProperty:           // Buy property
                assert property != null:
                    "Invalid action - property is null";
                assert player.getBalance() >= property.cost:
                    "Invalid action - insufficient balance to buy property";
                player.pay(property.cost);
                property.own(player);
                this.changedTiles[0] = position;
                break;

            case UpgradeProperty:       // Upgrade property
                assert property != null:
                    "Invalid action - property is null";
                assert player.getBalance() >= (property.cost / 2):
                    "Invalid action - insufficient balance to upgrade property";
                assert property.getRating() < 5:
                    "Invalid action - property already upgraded to 5 stars";
                player.pay(property.cost / 2);
                property.upgrade();
                this.changedTiles[0] = position;
                break;
        }

        // Refresh view
        setChanged();
        notifyObservers();
    }
}
