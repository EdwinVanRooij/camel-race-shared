package io.github.edwinvanrooij.camelraceshared.domain.mexican;

import io.github.edwinvanrooij.camelraceshared.domain.Game;
import io.github.edwinvanrooij.camelraceshared.domain.GameResults;
import io.github.edwinvanrooij.camelraceshared.domain.GameState;

/**
 * Created by eddy
 * on 7/27/17.
 */
public class MexicanGame extends Game {
    public MexicanGame(String id) throws Exception {
        super(id);
    }

    @Override
    protected void setInitialState() throws Exception {

    }

    @Override
    public GameState generateGameState() {
        return null;
    }

    @Override
    public GameResults generateGameResults() {
        return null;
    }
}
