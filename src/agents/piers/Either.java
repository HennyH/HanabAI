package agents.piers;

public class Either<A, B> {

    private Maybe<A> _a;
    private Maybe<B> _b;

    public Either(A a, B b) {
        this._a = new Maybe<A>(a);
        this._b = new Maybe<B>(b);
    }

    public Either(Maybe<A> a, Maybe<B> b) {
        this._a = a;
        this._b = b;
    }

    public Maybe<A> getLeft() {
        return this._a;
    }

    public Maybe<B> getRight() {
        return this._b;
    }
}