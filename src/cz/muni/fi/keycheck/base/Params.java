package cz.muni.fi.keycheck.base;

import java.io.BufferedWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * @author David Formanek
 * @author Peter Sekan, peter.sekan@mail.muni.cz
 */
public class Params {

    private static final int RADIX = 10;
    private static final int PRIME_CERTAINITY = 40;

    private BigInteger exponent;
    private BigInteger modulus;
    private BigInteger p;
    private BigInteger q;
    private long time;
    private boolean checkedValidity = false;
    private boolean validKey = true;

    public BigInteger getExponent() {
        return exponent;
    }

    public void setExponent(BigInteger exponent) {
        this.exponent = exponent;
    }

    public BigInteger getModulus() {
        return modulus;
    }

    public void setModulus(BigInteger modulus) {
        this.modulus = modulus;
    }

    public BigInteger getP() {
        return p;
    }

    public void setP(BigInteger p) {
        this.p = p;
    }

    public BigInteger getQ() {
        return q;
    }

    public void setQ(BigInteger q) {
        this.q = q;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public boolean isValidKey() {
        return isValid(false);
    }

    public boolean isValid(boolean writeInfo) {
        if (checkedValidity)
            return validKey;

        boolean isValid = true;
        if (!p.isProbablePrime(PRIME_CERTAINITY)) {
            isValid = false;
            if (writeInfo) {
                System.out.println("p " + p.toString(RADIX) + " is not a prime");
            }
        }
        if (!q.isProbablePrime(PRIME_CERTAINITY)) {
            isValid = false;
            if (writeInfo) {
                System.out.println("q " + q.toString(RADIX) + " is not a prime");
            }
        }
        if (!p.multiply(q).equals(modulus)) {
            isValid = false;
            if (writeInfo) {
                System.out.println("Modulus " + modulus.toString(RADIX) + " has not factors p a q");
            }
        }
        BigInteger phi = modulus.subtract(p).subtract(q).add(BigInteger.ONE);
        if (!phi.gcd(exponent).equals(BigInteger.ONE)) {
            isValid = false;
            if (writeInfo) {
                System.out.println("Exponent " + exponent.toString(RADIX)
                        + " is not coprime to phi of " + modulus.toString(RADIX));
            }
        }
        checkedValidity = true;
        validKey = isValid;
        return isValid;
    }

    public BigInteger getPrimeDifference() {
        return p.subtract(q).abs();
    }

    public BigInteger getPrivateExponent() {
        BigInteger phi = modulus.subtract(p).subtract(q).add(BigInteger.ONE);
        return exponent.modInverse(phi);
    }

    public static Params readPublicKeyFromTlv(String value) throws WrongKeyException {
        Params params = new Params();
        BigInteger[] values = parseTlv(value);
        if (values.length != 2) {
            throw new WrongKeyException("Public key " + value + " not composed from 2 values");
        }
        params.setExponent(values[0]);
        params.setModulus(values[1]);
        return params;
    }

    public void readPrivateKeyFromTlv(String value) throws WrongKeyException {
        BigInteger[] values = parseTlv(value);
        if (values.length != 2) {
            throw new WrongKeyException("Private key " + value + "  not composed from 2 values");
        }
        setP(values[0]);
        setQ(values[1]);
    }

    private static BigInteger[] parseTlv(String values) throws WrongKeyException {
        int totalLength = values.length();
        if (totalLength % 2 == 1) {
            throw new WrongKeyException("Odd String length of " + values);
        }
        List<BigInteger> result = new ArrayList<>();
        int length;
        for (int offset = 0; offset != totalLength; offset += 6 + 2 * length) {
            if (offset > totalLength) {
                throw new WrongKeyException("Invalid TLV length in " + values);
            }
            if (!values.startsWith("82", offset)) {
                throw new WrongKeyException("Invalid TLV type in " + values);
            }
            length = Integer.parseInt(values.substring(offset + 2, offset + 6), 16);
            String value = values.substring(offset + 6, offset + 6 + 2 * length);
            result.add(new BigInteger(value, 16));
        }
        return result.toArray(new BigInteger[result.size()]);
    }

    public void writeToFile(BufferedWriter writer, long keyNumber) throws IOException {
        writer.write(Long.toString(keyNumber));
        writer.write(';');
        writer.write(modulus.toString(16).toUpperCase());
        writer.write(';');
        writer.write(exponent.toString(2));
        writer.write(';');
        writer.write(p.toString(16).toUpperCase());
        writer.write(';');
        writer.write(q.toString(16).toUpperCase());
        writer.write(';');
        writer.write(getPrivateExponent().toString(16).toUpperCase());
        writer.write(';');
        writer.write(Long.toString(time));
        writer.newLine();
    }
}
