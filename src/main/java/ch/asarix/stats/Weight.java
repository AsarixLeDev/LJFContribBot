package ch.asarix.stats;

public class Weight {

    private final double base;
    private final double overflow;

    public Weight(double base, double overflow) {
        if (Double.isFinite(base)) {
            this.base = base;
        } else {
            this.base = 0;
        }
        if (Double.isFinite(overflow)) {
            this.overflow = overflow;
        } else {
            this.overflow = 0;
        }
    }

    public double base() {
        return base;
    }

    public double overflow() {
        return overflow;
    }

    public double total() {
        return base + overflow;
    }
}
