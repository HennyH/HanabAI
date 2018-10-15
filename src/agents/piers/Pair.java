package agents.piers;

public class Pair<A, B> {

    private A _a;
    private B _b;

    public Pair(A a, B b) {
        this._a = a;
        this._b = b;
    }

    public A getLeft() {
        return this._a;
    }

    public B getRight() {
        return this._b;
    }
}