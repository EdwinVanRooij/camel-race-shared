package io.github.edwinvanrooij.camelraceshared.domain;

/**
 * Created by eddy
 * on 6/8/17.
 */
public class Bid {
    private CardType type;
    private int value;

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "Bid{" +
                "type=" + type +
                ", value=" + value +
                '}';
    }

    public Bid(CardType type, int value) {
        this.type = type;
        this.value = value;
    }
}

