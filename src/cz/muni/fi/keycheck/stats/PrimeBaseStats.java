package cz.muni.fi.keycheck.stats;

import cz.muni.fi.keycheck.base.CardStats;
import cz.muni.fi.keycheck.base.Params;
import cz.muni.fi.keycheck.helpers.CollectionsHelper;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author David Formanek
 * @author Peter Sekan, peter.sekan@mail.muni.cz
 * @version 24.10.2015
 */
public class PrimeBaseStats extends CardStats {
    private Map<Long, Long> valid = new HashMap<>();
    private Map<Long, Long> unique = new HashMap<>();
    private Map<Long, Long> uniqueP = new HashMap<>();
    private Map<Long, Long> uniqueQ = new HashMap<>();
    private Map<Long, Long> exponent = new HashMap<>();

    private Set<BigInteger> pSet = new HashSet<>();
    private Set<BigInteger> qSet = new HashSet<>();

    public PrimeBaseStats(String icsn) {
        super(icsn);
    }

    public PrimeBaseStats(String icsn, long numOfKey) {
        super(icsn, numOfKey);
    }

    @Override
    protected String[] getDataNames() {
        return new String[]{
                "validity.dat",
                "uniqueness.dat",
                "uniqueness.p.dat",
                "uniqueness.q.dat",
                "private.exponent.bitlength.dat"
        };
    }

    @Override
    protected Map<Long, Long> getData(int dataType) {
        switch (dataType) {
            case 0:
                return valid;
            case 1:
                return unique;
            case 2:
                return uniqueP;
            case 3:
                return uniqueQ;
            case 4:
                return exponent;
            default:
                return new HashMap<>();
        }
    }

    @Override
    public String getLabelForKey(int dataType, Long key) {
        int keyValue = key.intValue();
        switch (dataType) {
            case 0:
                switch (keyValue) {
                    case 0:
                        return "Invalid";
                    default:
                        return "Valid";
                }
            case 1:
            case 2:
            case 3:
                switch (keyValue) {
                    case 0:
                        return "Non-unique";
                    default:
                        return "Unique";
                }
            case 4:
                switch (keyValue) {
                    case -1:
                        return "Invalid";
                    default:
                        return super.getLabelForKey(dataType, key);
                }
            default:
                return super.getLabelForKey(dataType, key);
        }
    }

    @Override
    public void process(Params params) {
        super.process(params);
        boolean uqP = pSet.add(params.getP()),
                uqQ = qSet.add(params.getQ());

        CollectionsHelper.incrementMap(valid, (params.isValid(true) ? 1 : 0));
        CollectionsHelper.incrementMap(unique, ((uqP && uqQ) ? 1 : 0));
        CollectionsHelper.incrementMap(uniqueP, (uqP ? 1 : 0));
        CollectionsHelper.incrementMap(uniqueQ, (uqQ ? 1 : 0));
        try {
            CollectionsHelper.incrementMap(exponent, params.getPrivateExponent().bitLength());
        } catch (ArithmeticException ex) {
            CollectionsHelper.incrementMap(exponent, Long.valueOf(-1));
        }
    }

    @Override
    public void print() {
        CollectionsHelper.insertIfNotContains(valid, 0L, 0L);
        CollectionsHelper.insertIfNotContains(valid, 1L, 0L);
        CollectionsHelper.insertIfNotContains(unique, 0L, 0L);
        CollectionsHelper.insertIfNotContains(unique, 1L, 0L);
        CollectionsHelper.insertIfNotContains(uniqueP, 0L, 0L);
        CollectionsHelper.insertIfNotContains(uniqueP, 1L, 0L);
        CollectionsHelper.insertIfNotContains(uniqueQ, 0L, 0L);
        CollectionsHelper.insertIfNotContains(uniqueQ, 1L, 0L);
    }
}
