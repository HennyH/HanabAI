package agents.piers;

public interface Func<T1, R> {
    R apply(T1 param);

    public static <T1> Func<T1, Boolean> invert(Func<T1, Boolean> func) {
        return new Func<T1, Boolean>() {
            @Override
            public Boolean apply(T1 arg) {
                return !func.apply(arg);
            }
        };
    }

    public static <T1> Func<T1, T1> identity() {
        return new Func<T1, T1>() {
            @Override
            public T1 apply(T1 arg) {
                return arg;
            }
        };
    }
}