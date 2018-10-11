package agents.piers;

import java.util.ArrayList;

public class Linq {

    public static <T> ArrayList<T> filter(ArrayList<T> source, Func<T, Boolean> predicate) {
        ArrayList<T> result = new ArrayList<T>();
        for (T obj : source) {
            if (predicate.apply(obj) == true) {
                result.add(obj);
            }
        }
        return result;
    }

    public static <T> int count(ArrayList<T> source, Func<T, Boolean> predicate) {
        int seen = 0;
        for (T obj : source) {
            if (predicate.apply(obj) == true) {
                seen += 1;
            }
        }
        return seen;
    }

    public static <T> boolean some(ArrayList<T> source, Func<T, Boolean> predicate) {
        for (T obj : source) {
            if (predicate.apply(obj) == true) {
                return true;
            }
        }
        return false;
    }

    public static float sum(ArrayList<Float> source) {
        float total = (float)0.0;
        for (Float obj : source) {
            total += obj;
        }
        return total;
    }
}