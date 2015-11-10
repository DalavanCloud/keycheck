package cz.muni.fi.keycheck.stats;

import cz.muni.fi.keycheck.base.CardStats;
import cz.muni.fi.keycheck.base.Params;
import cz.muni.fi.keycheck.helpers.CollectionsHelper;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Peter Sekan, peter.sekan@mail.muni.cz
 * @version 27.10.2015
 */
public class TimeStats extends CardStats {
    private Map<Long, Long> time = new HashMap<>();

    public TimeStats(String icsn) {
        super(icsn);
    }

    public TimeStats(String icsn, long numOfKey) {
        super(icsn, numOfKey);
    }

    @Override
    protected String[] getDataNames() {
        return new String[]{
                "time.dat"
        };
    }

    @Override
    protected Map<Long, Long> getData(int dataType) {
        switch (dataType) {
            case 0:
                return time;
            default:
                return new HashMap<>();
        }
    }

    @Override
    public void process(Params params) {
        if (!params.isValidKey()) {
            return;
        }
        super.process(params);
        CollectionsHelper.incrementMap(time, params.getTime());
    }

    @Override
    public void print() {
        CollectionsHelper.insertIfNotContains(time, 0L, 0L);
    }
}
