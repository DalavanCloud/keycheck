package cz.muni.fi.keycheck.base;

import cz.muni.fi.keycheck.helpers.CollectionsHelper;

import java.util.Map;

/**
 * @author Peter Sekan, peter.sekan@mail.muni.cz
 * @version 21.10.2015
 */
public abstract class CardStats {
    private String icsn;
    private long processedKeys;
    protected long numOfKey;

    public CardStats(String icsn) {
        this.icsn = icsn;
        this.processedKeys = 0;
    }

    public CardStats(String icsn, long numOfKey) {
        this.icsn = icsn;
        this.processedKeys = 0;
        this.numOfKey = numOfKey;
    }

    public long getProcessedKeys() {
        return processedKeys;
    }

    public String getIcsn() {
        return icsn;
    }

    /**
     * Get number of data processed from card.
     *
     * @return number of data
     */
    protected int getNumOfData() {
        return getDataNames().length;
    }

    /**
     * Get names for data.
     *
     * @return names of data
     */
    protected abstract String[] getDataNames();

    /**
     * Get map for data i
     *
     * @param dataType index of data, 0 <= i < getNumOfData()
     * @return map with data
     */
    protected abstract Map<Long, Long> getData(int dataType);

    /**
     * Get map of percentage usage of data i
     *
     * @param dataType index of data, 0 <= i < getNumOfData()
     * @param binning null|not negative number
     * @param minKey minimum for generating map
     * @param maxKey maximum for generating map
     * @return map with percentage usage of data
     */
    public Map<Long, Double> getPercentageData(int dataType, Long binning, Long minKey, Long maxKey) {
        Map<Long, Long> data = getData(dataType);
        long sumAll = getSumAllForType(dataType);
        if (binning != null && binning > 0) {
            return CollectionsHelper.computePercentageWithBinning(data, binning, sumAll, minKey, maxKey);
        }
        else if ((maxKey -minKey) > 200)
            return CollectionsHelper.computePercentageWithBinning(data, (maxKey - minKey) / 200, sumAll, minKey, maxKey);
        else
            return CollectionsHelper.computePercentage(data, sumAll, minKey, maxKey);
    }

    /**
     * Get number of element in data for compute percentage
     *
     * @param dataType index of data, 0 <= i < getNumOfData()
     * @return sum of all element in this data map
     */
    protected long getSumAllForType(int dataType) {
        return CollectionsHelper.sumOfCollection(getData(dataType).values());
    }

    /**
     * Get label name for key
     *
     * @param dataType index of data, 0 <= i < getNumOfData()
     * @param key      key from data map
     * @return label used as row name in stats
     */
    public String getLabelForKey(int dataType, Long key) {
        return key.toString();
    }

    /**
     * Process key on this card.
     *
     * @param params Key to process
     */
    public void process(Params params) {
        processedKeys++;
    }

    /**
     * Print some actual statistics for this card.
     */
    public void print() {

    }
}
