package cz.muni.fi.keycheck.base;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.DecimalFormat;
import java.util.*;

/**
 * @author Peter Sekan, peter.sekan@mail.muni.cz
 * @version 21.10.2015
 */
public class CardStatsPercentageContainer<T extends CardStats> implements Stats {
    protected Class<T> typeParameterClass = null;
    protected ArrayList<T> cardsStatistics = new ArrayList<>();
    protected T actualCardStatistics = null;
    protected T summaryStatistics;
    protected Long binning = null;

    /**
     * Constructor without binning
     * @param typeParameterClass Class extends CardStats
     */
    public CardStatsPercentageContainer(Class<T> typeParameterClass) {
        this(typeParameterClass, null);
    }

    /**
     * Constructor without binning
     * @param typeParameterClass Class extends CardStats
     * @param binning (> 0) = binning | (null or <= 0) = no binning
     */
    public CardStatsPercentageContainer(Class<T> typeParameterClass, Long binning) {
        this.typeParameterClass = typeParameterClass;
        this.binning = binning;
        summaryStatistics = createCardStats("Summary", 0);
    }

    /**
     * Create new instance of class T which extends CardStats
     * @param icsn ICSN of new card
     * @param numOfKeys Number of keys generated on this card
     * @return new instance of T
     * @throws IllegalArgumentException if creating new instance failure
     */
    private T createCardStats(String icsn, long numOfKeys) {
        try {
            if (numOfKeys <= 0)
                return typeParameterClass.getDeclaredConstructor(String.class).newInstance(icsn);
            else
                return typeParameterClass.getDeclaredConstructor(String.class, long.class).newInstance(icsn, numOfKeys);
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            throw new IllegalArgumentException("Cannot create object of class " + typeParameterClass);
        }
    }

    /**
     * Create and add new card to statistics
     * @param icsn ICSN of new card
     * @throws IllegalArgumentException if creating new card instance failure
     */
    @Override
    public void changeCard(String icsn) {
        actualCardStatistics = createCardStats(icsn, 0);
        cardsStatistics.add(actualCardStatistics);
    }

    /**
     * Create and add new card to statistics
     * @param icsn ICSN of new card
     * @param numOfKeys Number of keys generated on this card
     * @throws IllegalArgumentException if creating new card instance failure
     */
    @Override
    public void changeCard(String icsn, long numOfKeys) {
        actualCardStatistics = createCardStats(icsn, numOfKeys);
        cardsStatistics.add(actualCardStatistics);
    }

    /**
     * Process key on last added card.
     * If no card exists in container create new card with not defined ICSN).
     * @param params key
     */
    @Override
    public void process(Params params) {
        if (actualCardStatistics == null)
            changeCard("NotDefinedICSN");
        actualCardStatistics.process(params);
        summaryStatistics.process(params);
    }

    /**
     * Print percentage statistics
     */
    @Override
    public void print() {
        if (summaryStatistics.getProcessedKeys() == 0)
            return;

        DecimalFormat formatter = new DecimalFormat("#0.0000");
        int columns = 0;
        ArrayList<String> names = new ArrayList<>();
        for (CardStats cardsStatistic : cardsStatistics) {
            if (cardsStatistic.getProcessedKeys() > 0) {
                names.add(cardsStatistic.getIcsn());
                cardsStatistic.print();
                columns++;
            }
        }
        if (columns > 1) {
            if (summaryStatistics.getProcessedKeys() > 0) {
                names.add(summaryStatistics.getIcsn());
                columns++;
            }
        }

        ArrayList<ArrayList<Map<Long, Double>>> data = new ArrayList<>();
        for (int type = 0; type < summaryStatistics.getNumOfData(); type++) {
            ArrayList<Map<Long, Double>> data_by_type = new ArrayList<>();

            Long minKey = null, maxKey = null;
            for (CardStats cardsStatistic : cardsStatistics) {
                if (cardsStatistic.getProcessedKeys() > 0) {
                    Map<Long, Long> map = cardsStatistic.getData(type);
                    try {
                        Long cardMin = Collections.min(map.keySet());
                        Long cardMax = Collections.max(map.keySet());
                        if (minKey == null || cardMin.compareTo(minKey) == -1)
                            minKey = cardMin;
                        if (maxKey == null || cardMax.compareTo(maxKey) == 1)
                            maxKey = cardMax;
                    } catch (NoSuchElementException ex) {
                        System.err.println("Statistics under class " + typeParameterClass.toString() + ", type " + type + "is empty.");
                    }
                }
            }
            if (minKey == null || maxKey == null)
                continue;

            for (CardStats cardsStatistic : cardsStatistics) {
                if (cardsStatistic.getProcessedKeys() > 0) {
                    data_by_type.add(cardsStatistic.getPercentageData(type, binning, minKey, maxKey));
                }
            }
            if (columns > 1) {
                data_by_type.add(summaryStatistics.getPercentageData(type, binning, minKey, maxKey));
            }
            data.add(data_by_type);
        }

        String fileOutputs[] = summaryStatistics.getDataNames();
        for (int type = 0; type < summaryStatistics.getNumOfData(); type++) {
            if (data.get(type).size() == 0) {
                continue;
            }

            String fileName = fileOutputs[type];
            if (binning != null && binning > 0) {
                fileName = fileName.replace(".dat", "." + binning + "-binning.dat");
            }
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
                for (String name : names)
                    writer.write("\t" + name);
                writer.newLine();

                Set<Long> keys = new TreeSet<>();
                for (Map<Long, Double> map : data.get(type)) {
                    keys.addAll(map.keySet());
                }

                for (Long key : keys) {
                    writer.write(summaryStatistics.getLabelForKey(type, key));
                    for (int column = 0; column < columns; column++) {
                        Map<Long, Double> map = data.get(type).get(column);
                        Double value = map.getOrDefault(key, 0.0);
                        writer.write("\t" + formatter.format(value));
                    }
                    writer.newLine();
                }

                writer.flush();
            } catch (IOException exception) {
                System.err.println("Cannot generate statistics to file " + fileName + ".");
            }
        }
    }
}
