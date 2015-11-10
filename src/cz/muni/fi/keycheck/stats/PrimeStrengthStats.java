package cz.muni.fi.keycheck.stats;

import cz.muni.fi.keycheck.base.CardStats;
import cz.muni.fi.keycheck.base.Params;
import cz.muni.fi.keycheck.helpers.CollectionsHelper;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Peter Sekan, peter.sekan@mail.muni.cz
 * @author David Formanek
 * @version 27.10.2015
 */
public class PrimeStrengthStats extends CardStats {
    public static final int smoothBound = 100000;
    private static final int primeCertainity = 40;
    private static List<BigInteger> primesUnderBound = null;

    private Map<Long, Long> smallFactorCounts = new HashMap<>();
    private Map<Long, Long> smoothPartLengths = new HashMap<>();
    private Map<Long, Long> smallFactorPCounts = new HashMap<>();
    private Map<Long, Long> smoothPartPLengths = new HashMap<>();
    private Map<Long, Long> smallFactorQCounts = new HashMap<>();
    private Map<Long, Long> smoothPartQLengths = new HashMap<>();
    private Map<Long, Long> smoothAndFactoredNumberCountMap = new HashMap<>();
    private long smoothNumberCount = 0;
    private long factoredNumberCount = 0;

    public PrimeStrengthStats(String icsn) {
        this(icsn, 0);
    }

    public PrimeStrengthStats(String icsn, long numOfKey) {
        super(icsn, numOfKey);

        if (primesUnderBound == null)
            primesUnderBound = getPrimesUnder(smoothBound);
    }

    private static List<BigInteger> getPrimesUnder(int bound) {
        // Sieve of Eratosthenes
        final int sqrt = (int) Math.sqrt(bound) + 1;
        boolean[] isComposite = new boolean[bound];
        for (int i = 2; i < sqrt; i++) {
            if (!isComposite[i]) {
                for (int j = i * i; j < bound; j += i) {
                    isComposite[j] = true;
                }
            }
        }
        List<BigInteger> smallPrimes = new ArrayList<>();
        for (int i = 2; i < bound; i++) {
            if (!isComposite[i]) {
                smallPrimes.add(BigInteger.valueOf(i));
            }
        }
        return smallPrimes;
    }

    @Override
    protected String[] getDataNames() {
        return new String[]{
                "small_factor_count.dat",
                "small_factor_count.p.dat",
                "small_factor_count.q.dat",
                "smooth_part_lengths.dat",
                "smooth_part_lengths.p.dat",
                "smooth_part_lengths.q.dat",
                "smooth_and_factored_number_count.dat"
        };
    }

    @Override
    protected Map<Long, Long> getData(int dataType) {
        switch (dataType) {
            case 0:
                return smallFactorCounts;
            case 1:
                return smallFactorPCounts;
            case 2:
                return smallFactorQCounts;
            case 3:
                return smoothPartLengths;
            case 4:
                return smoothPartPLengths;
            case 5:
                return smoothPartQLengths;
            case 6:
                return smoothAndFactoredNumberCountMap;
            default:
                return new HashMap<>();
        }
    }

    @Override
    public String getLabelForKey(int dataType, Long key) {
        if (dataType == 6) {
            switch (key.intValue()) {
                case 0:
                    return smoothBound + "-smooth";
                case 1:
                    return "only one bigger factor";
            }
        }
        return super.getLabelForKey(dataType, key);
    }

    private void checkSmoothness(BigInteger n, Map<Long, Long> smallFactorMap, Map<Long, Long> smoothPartMap) {
        long nSmallFactors = 0;
        BigInteger smoothPart = BigInteger.ONE;
        for (BigInteger prime : primesUnderBound) {
            for (; ; ) {
                if (n.mod(prime).equals(BigInteger.ZERO)) {
                    n = n.divide(prime);
                    nSmallFactors++;
                    smoothPart = smoothPart.multiply(prime);
                } else {
                    break;
                }
            }
        }
        CollectionsHelper.incrementMap(smallFactorCounts, nSmallFactors);
        CollectionsHelper.incrementMap(smoothPartLengths, smoothPart.bitLength());
        CollectionsHelper.incrementMap(smallFactorMap, nSmallFactors);
        CollectionsHelper.incrementMap(smoothPartMap, smoothPart.bitLength());

        if (n.equals(BigInteger.ONE)) {
            smoothNumberCount++;
        }
        if (n.isProbablePrime(primeCertainity)) {
            factoredNumberCount++;
        }
    }

    @Override
    public void process(Params params) {
        if (!params.isValidKey()) {
            return;
        }
        super.process(params);

        checkSmoothness(params.getP().subtract(BigInteger.ONE), smallFactorPCounts, smoothPartPLengths);
        checkSmoothness(params.getQ().subtract(BigInteger.ONE), smallFactorQCounts, smoothPartQLengths);
    }

    @Override
    public void print() {
        CollectionsHelper.insertIfNotContains(smoothAndFactoredNumberCountMap, 0L, smoothNumberCount);
        CollectionsHelper.insertIfNotContains(smoothAndFactoredNumberCountMap, 1L, factoredNumberCount);

        CollectionsHelper.insertIfNotContains(smallFactorCounts, 0L, 0L);
        CollectionsHelper.insertIfNotContains(smallFactorPCounts, 0L, 0L);
        CollectionsHelper.insertIfNotContains(smallFactorQCounts, 0L, 0L);
        CollectionsHelper.insertIfNotContains(smoothPartLengths, 0L, 0L);
        CollectionsHelper.insertIfNotContains(smoothPartPLengths, 0L, 0L);
        CollectionsHelper.insertIfNotContains(smoothPartQLengths, 0L, 0L);
    }
}
