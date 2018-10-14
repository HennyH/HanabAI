package agents.piers;

public class Identity<T> implements Func<T, T> {
    @Override
    public T apply(T arg) {
        return arg;
    }
}
