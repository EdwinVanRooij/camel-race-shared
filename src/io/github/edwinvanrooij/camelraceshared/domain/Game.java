package io.github.edwinvanrooij.camelraceshared.domain;


import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by eddy
 * on 6/5/17.
 */
public class Game {

    private static AtomicInteger nextId = new AtomicInteger();

    private String id;
    private HashMap<Integer, Bid> bids; // player ID with bids
    private transient HashMap<Integer, Boolean> readyMap; // player ID with ready map
    private transient HashMap<Integer, Boolean> playAgainMap; // players who want to play again, true if they do
    private transient HashMap<Player, Long> aliveCheckMap; // player with last alive check timestamp

    private List<Player> players;

    private List<SideCard> sideCardList;
    private List<Camel> camelList;
    private List<Card> deck;

    private Card lastPickedCard;

    private boolean gameEnded;
    private Camel winner;

    private SideCard sideCardToTurn = null;

    public Camel getWinner() {
        return winner;
    }

    public List<SideCard> getSideCardList() {
        return sideCardList;
    }

    public List<Camel> getCamelList() {
        return camelList;
    }
    public String getId() {
        return id;
    }

    public synchronized List<Player> getPlayers() {
        return players;
    }

    public Game(String id) throws Exception {
        this.id = id;
        initVariables();
        setInitialState();

        int interval = 5; // in seconds
        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                try {
                    startAliveChecker();
                } catch (InterruptedException | NullPointerException e ) {
                    e.printStackTrace();
                }
            }
        };
        timer.schedule(task, 5000, interval * 1000);
    }

    /**
     * This will delete the inactive players which are still in the game
     *
     * @throws InterruptedException
     */
    private void startAliveChecker() throws InterruptedException {
        List<Player> playersToRemove = new ArrayList<>();
        for (Player player : getPlayers()) {

            long lastCheck = aliveCheckMap.get(player);
            long currentTime = System.nanoTime();
            long elapsedTimeInSeconds = (currentTime - lastCheck) / 1000000000;
            if (elapsedTimeInSeconds > 4) {
                playersToRemove.add(player);
            }
        }
        players.removeAll(playersToRemove);
    }

    public void playerAliveCheck(Player player) {
        aliveCheckMap.put(player, System.nanoTime());
    }

    public boolean everyoneIsReady() {
        try {
            for (Player player : players) {
                try {
                    // Check if every player has a positive entry in the ready map
                    if (!readyMap.get(player.getId())) {
                        // Player is not ready
                        return false;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            // Player was probably not once added to the map, meaning it's never been at true
            return false;
        }
        // Only if all checks were passed, everyone is really ready
        return true;
    }

    public void newBid(Player player, Bid bid) {
        bids.put(player.getId(), bid);
    }

    public Bid getBid(int playerId) {
        try {
            return bids.get(playerId);
        } catch (Exception e) {
            return null;
        }
    }

    public void ready(Player player, boolean ready) {
        readyMap.put(player.getId(), ready);
    }

    public Player addPlayer(Player player) {
        int uniqueId = nextId.incrementAndGet();

        Player playerWithId = new Player(uniqueId, player.getName());

        players.add(playerWithId);
        return playerWithId;
    }

    public Player getPlayer(int id) throws Exception {
        for (Player player : players) {
            if (player.getId() == id) {
                return player;
            }
        }
        throw new Exception("No player found with ID " + id);
    }

    public void restart() throws Exception {
        sideCardList.clear();
        camelList.clear();
        readyMap.clear();
        playAgainMap.clear();
        deck.clear();
        lastPickedCard = null;
        winner = null;
        gameEnded = false;
        sideCardToTurn = null;

        setInitialState();
    }

    public void playAgain(int playerId, boolean playAgain) {
        playAgainMap.put(playerId, playAgain);
    }

    /**
     * Checks if all players have signed up to play again.
     *
     * @return true if they all wanna play again
     */
    public boolean allPlayAgain() {
        // Check for every player if the play again is at true
        for (Player player : getPlayers()) {
            // If the player is not in the map, or the value is not at true, not everyone wants to play again
            try {
                if (!playAgainMap.get(player.getId())) {
                    return false;
                }
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }

    private void initVariables() {
        players = new ArrayList<>();
        bids = new HashMap<>();
        readyMap = new HashMap<>();
        playAgainMap = new HashMap<>();
        aliveCheckMap = new HashMap<>();

        sideCardList = new ArrayList<>();
        camelList = new ArrayList<>();
        deck = new ArrayList<>();
    }

    private void addCamels() {
        // Create and add camels to list
        Camel clubsCamel = new Camel(CardType.CLUBS, 0);
        Camel diamondsCamel = new Camel(CardType.DIAMONDS, 1);
        Camel heartsCamel = new Camel(CardType.HEARTS, 2);
        Camel spadesCamel = new Camel(CardType.SPADES, 3);
        camelList.add(clubsCamel);
        camelList.add(diamondsCamel);
        camelList.add(heartsCamel);
        camelList.add(spadesCamel);
    }

    private void initDeck() {
        // Create and fill the remaining deck
        List<Card> clubsDeck = new ArrayList<>();
        List<Card> diamondsDeck = new ArrayList<>();
        List<Card> heartsDeck = new ArrayList<>();
        List<Card> spadesDeck = new ArrayList<>();

        for (CardValue v : CardValue.values()) {
            // Don't add the ace to the (remaining) deck
            if (v == CardValue.ACE)
                continue;

            clubsDeck.add(new Card(CardType.CLUBS, v));
            diamondsDeck.add(new Card(CardType.DIAMONDS, v));
            heartsDeck.add(new Card(CardType.HEARTS, v));
            spadesDeck.add(new Card(CardType.SPADES, v));
        }

        // Add all small decks to the total
        deck.addAll(clubsDeck);
        deck.addAll(diamondsDeck);
        deck.addAll(heartsDeck);
        deck.addAll(spadesDeck);
    }

    private void initSideCards() throws Exception {
        // Take 4 random cards from the deck, fill in side cards
        for (int i = 1; i < 5; i++) {
            Card randomCard = pickCard();
            SideCard card = new SideCard(randomCard.getCardType(), randomCard.getCardValue(), i);
            sideCardList.add(card);
        }
        lastPickedCard = null;
    }

    private void setInitialState() throws Exception {
        addCamels();
        initDeck();
        initSideCards();
    }

    public Card pickCard() throws Exception {
        int randomNumber = new Random().nextInt(deck.size());

        // Get a non-empty card
        lastPickedCard = null;
        do {
            lastPickedCard = deck.get(randomNumber);
        } while (lastPickedCard == null);

        deck.remove(lastPickedCard);

        return lastPickedCard;
    }

    public Camel didCamelWinYet() throws Exception {
        Camel camel = getCamelByCardType(lastPickedCard.getCardType());
        if (camel.getPosition() + 1 > sideCardList.size()) {
            winner = camel;
            gameEnded = true;
            return camel;
        }
        return null;
    }

    public void moveCamelAccordingToLastCard() throws Exception {
        // Get the camel that matches this card
        CardType cardType = lastPickedCard.getCardType();
        Camel camel = getCamelByCardType(cardType);
        moveCamel(camel, true);
    }

    private void setCamelPosition(Camel camel) {
        for (int i = 0; i < camelList.size(); i++) {
            if (camelList.get(i).getCardType() == camel.getCardType()) {
                camelList.set(i, camel);
            }
        }
    }

    public boolean shouldTurnSideCard() throws Exception {
        int position = getCamelByCardType(lastPickedCard.getCardType()).getPosition();

        // Find card at this position
        for (SideCard card : sideCardList) {
            if (card.getPosition() == position) {

                // Check if card was turned around yet
                if (card.isVisible()) {
                    // Card is visible, do nothing to it
                    return false;
                }

                // Card is invisible
                for (Camel c : camelList) {
                    if (c.getPosition() < position) {
                        // There's a card below this position, don't turn around
                        return false;
                    }
                }

                // All camels have passed this position or are on it, move card around
                card.setVisible(true);
                sideCardToTurn = card;
                return true;
            }
        }
        return false;
    }

    public List<Camel> newCamelList() throws Exception {
        // Get the camel that matches this card
        CardType cardType = sideCardToTurn.getCardType();
        Camel camel = getCamelByCardType(cardType);

        moveCamel(camel, false);
        return camelList;
    }

    private void moveCamel(Camel camel, boolean forward) throws Exception {
        // Get camel position
        int position = camel.getPosition();

        // Put camel one position according to forward or not
        if (forward) {
            position++;
        } else {
            position--;
        }

        // If camel is now past the total size of side cards, it wins the game
        if (position > sideCardList.size()) {
            gameEnded = true;
            winner = camel;
            return;
        }

        // Set a new position for this camel
        camel.setPosition(position);

        // Update real camelList
        setCamelPosition(camel);
    }

    private Camel getCamelByCardType(CardType type) throws Exception {
        for (Camel c : camelList) {
            if (c.getCardType() == type) {
                return c;
            }
        }
        throw new Exception(String.format("Camel with type %s not found!", type.toString()));
    }

    public GameState generateGameState() {
        return new GameState(sideCardList, camelList, deck, lastPickedCard, winner, gameEnded);
    }

    public GameResults generateGameResults() {
        GameResults results = new GameResults(winner.getCardType());

        for (Player player : players) {
            ResultItem item = new ResultItem(player, bids.get(player.getId()));
            if (item.getBid() != null) {
                results.addResultItem(item);
            }
        }

        return results;
    }

}
