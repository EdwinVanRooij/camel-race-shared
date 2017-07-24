package io.github.edwinvanrooij.camelraceshared.domain;


/**
 * Created by eddy
 * on 6/8/17.
 */
public class PlayerAliveCheck {
     private String gameId;
     private Player player;

     public String getGameId() {
          return gameId;
     }

     public void setGameId(String gameId) {
          this.gameId = gameId;
     }

     public Player getPlayer() {
          return player;
     }

     public void setPlayer(Player player) {
          this.player = player;
     }

     public PlayerAliveCheck(String gameId, Player player) {
          this.gameId = gameId;
          this.player = player;
     }
}
