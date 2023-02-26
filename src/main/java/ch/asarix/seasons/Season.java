package ch.asarix.seasons;

import ch.asarix.Contribution;

public record Season(String name, long fromMillis, long toMillis) {

    public boolean contains(Contribution contribution) {
        return fromMillis <= contribution.dateMillis() && toMillis >= contribution.dateMillis();
    }
}
