package agents.piers;

import java.util.ArrayList;

public class MathUtils {

    public static float stdevP(ArrayList<Float> samples) {
        float sum = Linq.sum(samples);
        float mean = sum / samples.size();
        float stdev = (float)0.0;
        for (float sample: samples) {
            stdev += Math.pow(sample - mean, 2);
        }

        return (float)Math.sqrt(stdev / (samples.size() -1));
    }
}