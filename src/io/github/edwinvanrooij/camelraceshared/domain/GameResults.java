package io.github.edwinvanrooij.camelraceshared.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by eddy
 * on 6/11/17.
 */
public class GameResults {
    private CardType winningType;
    private List<ResultItem> resultItems;

    public CardType getWinningType() {
        return winningType;
    }

    public void setWinningType(CardType winningType) {
        this.winningType = winningType;
    }

    public GameResults(CardType winningType) {
        this.winningType = winningType;
        resultItems = new ArrayList<>();

    }

    public void addResultItem(ResultItem item) {
        resultItems.add(item);
    }

}
