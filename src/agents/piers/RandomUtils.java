package agents.piers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;

public class RandomUtils {

    private static Random _rand = new Random();

    public static <T> T choose(T... options) {
        int i = RandomUtils._rand.nextInt(options.length);
        return options[i];
    }

    @SuppressWarnings("unchecked")
    public static <T> ArrayList<T> sample(ArrayList<T> population, int number) {
        int populationSize = population.size();
        if (populationSize <= number) {
            return (ArrayList<T>)population.clone();
        }
        ArrayList<T> result = new ArrayList<T>();
        HashSet<Integer> selected = new HashSet<Integer>();
        while (result.size() < number) {
            int i = RandomUtils._rand.nextInt(populationSize);
            if (selected.contains(i)) {
                continue;
            }
            result.add(population.get(i));
            selected.add(i);
        }
        return result;
    }

    public static <T> T choose(ArrayList<T> options) {
        int i = RandomUtils._rand.nextInt(options.size());
        return options.get(i);
    }

    public static boolean chance(double probability) {
        return RandomUtils._rand.nextDouble() <= probability;
    }

    public static int integer(int lower, int upper) {
        return RandomUtils._rand.nextInt(upper + 1) + lower;
    }

    public static float weight() {
        return RandomUtils._rand.nextFloat();
    }
}