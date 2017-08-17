package io.github.edwinvanrooij.camelraceshared.domain.mexican;

import io.github.edwinvanrooij.camelraceshared.domain.Player;

/**
 * Created by eddy
 * on 8/17/17.
 */
public class MexicanEmulator {

    public MexicanEmulator() throws Exception {
        MexicanGame game = new MexicanGame("gameId");

        Player tom = new Player("Tom");
        Player fons = new Player("Fons");
        Player rik = new Player("Rik");
        Player bob = new Player("Bob");
        Player edwin = new Player("Edwin");

        game.addPlayer(tom);
        game.addPlayer(fons);
        game.addPlayer(rik);
        game.addPlayer(bob);
        game.addPlayer(edwin);

        game.setGameMode(MexicanGame.GameMode.NORMAL);

        game.generatePlayerOrder();

        Player p = game.getNextPlayer();
        System.out.println(String.format("Next p is %s", p.getName()));

        int maxThrows = game.getMaxThrows(p.getId());
        System.out.println(String.format("%s can throw %s times", p.getName(), maxThrows));

        Throw th = game.newPlayerThrow(p);
        System.out.println(String.format("Score: %s, threw %s and %s", th.getScore(), th.getFirstScore(), th.getSecondScore()));
        th = game.newPlayerThrow(p);
        System.out.println(String.format("Score: %s, threw %s and %s", th.getScore(), th.getFirstScore(), th.getSecondScore()));

        game.playerStops(p);

        int throwsLeft = game.getThrowsLeft(p.getId());
        System.out.println(String.format("%s can throw %s times", p.getName(), throwsLeft));

        Player p2 = game.getNextPlayer();
        int max = game.getMaxThrows(p2.getId());
        int left = game.getThrowsLeft(p2.getId());
        System.out.println(String.format("Next p to throw is %s, has %s max th and %s left.", p2.getName(), max, left));

        th = game.newPlayerThrow(p2);
        System.out.println(String.format("Score: %s, threw %s and %s", th.getScore(), th.getFirstScore(), th.getSecondScore()));

        left = game.getThrowsLeft(p2.getId());
        System.out.println(String.format("Next p to throw is %s, has %s max th and %s left.", p2.getName(), max, left));
    }

    public static void main(String[] args) {
        try {
            new MexicanEmulator();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
