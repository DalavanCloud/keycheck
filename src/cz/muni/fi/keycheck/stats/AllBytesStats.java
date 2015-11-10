package cz.muni.fi.keycheck.stats;

import cz.muni.fi.keycheck.base.CardStats;
import cz.muni.fi.keycheck.base.Params;
import cz.muni.fi.keycheck.helpers.CollectionsHelper;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author Peter Sekan, peter.sekan@mail.muni.cz
 * @version 27.10.2015
 */
public class AllBytesStats extends CardStats {
    public static final int BITS_IN_BYTE = 8;
    public static final int BITS_IN_PART = 256;
    private static boolean useAllBytes = true;
    private static int bytesFromStartToAnalyse = 0;
    private static int bytesFromEndToAnalyse = 0;
    private TreeMap<Long, Long> primesBits = new TreeMap<>();
    private TreeMap<Long, Long> modulusBits = new TreeMap<>();
    private TreeMap<Long, Long> pBits = new TreeMap<>();
    private TreeMap<Long, Long> qBits = new TreeMap<>();

    public AllBytesStats(String icsn) {
        super(icsn);
    }

    public AllBytesStats(String icsn, long numOfKey) {
        super(icsn, numOfKey);
    }

    /**
     * Set only subset of bytes to analyse
     * @param fromStart bytes numbers to analyse from start of primes and modulus
     * @param fromEnd bytes numbers to analyse from end of primes and modulus
     */
    public static void setBytesToAnalyse(int fromStart, int fromEnd) {
        useAllBytes = false;
        bytesFromStartToAnalyse = fromStart;
        bytesFromEndToAnalyse = fromEnd;
    }

    @Override
    protected String[] getDataNames() {
        int numOfPrimeData = primesBits.size() / BITS_IN_PART;
        int numOfModulusData = modulusBits.size() / BITS_IN_PART;
        int numOfData = 3 * numOfPrimeData + numOfModulusData;
        int dataPos = 0;
        String dataNames[];
        if (!useAllBytes) {
            dataNames = new String[(bytesFromStartToAnalyse + bytesFromEndToAnalyse) * 4];
        }
        else {
            dataNames = new String[numOfData];
        }

        for (int i = 0; i < numOfPrimeData; i++) {
            if (!useAllBytes && (i >= bytesFromStartToAnalyse && numOfPrimeData > (bytesFromEndToAnalyse + i))) {
                continue;
            }
            dataNames[dataPos++] = "primes_bytes.part_" + (i + 1) + ".dat";
        }
        for (int i = 0; i < numOfPrimeData; i++) {
            if (!useAllBytes && (i >= bytesFromStartToAnalyse && numOfPrimeData > (bytesFromEndToAnalyse + i))) {
                continue;
            }
            dataNames[dataPos++] = "primes_bytes.p.part_" + (i + 1) + ".dat";
        }
        for (int i = 0; i < numOfPrimeData; i++) {
            if (!useAllBytes && (i >= bytesFromStartToAnalyse && numOfPrimeData > (bytesFromEndToAnalyse + i))) {
                continue;
            }
            dataNames[dataPos++] = "primes_bytes.q.part_" + (i + 1) + ".dat";
        }
        for (int i = 0; i < numOfModulusData; i++) {
            if (!useAllBytes && (i >= bytesFromStartToAnalyse && numOfModulusData > (bytesFromEndToAnalyse + i))) {
                continue;
            }
            dataNames[dataPos++] = "modulus_bytes.part_" + (i + 1) + ".dat";
        }

        return dataNames;
    }

    @Override
    protected Map<Long, Long> getData(int dataType) {
        int numOfPrimeData = primesBits.size() / BITS_IN_PART;
        int numOfModulusData = modulusBits.size() / BITS_IN_PART;
        int numOfData = 3 * numOfPrimeData + numOfModulusData;

        if (!useAllBytes) {
            int type = dataType / (bytesFromStartToAnalyse + bytesFromEndToAnalyse);
            int inTypeIndex = dataType % (bytesFromStartToAnalyse + bytesFromEndToAnalyse);

            if (inTypeIndex >= bytesFromStartToAnalyse) {
                dataType = (type * numOfPrimeData + (type == 3 ? numOfModulusData : numOfPrimeData)) - (inTypeIndex - bytesFromStartToAnalyse + 1);
            } else {
                dataType = type * numOfPrimeData + inTypeIndex;
            }
        }

        Map<Long, Long> willBeReturned = new TreeMap<>(Collections.reverseOrder());
        if (dataType < numOfPrimeData) {
            long from = dataType;
            long to = from + 1;
            willBeReturned.putAll(primesBits.subMap(from * BITS_IN_PART, to * BITS_IN_PART));
        } else if (dataType < 2 * numOfPrimeData) {
            long from = dataType - numOfPrimeData;
            long to = from + 1;
            willBeReturned.putAll(pBits.subMap(from * BITS_IN_PART, to * BITS_IN_PART));
        } else if (dataType < 3 * numOfPrimeData) {
            long from = dataType - (2 * numOfPrimeData);
            long to = from + 1;
            willBeReturned.putAll(qBits.subMap(from * BITS_IN_PART, to * BITS_IN_PART));
        } else if (dataType < 3 * numOfPrimeData + numOfData) {
            long from = dataType - (3 * numOfPrimeData);
            long to = from + 1;
            willBeReturned.putAll(modulusBits.subMap(from * BITS_IN_PART, to * BITS_IN_PART));
        }
        return willBeReturned;
    }

    @Override
    public String getLabelForKey(int dataType, Long key) {
        String bits = Long.toString(key % BITS_IN_PART, 2);
        while (bits.length() < 8) {
            bits = "0" + bits;
        }
        return bits;
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
        if (addTo.size() == 0) {
            for (long i = 0; i < (bitsString.length() / BITS_IN_BYTE) * BITS_IN_PART; i++) {
                CollectionsHelper.insertIfNotContains(addTo, i, 0L);
            }
        }

        int allBytes = bitsString.length() / BITS_IN_BYTE;
        for (long b = 0; b < allBytes; b++) {
            if (!useAllBytes &&
                (b >= bytesFromStartToAnalyse && allBytes > (bytesFromEndToAnalyse + b))) {
                continue;
            }
            int val = Integer.parseInt(bitsString.substring((int)(b * BITS_IN_BYTE), (int)((b + 1) * BITS_IN_BYTE)), 2);
            Long keyInMap = b * BITS_IN_PART + val % BITS_IN_PART;
            CollectionsHelper.incrementMap(addTo, keyInMap);
        }
    }
}
