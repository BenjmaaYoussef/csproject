package util;

/**
 * Generic immutable pair utility.
 * Used for returning two related values together (e.g., exercise name + best weight).
 *
 * @param <A> type of the first element
 * @param <B> type of the second element
 */
public class Pair<A, B> {

    private final A first;
    private final B second;

    public Pair(A first, B second) {
        this.first = first;
        this.second = second;
    }

    public A getFirst() { return first; }
    public B getSecond() { return second; }

    @Override
    public String toString() {
        return "(" + first + ", " + second + ")";
    }
}
