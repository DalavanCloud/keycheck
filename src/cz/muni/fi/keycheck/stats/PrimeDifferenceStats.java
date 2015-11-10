package cz.muni.fi.keycheck.stats;

import cz.muni.fi.keycheck.base.CardStats;
import cz.muni.fi.keycheck.base.Params;
import cz.muni.fi.keycheck.helpers.CollectionsHelper;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Peter Sekan, peter.sekan@mail.muni.cz
 * @version 21.10.2015
 */
public class PrimeDifferenceStats extends CardStats {
    private Map<Long, Long> pMinusQ = new HashMap<>();
    private Map<Long, Long> pGreaterThanQ = new HashMap<>();
    private Map<Long, Long> primeDifferenceBitLength = new HashMap<>();
    private Map<Integer, BigInteger> maxValueOfPrime = new HashMap<>();

    public PrimeDifferenceStats(String icsn) {
        super(icsn);
    }

    public PrimeDifferenceStats(String icsn, long numOfKey) {
        super(icsn, numOfKey);
    }

    @Override
    protected String[] getDataNames() {
        return new String[]{
            "p_minus_q.dat",
            "p_greater_then_q.dat",
            "prime_difference_bitlength.dat"
        };
    }

    @Override
    protected Map<Long, Long> getData(int dataType) {
        switch (dataType) {
            case 0: return pMinusQ;
            case 1: return pGreaterThanQ;
            case 2: return primeDifferenceBitLength;
            default:
                return new HashMap<>();
        }
    }

    @Override
    public String getLabelForKey(int dataType, Long key) {
        if (dataType == 1) {
            switch (key.intValue()) {
                case -1:
                    return "p < q";
                case 0:
                    return "p == q";
                case 1:
                    return "p > q";
                default:
                    return super.getLabelForKey(dataType, key);
            }
        }
        return super.getLabelForKey(dataType, key);
    }

    private BigInteger getMaxValueOfPrime(Params params) {
        int bitLength = Integer.max(params.getP().bitLength(), params.getQ().bitLength());
        if (maxValueOfPrime.containsKey(bitLength))
            return maxValueOfPrime.get(bitLength);

        BigInteger maxValue = BigInteger.valueOf(2).pow(bitLength);
        maxValueOfPrime.put(bitLength, maxValue);
        return maxValue;
    }

    @Override
    public void process(Params params) {
        if (!params.isValidKey()) {
            return;
        }
        super.process(params);
        BigInteger difference = params.getPrimeDifference();
        BigInteger maxKeyValue = getMaxValueOfPrime(params);
        Long pMinusQPercentage = difference.multiply(BigInteger.valueOf(100)).divide(maxKeyValue).longValue();

        CollectionsHelper.incrementMap(pMinusQ, pMinusQPercentage);
        CollectionsHelper.incrementMap(primeDifferenceBitLength, difference.bitLength());
        CollectionsHelper.incrementMap(pGreaterThanQ, params.getP().subtract(params.getQ()).signum());
    }

    @Override
    public void print() {
        CollectionsHelper.insertIfNotContains(pGreaterThanQ, -1L, 0L);
        CollectionsHelper.insertIfNotContains(pGreaterThanQ, 0L, 0L);
        CollectionsHelper.insertIfNotContains(pGreaterThanQ, 1L, 0L);
        CollectionsHelper.insertIfNotContains(primeDifferenceBitLength, 0L, 0L);
    }
}
