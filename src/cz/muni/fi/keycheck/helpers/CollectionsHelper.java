package cz.muni.fi.keycheck.helpers;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author Peter Sekan, peter.sekan@mail.muni.cz
 * @version 07.11.2015
 */
public class CollectionsHelper {
    /**
     * Transform count to percentage.
     *
     * @param map Map of data, value is count of some value
     * @return Map with percentage usage some value (key)
     */
    public static Map<Long, Double> computePercentage(Map<Long, Long> map, long sumAll, Long minKey, Long maxKey) {
        return computePercentage(map, sumAll, minKey, maxKey, true);
    }

    /**
     * Transform count to percentage.
     *
     * @param map Map of data, value is count of some value
     * @param sumAll sum all values over all maps
     * @param minKey minKey used in other maps
     * @param maxKey maxKey used in other maps
     * @param fillOthers add 0 to undefined keys in interval (minKey, maxKey)
     * @return Map with percentage usage some value (key)
     */
    public static Map<Long, Double> computePercentage(Map<Long, Long> map, long sumAll, Long minKey, Long maxKey, boolean fillOthers) {
        Map<Long, Double> percentage = new TreeMap<>();
        for (Map.Entry<Long, Long> pair : map.entrySet()) {
            Long key = pair.getKey();
            Long count = pair.getValue();
            if (sumAll <= 0)
                percentage.put(key, 0.0);
            else
                percentage.put(key, (100.0 * count) / (double) sumAll);
        }

        if (fillOthers) {
            for (Long i = minKey; i < maxKey; i++) {
                CollectionsHelper.insertIfNotContains(percentage, i, 0);
            }
        }
        return percentage;
    }

    /**
     * Transform count to percentage.
     *
     * @param map Map of data, value is count of some value
     * @param binning Size of one part
     * @return Map with percentage usage some value (key)
     */
    public static Map<Long, Double> computePercentageWithBinning(Map<Long, Long> map, long binning, long sumAll) {
        Long min = Collections.min(map.keySet());
        Long max = Collections.max(map.keySet());

        return computePercentageWithBinning(map, binning, sumAll, min, max);
    }

    /**
     * Transform count to percentage with binning
     * @param map    Map of data, value is count of some value
     * @param binning Size of one part
     * @param minKey Minimal key to show
     * @param maxKey Maximal key to show   @return Map with percentage usage some value (key)
     */
    public static Map<Long, Double> computePercentageWithBinning(Map<Long, Long> map, long binning, long sumAll, Long minKey, Long maxKey) {
        Map<Long, Long> sumOfParts = new TreeMap<>();
        for (Map.Entry<Long, Long> pair : map.entrySet()) {
            Long key = pair.getKey();
            Long partIndex = (key - minKey) / binning;
            Long index = minKey + (partIndex * binning);
            long count = sumOfParts.getOrDefault(index, 0L);
            count += pair.getValue();
            sumOfParts.put(index, count);
        }

        for (Long i = minKey; i < maxKey; i = i + binning) {
            CollectionsHelper.insertIfNotContains(sumOfParts, i, 0);
        }
        return computePercentage(sumOfParts, sumAll, minKey, maxKey, false);
    }

    /**
     * Helper function for increment number of processed some value.
     *
     * @param map   Map
     * @param value Value for increment
     * @return incremented number of processed value
     */
    public static long incrementMap(Map<Long, Long> map, long value) {
        return incrementMap(map, Long.valueOf(value));
    }

    /**
     * Helper function for increment number of processed some value.
     *
     * @param map   Map
     * @param value Value for increment
     * @return incremented number of processed value
     */
    public static long incrementMap(Map<Long, Long> map, Long value) {
        long count = map.getOrDefault(value, 0L);
        count++;
        map.put(value, count);
        return count;
    }

    /**
     * Insert key to map with default value if map not contains this key
     *
     * @param map          map for insert
     * @param defaultValue long values set as default, if map not contains key
     */
    public static void insertIfNotContains(Map<Long, Long> map, Long key, long defaultValue) {
        if (!map.containsKey(key))
            map.put(key, defaultValue);
    }

    /**
     * Insert key to map with default value if map not contains this key
     *
     * @param map          map for insert
     * @param defaultValue double values set as default, if map not contains key
     */
    public static void insertIfNotContains(Map<Long, Double> map, Long key, double defaultValue) {
        if (!map.containsKey(key))
            map.put(key, defaultValue);
    }

    /**
     * Sum all values in collection of numbers.
     *
     * @param collection collection of numbers
     * @return Sum of numbers
     */
    public static long sumOfCollection(Collection<Long> collection) {
        long sum = 0;
        for (Long value : collection) {
            sum += value;
        }
        return sum;
    }

    /**
     * Sum all values in collection of numbers.
     *
     * @param collection collection of numbers
     * @return Sum of numbers
     */
    public static double sumOfCollectionDouble(Collection<Double> collection) {
        double sum = 0;
        for (Double value : collection) {
            sum += value;
        }
        return sum;
    }
}
