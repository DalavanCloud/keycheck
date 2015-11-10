package cz.muni.fi.keycheck.tests;

import cz.muni.fi.keycheck.helpers.CollectionsHelper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class CollectionsHelperTest {
    private Map<Long, Long> testMap;

    @Before
    public void setUp() throws Exception {
        testMap = new HashMap<>();
        testMap.put(10L, 0L);
        testMap.put(12L, 2L);
        testMap.put(13L, -1L);
        testMap.put(23L, 21L);
        testMap.put(32L, 13L);
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void testComputePercentage() throws Exception {
        Long min = 10L;
        Long max = 32L;

        double sum = 35;
        Map<Long, Double> newMap = CollectionsHelper.computePercentage(testMap, (long)sum, min, max);
        for (Map.Entry<Long, Long> entry : testMap.entrySet()) {
            Long key = entry.getKey();
            assertEquals(newMap.get(key), (entry.getValue()*100.0)/sum, 0.0001);
        }
        assertEquals(CollectionsHelper.sumOfCollectionDouble(newMap.values()), 100, 0.0001);
    }

    @Test
    public void testComputePercentageWithBinning() throws Exception {
        double sum = 35;
        Map<Long, Double> newMap = CollectionsHelper.computePercentageWithBinning(testMap, 10, (long) sum);
        assertTrue(newMap.size() == 3);
        assertEquals(newMap.get(10L), (1 * 100.0) / sum, 0.0001);
        assertEquals(newMap.get(20L), (21 * 100.0) / sum, 0.0001);
        assertEquals(newMap.get(30L), (13 * 100.0) / sum, 0.0001);

        newMap = CollectionsHelper.computePercentageWithBinning(testMap, 5, (long) sum, 6L, 40L);
        assertTrue(newMap.size() == 7);
        assertEquals(newMap.get(6L), (0 * 100.0) / sum, 0.0001);
        assertEquals(newMap.get(11L), (1 * 100.0) / sum, 0.0001);
        assertEquals(newMap.get(16L), (0 * 100.0) / sum, 0.0001);
        assertEquals(newMap.get(21L), (21 * 100.0) / sum, 0.0001);
        assertEquals(newMap.get(26L), (0 * 100.0) / sum, 0.0001);
        assertEquals(newMap.get(31L), (13 * 100.0) / sum, 0.0001);
        assertEquals(newMap.get(36L), (0 * 100.0) / sum, 0.0001);
    }

    @Test
    public void testIncrementMap() throws Exception {
        long newVal = CollectionsHelper.incrementMap(testMap, 10);
        assertTrue(newVal == 1);

        newVal = CollectionsHelper.incrementMap(testMap, 13);
        assertTrue(newVal == 0);

        assertFalse(testMap.containsKey(11L));
        newVal = CollectionsHelper.incrementMap(testMap, 11);
        assertTrue(newVal == 1);
    }

    @Test
    public void testInsertIfNotContains() throws Exception {
        Long bTen = 10L;
        Long bEleven = 11L;

        CollectionsHelper.insertIfNotContains(testMap, bTen, 2L);
        assertTrue(testMap.get(bTen) == 0L);

        CollectionsHelper.insertIfNotContains(testMap, bEleven, 2L);
        assertTrue(testMap.containsKey(bEleven));
        assertTrue(testMap.get(bEleven) == 2L);
    }

    @Test
    public void testSumOfCollection() throws Exception {
        assertTrue(CollectionsHelper.sumOfCollection(testMap.values()) == 35L);
    }
}