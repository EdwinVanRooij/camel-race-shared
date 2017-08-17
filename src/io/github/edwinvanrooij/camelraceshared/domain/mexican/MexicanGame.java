package io.github.edwinvanrooij.camelraceshared.domain.mexican;

import com.sun.xml.internal.ws.api.message.ExceptionHasMessage;
import io.github.edwinvanrooij.camelraceshared.domain.Game;
import io.github.edwinvanrooij.camelraceshared.domain.GameResults;
import io.github.edwinvanrooij.camelraceshared.domain.GameState;
import io.github.edwinvanrooij.camelraceshared.domain.Player;

import java.util.*;

/**
 * Created by eddy
 * on 7/27/17.
 */

// Voor spel:
//
// 2-..* players
// 2 dices
// 2 opties: easy/hardcore, decided by players
//
//
// Worpen:
//
// Random speler begint met gooien
// Aantal keer gooien hangt af van mode, normaal - 3x max, hardcore - 1x max
// De 'pot' staat op het scherm, begint op basis van mode - normaal - 2x, hardcore - 4x
//
// Normale worpen:
// enkele, 45 wordt 54
// dubbele, 33 wordt 300
//
// Speciale worpen:
// - 21 - hoogste, pot verdubbelt, klaar met beurt
// - 31 - je bent nu drieman, je gooi telt NIET als beurt, en je mag verder gaan
// - 11 - je bent nu honderdman, je gooi telt WEL als beurt, je mag verder proberen als je nog beurten over hebt
//
//
// Spelverloop:
//
// - Eerste speler mag x aantal keren gooien, mag eerder stoppen dan max aantal beurten die over zijn. Wanneer deze eerder stopt, mag de rest na hem/haar een gelijk aantal beurten gooien.
// - Wanneer er een drieman aanwezig is en er wordt een getal gegooid waar een 3 in zit, bijvoorbeeld 43, drinkt de drieman een slok.
// - Wanneer er een honderdman aanwezig is en er wordt een honderdtal gegooid, bijvoorbeeld 300, drinkt de honderdman een slok.
//
// Speciale gevallen hierop:
// - Wanneer er een nieuwe honderdman of drieman komt, vervalt de oude -man
// METEEN, de nieuwe -man drinkt de slokken.
//
// Iedereen heeft gegooid? Degene die het laagste getal gegooid heeft, drinkt de huidige 'pot' in slokken op.
//
// Opmerkingen:
// - Je gooit alle dobbelstenen opnieuw, of geen. Niet eentje.

public class MexicanGame extends Game {

    private Map<Integer, Throw> playerThrowMap; // (player id, throw)
    private Map<Integer, Integer> playersThrowsLeftMap; // (player id, maxTurns)
    private Map<Integer, Integer> playersMaxThrowsMap; // (player id, turnsLeft)

    private int firstPlayerId;

    private DiceThrower thrower;
    private GameMode gameMode;
    private int maxThrows;
    private int stake;

    private List<Integer> playerOrder; // player IDs
    private int playerOrderTurn;

    private static Random random = new Random();

    public MexicanGame(String id) throws Exception {
        super(id);
    }

    @Override
    protected void setInitialState() throws Exception {
        thrower = new DiceThrower();
        playerOrderTurn = 0;
        playerThrowMap = new HashMap<>();
        playersMaxThrowsMap = new HashMap<>();
        playersThrowsLeftMap = new HashMap<>();
        firstPlayerId = 0;
    }

    public Throw newPlayerThrow(Player p) throws Exception {
        int throwsLeft = playersThrowsLeftMap.get(p.getId());
        if (throwsLeft <= 0) {
            throw new Exception("Player is out of throws! Should not have been able to get this far, turn should be passed on to next person before this happens.");
        }
        playersThrowsLeftMap.put(p.getId(), throwsLeft - 1);
        Throw th = thrower.generateThrow();
        playerThrowMap.put(p.getId(), th);

        // Check if the first person is done throwing
        if (p.getId() == firstPlayerId && throwsLeft - 1 == 0) {
            onFirstPlayerDoneThrowing();
        }
        return th;
    }

    public void playerStops(Player p) throws Exception {
        if (p.getId() == firstPlayerId) {
            onFirstPlayerDoneThrowing();
        }
    }

    public void setGameMode(GameMode mode) throws Exception {
        gameMode = mode;
        setModeVariables(mode);
    }

    private void setModeVariables(GameMode mode) throws Exception {
        switch (mode) {
            case NORMAL:
                maxThrows = 3;
                stake = 2;
                break;
            case HARDCORE:
                maxThrows = 1;
                stake = 4;
                break;
            default:
                throw new Exception("Could not determine what gamemode this is");
        }
    }

    public void generatePlayerOrder() {
        playerOrder = new ArrayList<>();

        // Add a player to the player order
        do {
            // Get a new random index
            int index = random.nextInt(players.size());
            // Get a random player from the player list
            Player p = players.get(index);

            // If the player is not in the order yet, add it
            if (!playerIsInOrder(p)) {
                playerOrder.add(p.getId());
            }
        }
        // Do all of this, while the order does not have all of the players in the order yet
        while (playerOrder.size() < players.size());

        printPlayerOrder();
    }


    private void printPlayerOrder() {
        System.out.println("-------------");
        System.out.println("Player order");
        System.out.println("-------------");
        for (int order : playerOrder) {
            System.out.println(order);
        }
    }

    public int getThrowsLeft(int playerId) {
        return playersThrowsLeftMap.get(playerId);
    }

    public int getMaxThrows(int playerId) {
        return playersMaxThrowsMap.get(playerId);
    }

    public Player getNextPlayer() throws Exception {
        if (playerOrderTurn == 0) {
            onFirstPlayerWillThrow();
        } else if (playerOrderTurn > 0) {
            onAnotherPlayerWillThrow();
        } else {
            throw new Exception("Player order turn is below 0, should be impossible");
        }

        return getPlayer(playerOrder.get(playerOrderTurn++));
    }

    private void onFirstPlayerWillThrow() throws Exception {
        firstPlayerId = getPlayer(playerOrder.get(playerOrderTurn)).getId();

        playersThrowsLeftMap.put(firstPlayerId, maxThrows);
        playersMaxThrowsMap.put(firstPlayerId, maxThrows);
    }

    private void onFirstPlayerDoneThrowing() throws Exception {
        System.out.println("First person done throwing");

        int maxThrowsOfFirstPlayer = playersMaxThrowsMap.get(firstPlayerId);
        int throwsLeftOfFirstPlayer = playersThrowsLeftMap.get(firstPlayerId);

        // If the first player only got to throw once, it's once for all
        if (maxThrowsOfFirstPlayer == 1) {
            setThrowsForOtherPlayers(1);
            return;
        }

        // If the first player used all of the throws, set them all for all
        if (throwsLeftOfFirstPlayer == 0) {
            setThrowsForOtherPlayers(maxThrowsOfFirstPlayer);
            return;
        }

        // If the first player did not use all of the throws, subtract the throws left from the max
        if (throwsLeftOfFirstPlayer > 0) {
            int newThrowsForAll = maxThrowsOfFirstPlayer - throwsLeftOfFirstPlayer;
            setThrowsForOtherPlayers(newThrowsForAll);
            return;
        }

        throw new Exception("Could not determine a way to set other player throws after the first player threw.");
    }

    private void onAnotherPlayerWillThrow() throws Exception {

    }

    private void setThrowsForOtherPlayers(int amountOfThrows) {
        for (Player p : players) {
            if (p.getId() == firstPlayerId) {
                continue;
            }
            playersMaxThrowsMap.put(p.getId(), amountOfThrows);
            playersThrowsLeftMap.put(p.getId(), amountOfThrows);
        }
    }

    private boolean playerIsInOrder(Player player) {
        for (int playerId : playerOrder) {
            if (playerId == player.getId()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public GameState generateGameState() {
        return null;
    }

    @Override
    public GameResults generateGameResults() {
        return null;
    }

    public enum GameMode {
        NORMAL,
        HARDCORE
    }
}
