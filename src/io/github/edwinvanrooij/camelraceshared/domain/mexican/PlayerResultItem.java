package io.github.edwinvanrooij.camelraceshared.domain.mexican;

import io.github.edwinvanrooij.camelraceshared.domain.Player;

/**
 * Created by eddy
 * on 8/24/17.
 */
public class PlayerResultItem {
    private Player player;
    private int score;

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public PlayerResultItem(Player player, int score) {
        this.player = player;
        this.score = score;
    }
}
