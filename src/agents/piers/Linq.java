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

    public static <T> Maybe<T> first(ArrayList<T> source) {
        return source.size() > 0 ? new Maybe<T>(source.get(0)) : new Maybe<T>(null);
    }

    public static <T> Maybe<T> single(ArrayList<T> source) {
        return source.size() == 1 ? new Maybe<T>(source.get(0)) : new Maybe<T>(null);
    }

    public static <T> boolean any(ArrayList<T> source, Func<T, Boolean> predicate) {
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

    public static <T> ArrayList<T> removeInstanceWise(ArrayList<T> source, ArrayList<T> other) {
        ArrayList<T> result = new ArrayList<T>();
        @SuppressWarnings("unchecked")
        ArrayList<T> otherCopy = (ArrayList<T>)other.clone();
        for (T obj : source) {
            if (otherCopy.contains(obj)) {
                /* Remember this only removed the first occurance. */
                otherCopy.remove(obj);
            } else {
                result.add(obj);
            }
        }
        return result;
    }

    public static <T> ArrayList<T> notElementsOf(ArrayList<T> source, ArrayList<T> exclusions) {
        ArrayList<T> result = new ArrayList<T>();
        for (T obj : source) {
            if (!exclusions.contains(obj)) {
                result.add(obj);
            }
        }
        return result;
    }

    public static <T> ArrayList<T> elementsOf(ArrayList<T> source, ArrayList<T> other) {
        ArrayList<T> result = new ArrayList<T>();
        for (T obj : source) {
            if (other.contains(obj)) {
                result.add(obj);
            }
        }
        return result;
    }

    public static <T> ArrayList<T> extend(ArrayList<T> source, ArrayList<T> extras) {
        ArrayList<T> result = new ArrayList<T>();
        for (T obj : source) {
            result.add(obj);
        }
        for (T obj : extras) {
            result.add(obj);
        }
        return result;
    }


    public static <T> ArrayList<T> distinct(ArrayList<T> source) {
        ArrayList<T> result = new ArrayList<T>();
        for (T obj : source) {
            if (!result.contains(obj)) {
                result.add(obj);
            }
        }
        return result;
    }

}