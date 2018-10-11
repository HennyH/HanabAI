package agents.piers;

public class Maybe<T> {

    private T _value;

    public Maybe(T value) {
        this._value = value;
    }

    public T getValue() {
        if (this._value == null) {
            throw new IllegalAccessError("Attempted to get the value of an empty Maybe<T>");
        }
        return this._value;
    }

    public boolean hasValue() {
        return this._value != null;
    }
}
