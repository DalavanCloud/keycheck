package cz.muni.fi.keycheck.exports;

import cz.muni.fi.keycheck.base.ExportContainer;
import cz.muni.fi.keycheck.base.Params;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Peter Sekan, peter.sekan@mail.muni.cz
 * @version 02.11.2015
 */
public class DifferenceExport extends ExportContainer {
    private Map<Integer, BigDecimal> maxValueOfPrime = new HashMap<>();

    @Override
    protected String[] getFilesName() {
        return new String[]{
                "p_minus_q.export.dat",
                "prime_difference_bitlength.export.dat"
        };
    }

    private BigDecimal getMaxValueOfPrime(Params params) {
        int bitLength = Integer.max(params.getP().bitLength(), params.getQ().bitLength());
        if (maxValueOfPrime.containsKey(bitLength))
            return maxValueOfPrime.get(bitLength);

        BigDecimal maxValue = BigDecimal.valueOf(2).pow(bitLength);
        maxValueOfPrime.put(bitLength, maxValue);
        return maxValue;
    }

    @Override
    protected String[] getTransformed(Params params) {
        BigInteger difference = params.getPrimeDifference();
        BigDecimal maxKeyValue = getMaxValueOfPrime(params);
        BigDecimal pMinusQPercentage = (new BigDecimal(difference)).multiply(BigDecimal.valueOf(100)).divide(maxKeyValue, BigDecimal.ROUND_CEILING, 2);

        return new String[]{
                pMinusQPercentage.toString(),
                Integer.toString(difference.bitLength())
        };
    }
}
