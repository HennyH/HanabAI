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

    public static <T1, T2, R> ArrayList<R> zipShortestMap(ArrayList<T1> source, ArrayList<T2> other, Func<Pair<T1, T2>, R> selector) {
        ArrayList<R> result = new ArrayList<R>();
        int smallestLen = Math.min(source.size(), other.size());
        for (int i = 0; i < smallestLen; i++) {
            result.add(selector.apply(new Pair<T1, T2>(source.get(i), other.get(i))));
        }
        return result;
    }

    public static <T, R> ArrayList<R> map(ArrayList<T> source, Func<T, R> selector) {
        ArrayList<R> result = new ArrayList<R>();
        for (T obj : source) {
            result.add(selector.apply(obj));
        }
        return result;
    }

    public static <T, R> ArrayList<R> mapi(ArrayList<T> source, Func<Pair<T, Integer>, R> selector) {
        ArrayList<R> result = new ArrayList<R>();
        for (int i = 0; i < source.size(); i++) {
            result.add(selector.apply(new Pair<>(source.get(i), i)));
        }
        return result;
    }

    public static <T> ArrayList<T> repeat(T value, int times) {
        ArrayList<T> result = new ArrayList<T>();
        for (int i = 1; i <= times; i++) {
            result.add(value);
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

    public static <T, R extends Comparable<R>> Maybe<T> max(ArrayList<T> source, Func<T, R> selector) {
        T maxObj = null;
        R maxObjProjection = null;
        for (T obj : source) {
            R projection = selector.apply(obj);
            if (maxObj == null) {
                maxObj = obj;
                maxObjProjection = projection;
            } else if (projection.compareTo(maxObjProjection) >= 0) {
                maxObj = obj;
                maxObjProjection = projection;
            }
        }

        return new Maybe<T>(maxObj);
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

    public static <T> Maybe<Float> avg(ArrayList<T> source, Func<T, Integer> selector) {
        Integer total = 0;
        for (T obj : source) {
            total += selector.apply(obj);
        }
        return source.size() > 0
            ? new Maybe<Float>((float)total / (float)source.size())
            : new Maybe<Float>(null);
    }

    public static <T> Maybe<Float> avgF(ArrayList<T> source, Func<T, Float> selector) {
        Float total = (float)0.0;
        for (T obj : source) {
            total += selector.apply(obj);
        }
        return source.size() > 0
            ? new Maybe<Float>((float)total / (float)source.size())
            : new Maybe<Float>(null);
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