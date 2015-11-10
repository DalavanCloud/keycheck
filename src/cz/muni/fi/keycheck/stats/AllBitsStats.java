package cz.muni.fi.keycheck.stats;

import cz.muni.fi.keycheck.base.CardStats;
import cz.muni.fi.keycheck.base.Params;
import cz.muni.fi.keycheck.helpers.CollectionsHelper;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author Peter Sekan, peter.sekan@mail.muni.cz
 * @version 27.10.2015
 */
public class AllBitsStats extends CardStats {
    public static final int BITS_IN_PART = 128;
    private TreeMap<Long, Long> primesBits = new TreeMap<>();
    private TreeMap<Long, Long> modulusBits = new TreeMap<>();
    private TreeMap<Long, Long> pBits = new TreeMap<>();
    private TreeMap<Long, Long> qBits = new TreeMap<>();

    public AllBitsStats(String icsn) {
        super(icsn);
    }

    public AllBitsStats(String icsn, long numOfKey) {
        super(icsn, numOfKey);
    }

    @Override
    protected String[] getDataNames() {
        int numOfPrimeData = primesBits.size() / BITS_IN_PART;
        int numOfModulusData = modulusBits.size() / BITS_IN_PART;
        int numOfData = 3 * numOfPrimeData + numOfModulusData;
        String dataNames[] = new String[numOfData];
        int dataPos = 0;

        for (int i = 0; i < numOfPrimeData; i++) {
            dataNames[dataPos++] = "primes_bits.part_" + (i + 1) + ".dat";
        }
        for (int i = 0; i < numOfPrimeData; i++) {
            dataNames[dataPos++] = "primes_bits.p.part_" + (i + 1) + ".dat";
        }
        for (int i = 0; i < numOfPrimeData; i++) {
            dataNames[dataPos++] = "primes_bits.q.part_" + (i + 1) + ".dat";
        }
        for (int i = 0; i < numOfModulusData; i++) {
            dataNames[dataPos++] = "modulus_bits.part_" + (i + 1) + ".dat";
        }
        return dataNames;
    }

    @Override
    protected Map<Long, Long> getData(int dataType) {
        int numOfPrimeData = primesBits.size() / BITS_IN_PART;
        int numOfModulusData = modulusBits.size() / BITS_IN_PART;
        int numOfData = 3 * numOfPrimeData + numOfModulusData;
        if (dataType < numOfPrimeData) {
            long from = dataType;
            long to = from + 1;
            return primesBits.subMap(from * BITS_IN_PART, to * BITS_IN_PART);
        } else if (dataType < 2 * numOfPrimeData) {
            long from = dataType - numOfPrimeData;
            long to = from + 1;
            return pBits.subMap(from * BITS_IN_PART, to * BITS_IN_PART);
        } else if (dataType < 3 * numOfPrimeData) {
            long from = dataType - (2 * numOfPrimeData);
            long to = from + 1;
            return qBits.subMap(from * BITS_IN_PART, to * BITS_IN_PART);
        } else if (dataType < 3 * numOfPrimeData + numOfModulusData) {
            long from = dataType - (3 * numOfPrimeData);
            long to = from + 1;
            return modulusBits.subMap(from * BITS_IN_PART, to * BITS_IN_PART);
        } else
            return new HashMap<>();
    }

    @Override
    protected long getSumAllForType(int dataType) {
        int numOfPrimeData = primesBits.size() / BITS_IN_PART;
        if (dataType < numOfPrimeData) {
            return 2 * getProcessedKeys();
        }
        return getProcessedKeys();
    }

    @Override
    public String getLabelForKey(int dataType, Long key) {
        return super.getLabelForKey(dataType, key);
    }

    @Override
    public void process(Params params) {
        if (!params.isValidKey()) {
            return;
        }
        super.process(params);

        String pBitsString = params.getP().toString(2),
                qBitsString = params.getQ().toString(2);

        parseBits(pBitsString, primesBits);
        parseBits(qBitsString, primesBits);
        parseBits(pBitsString, pBits);
        parseBits(qBitsString, qBits);
        parseBits(params.getModulus().toString(2), modulusBits);
    }

    private void parseBits(String bitsString, Map<Long, Long> addTo) {
        boolean createParts = false;
        if (addTo.size() == 0) createParts = true;

        for (long bit = 0; bit < bitsString.length(); bit++) {
            int val = Integer.parseInt(bitsString.substring((int)bit, (int)(bit + 1)));
            if (createParts) {
                CollectionsHelper.insertIfNotContains(addTo, bit, 0L);
            }
            if (val == 1)
                CollectionsHelper.incrementMap(addTo, bit);
        }
    }
}
