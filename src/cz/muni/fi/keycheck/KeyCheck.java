package cz.muni.fi.keycheck;

import cz.muni.fi.keycheck.base.*;
import cz.muni.fi.keycheck.exports.DifferenceExport;
import cz.muni.fi.keycheck.exports.TimeExport;
import cz.muni.fi.keycheck.helpers.ArgumentsPair;
import cz.muni.fi.keycheck.helpers.RedirectOutput;
import cz.muni.fi.keycheck.stats.*;

import java.io.*;
import java.util.*;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.text.DecimalFormat;

/**
 * @author David Formanek
 * @author Peter Sekan, peter.sekan@mail.muni.cz
 */
public class KeyCheck extends RedirectOutput {
    //ALL POSSIBLE PARAMETERS
    private static final String PARAMETER_NEW_FORMAT = "new-format";
    private static final String PARAMETER_TRANSFORM = "transform";
    private static final String PARAMETER_GENERATE = "generate";
    private static final String PARAMETER_BASE = "base";
    private static final String PARAMETER_BITS = "bits";
    private static final String PARAMETER_BYTES = "bytes";
    private static final String PARAMETER_DIFFERENCE = "difference";
    private static final String PARAMETER_STRENGTH = "strength";
    private static final String PARAMETER_TIME = "time";
    private static final String PARAMETER_ALL = "all";

    //INFORMATION FOR KEYS GENERATING
    private static boolean GENERATE_KEYS = false;
    private static int GENERATED_PRIME_BIT_LENGTH = 512;
    private static final long GENERATED_KEY_COUNT = 50000;
    private static final BigInteger EXPONENT_FOR_GENERATED = new BigInteger("65537");

    //PROGRESS INFORMATION
    private static final long STATUS_MESSAGE_AFTER = 500000000L;
    private static long keyCount = 0;
    private static long startTime;
    private static long lastStatusMessageTime;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws InterruptedException {
        redirectOutputToFile("stats.txt");

        ArgumentsPair arguments = parseArgs(args);
        Set<String> params = arguments.getParams();
        ArrayList<String> files = arguments.getFiles();

        boolean useNewFormat = params.contains(PARAMETER_NEW_FORMAT);
        if (useNewFormat) {
            params.remove(PARAMETER_NEW_FORMAT);
        }
        if (params.size() == 0) {
            System.err.println("No parameter for analyse specified.\n");
        }
        if (files.size() == 0) {
            System.err.println("No file for analyse specified.\n");
        }
        if (params.size() == 0 || files.size() == 0) {
            printHelp();
            return;
        }

        Stats stats = init(params);
        if (GENERATE_KEYS) {
            generate(GENERATED_KEY_COUNT, GENERATED_PRIME_BIT_LENGTH);
        } else {
            for (String filename : files) {
                try {
                    if (useNewFormat) {
                        loadNewFormat(filename, stats);
                    } else {
                        load(filename, stats);
                    }
                } catch (IOException ex) {
                    System.err.println("IO error: " + ex.getMessage());
                }
            }
            printStats(stats);
        }
    }

    /**
     * Parse arguments to parameters and files
     * @param args the command line arguments
     * @return pair, key is set of params and value is list of files
     */
    private static ArgumentsPair parseArgs(String args[]) {
        String allArgs[] = new String[] {
                PARAMETER_NEW_FORMAT,
                PARAMETER_TRANSFORM,
                PARAMETER_BASE,
                PARAMETER_BITS,
                PARAMETER_BYTES,
                PARAMETER_DIFFERENCE,
                PARAMETER_STRENGTH,
                PARAMETER_TIME
        };
        Set<String> allArgsSet = new HashSet<>(Arrays.asList(allArgs));
        ArgumentsPair argumentsPair = new ArgumentsPair();

        for (int i = 0; i < args.length; i++) {
            String param = args[i].substring(1);
            if (allArgsSet.contains(param)) {
                argumentsPair.paramsAdd(param);
            } else if (param.equals(PARAMETER_ALL)) {
                argumentsPair.paramsAddAll(allArgsSet);
                argumentsPair.getParams().remove(PARAMETER_NEW_FORMAT);
                argumentsPair.getParams().remove(PARAMETER_TRANSFORM);
            } else if (param.equals(PARAMETER_GENERATE)) {
                if (args.length <= i + 1) {
                    System.err.println("Wrong -" + PARAMETER_GENERATE + " parameter. Use -" + PARAMETER_GENERATE + " keyBitLength. (keyBitLength = 512|1024)");
                }
                else {
                    int keyBitLength = Integer.valueOf(args[++i]);
                    switch (keyBitLength) {
                        case 1024:
                            GENERATE_KEYS = true;
                            GENERATED_PRIME_BIT_LENGTH = 512;
                            break;
                        case 512:
                            GENERATE_KEYS = true;
                            GENERATED_PRIME_BIT_LENGTH = 256;
                            break;
                        default:
                            System.err.println("Wrong -" + PARAMETER_GENERATE + " parameter. Use -" + PARAMETER_GENERATE + " keyBitLength. (keyBitLength = 512|1024)");
                    }
                }
            } else {
                argumentsPair.filesAdd(args[i]);
            }
        }
        return argumentsPair;
    }

    /**
     * Init container for statistics
     * @param params set of parameters
     * @return new StatsContainer
     */
    private static Stats init(Set<String> params) {
        StatsContainer stats = new StatsContainer();
        if (params.contains(PARAMETER_TRANSFORM)) {
            stats.add(new FormatTransform());
        }
        if (params.contains(PARAMETER_BASE)) {
            stats.add(new CardStatsPercentageContainer<>(PrimeBaseStats.class));
        }
        if (params.contains(PARAMETER_BITS)) {
            stats.add(new CardStatsPercentageContainer<>(AllBitsStats.class));
        }
        if (params.contains(PARAMETER_BYTES)) {
            stats.add(new CardStatsPercentageContainer<>(AllBytesStats.class));
            AllBytesStats.setBytesToAnalyse(1,1);
        }
        if (params.contains(PARAMETER_DIFFERENCE)) {
            stats.add(new CardStatsPercentageContainer<>(PrimeDifferenceStats.class));
            stats.add(new DifferenceExport());
        }
        if (params.contains(PARAMETER_STRENGTH)) {
            stats.add(new CardStatsPercentageContainer<>(PrimeStrengthStats.class));
        }
        if (params.contains(PARAMETER_TIME)) {
            stats.add(new TimeExport());
            stats.add(new CardStatsPercentageContainer<>(TimeStats.class, 10L));
            stats.add(new CardStatsPercentageContainer<>(TimeStats.class, 37L));
            stats.add(new CardStatsPercentageContainer<>(TimeStats.class, 79L));
        }
        startTime = System.nanoTime();
        return stats;
    }

    /**
     * Generate [count] keys with [bits*2] bit length using SecureRandom
     * Save this keys to file in new format
     * @param count number of keys to generate
     * @param bits bit length of one prime
     */
    private static void generate(long count, int bits) {
        final SecureRandom rnd = new SecureRandom();
        System.out.println("Generating " + GENERATED_KEY_COUNT + " keys");
        getConsole().println("Generating " + GENERATED_KEY_COUNT + " keys");

        String filename = "keycheck." + (bits * 2) + "b.csv";
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            lastStatusMessageTime = System.nanoTime();
            long numOfKeys = GENERATED_KEY_COUNT,
                    startFileTime = System.nanoTime(),
                    startKeyTime = System.currentTimeMillis();
            for (int actualKey = 0; actualKey < count; actualKey++) {
                BigInteger p = new BigInteger(bits, 40, rnd);
                BigInteger q = new BigInteger(bits, 40, rnd);
                BigInteger modulus = p.multiply(q);
                BigInteger phi = modulus.subtract(p).subtract(q).add(BigInteger.ONE);
                if (!phi.gcd(EXPONENT_FOR_GENERATED).equals(BigInteger.ONE)) {
                    actualKey--;
                    continue;
                }
                int time = (int) (System.currentTimeMillis() - startKeyTime);

                Params params = new Params();
                params.setP(p);
                params.setQ(q);
                params.setExponent(EXPONENT_FOR_GENERATED);
                params.setModulus(modulus);
                params.setTime(time);

                params.writeToFile(writer, actualKey + 1);

                showProgress(actualKey + 1, numOfKeys, startFileTime);
                startKeyTime = System.currentTimeMillis();
            }
        } catch (FileNotFoundException ex) {
            System.err.println("File '" + filename + "' not found");
        } catch (IOException ex) {
            System.err.println("Error on write to file '" + filename + "'");
        } finally {
            consoleDoneLine();
        }
    }

    /**
     * Load keys from file in old format
     * @param filename csv file in old format
     * @param stats StatsContainer with analysis on this keys
     * @throws IOException
     */
    private static void load(String filename, Stats stats) throws IOException {
        System.out.println("Analysing file: " + filename);
        getConsole().println("Analysing file: " + filename.substring(0, Integer.min(filename.length(), 60)) + (filename.length() > 60 ? "..." : ""));

        String line;
        long numOfKeys = 0,
                actualKey = 0,
                actualLineNumber = 0,
                startFileTime = System.nanoTime();
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            while ((line = reader.readLine()) != null) {
                String[] tuple = line.split(":", 2);
                if (tuple.length == 2 && tuple[0].equals("PRIV")) {
                    numOfKeys++;
                }
            }
        } catch (FileNotFoundException ex) {
            System.err.println("File '" + filename + "' not found");
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            lastStatusMessageTime = System.nanoTime();
            Params params = null;
            while ((line = reader.readLine()) != null) {
                actualLineNumber++;
                String[] tuple = line.split(";", 2);
                if (tuple[0].equals("CPLC.ICSerialNumber")) {
                    stats.changeCard(tuple[1]);
                }

                tuple = line.split(":", 2);
                if (tuple.length != 2) {
                    continue;
                }
                String value = tuple[1].replaceAll("\\s", "");
                switch (tuple[0]) {
                    case "PUBL":
                        if (params != null) {
                            throw new WrongKeyException("Loading public key on line " + actualLineNumber + " while another public key is loaded");
                        }
                        params = Params.readPublicKeyFromTlv(value);
                        break;
                    case "PRIV":
                        if (params == null) {
                            throw new WrongKeyException("Loading private key on line " + actualLineNumber + " while public key not loaded");
                        }
                        params.readPrivateKeyFromTlv(value);
                        break;
                    default:
                        if (tuple[0].charAt(0) == '#') {
                            if (params == null) {
                                throw new WrongKeyException("Loading time on line " + actualLineNumber + " while public key not loaded");
                            }

                            int time = (int) (Double.parseDouble(tuple[1]) * 1000.0);
                            params.setTime(time);
                            stats.process(params);

                            params = null;
                            actualKey++;
                            showProgress(actualKey, numOfKeys, startFileTime);
                        }
                }
            }
        } catch (FileNotFoundException ex) {
            System.err.println("File '" + filename + "' not found");
        } catch (WrongKeyException ex) {
            System.err.println("File '" + filename + "' is in wrong format.\n" + ex.getMessage());
        } finally {
            consoleDoneLine();
        }
    }

    /**
     * Load keys from file in new format
     * @param filename csv file in new format
     * @param stats StatsContainer with analysis on this keys
     * @throws IOException
     */
    private static void loadNewFormat(String filename, Stats stats) throws IOException {
        System.out.println("Analysing file: " + filename);
        getConsole().println("Analysing file: " + filename.substring(0, Integer.min(filename.length(), 60)) + (filename.length() > 60 ? "..." : ""));

        lastStatusMessageTime = System.nanoTime();
        String line;
        long numOfKeys = 0,
                actualKey = 0,
                startFileTime = System.nanoTime();
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            while ((line = reader.readLine()) != null) {
                String tuple[] = line.replace(",", ";").split(";", 7);
                if (tuple.length == 7 && tuple[0].matches("\\d+")) {
                    numOfKeys++;
                }
            }
        } catch (FileNotFoundException ex) {
            System.err.println("File '" + filename + "' not found");
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            int pos = filename.lastIndexOf('.');
            String icsn = filename;
            if (pos >= 0) {
                icsn = icsn.substring(0, pos);
            }
            stats.changeCard(icsn);
            while ((line = reader.readLine()) != null) {
                String tuple[] = line.replace(",", ";").split(";", 7);
                if (tuple.length != 7 || !tuple[0].matches("\\d+")) {
                    continue;
                }

                try {
                    Params params = new Params();
                    params.setModulus(new BigInteger(tuple[1], 16));
                    params.setExponent(new BigInteger(tuple[2], 2));
                    params.setP(new BigInteger(tuple[3], 16));
                    params.setQ(new BigInteger(tuple[4], 16));
                    params.setTime(Long.valueOf(tuple[6]));
                    stats.process(params);

                    actualKey++;
                    showProgress(actualKey, numOfKeys, startFileTime);
                } catch (NumberFormatException ex) {
                    String message = "\nKey " + actualKey + " is not correct.";
                    getConsole().println(message);
                    System.out.println(message);
                    System.out.println("  " + line);
                }
            }
        } catch (FileNotFoundException ex) {
            System.err.println("File '" + filename + "' not found");
        } finally {
            consoleDoneLine();
        }
    }

    /**
     * Show analysis/generating progress
     * @param actualKey actual key number
     * @param numOfKeys number of all keys to analysis/generating
     * @param startFileTime time in nanoseconds from start file analysis
     */
    private static void showProgress(long actualKey, long numOfKeys, long startFileTime) {
        keyCount++;
        if ((System.nanoTime() - lastStatusMessageTime) > STATUS_MESSAGE_AFTER || (actualKey == numOfKeys)) {
            long elapsedTime = (System.nanoTime() - startFileTime) / 1000000;

            DecimalFormat formatter = new DecimalFormat("#0.00");
            consoleReplaceLine("Processed keys: " + actualKey + " / " + numOfKeys + " | " + "Avg. speed: " + formatter.format(actualKey / (elapsedTime / 1000.0)) + " keys/s");
            lastStatusMessageTime = System.nanoTime();
        }
    }

    /**
     * Print stats when analysis/generating finished
     * @param stats StatsContainer with keys analysis
     */
    private static void printStats(Stats stats) {
        long elapsedTime = (System.nanoTime() - startTime) / 1000000;
        System.out.println("Analysis completed - " + keyCount + " keys processed in " + elapsedTime + " ms");
        getConsole().println("Analysis completed - " + keyCount + " keys processed in " + elapsedTime + " ms");
        stats.print();
    }

    /**
     * Print help how to use keycheck
     */
    private static void printHelp() {
        getConsole().println("Keycheck\n" +
                "\n" +
                "Usage:\n" +
                "  keycheck.jar parameter1 parameter2 ... file1 file2 ...\n" +
                "Example:\n" +
                "  java -jar keycheck.jar -" + PARAMETER_BASE + " -" + PARAMETER_BITS + " file1.csv file2.csv" +
                "\n" +
                "Parameters:\n" +
                "  -" + PARAMETER_GENERATE + " [512|1024] Generate " + GENERATED_KEY_COUNT + " keys\n" +
                "  -" + PARAMETER_NEW_FORMAT + "          New format will be use to load keys from file\n" +
                "  -" + PARAMETER_TRANSFORM + "           Transform all keys to new format to file 'CARD_ICSN.csv'\n" +
                "  -" + PARAMETER_BASE + "                Check base stats of keys\n" +
                "  -" + PARAMETER_BITS + "                Generate statistics for all bits\n" +
                "  -" + PARAMETER_BYTES + "               Generate statistics for all bytes\n" +
                "  -" + PARAMETER_DIFFERENCE + "          Check primes difference\n" +
                "  -" + PARAMETER_STRENGTH + "            Check primes strength\n" +
                "  -" + PARAMETER_TIME + "                Generate time statistics\n" +
                "  -" + PARAMETER_ALL + "                 Generate all statistics and check all tests.\n");
    }
}