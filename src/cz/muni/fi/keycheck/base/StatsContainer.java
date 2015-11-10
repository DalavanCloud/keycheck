package cz.muni.fi.keycheck.base;

import java.util.ArrayList;
import java.util.List;

/**
 * @author David Formanek
 */
public class StatsContainer implements Stats {
    private List<Stats> statsInstances = new ArrayList<>();

    public void add(Stats stats) {
        statsInstances.add(stats);
    }

    @Override
    public void process(Params params) {
        for (Stats statsInstance : statsInstances) {
            statsInstance.process(params);
        }
    }

    @Override
    public void print() {
        for (Stats statsInstance : statsInstances) {
            statsInstance.print();
        }
    }

    @Override
    public void changeCard(String icsn) {
        for (Stats statsInstance : statsInstances) {
            statsInstance.changeCard(icsn);
        }
    }

    @Override
    public void changeCard(String icsn, long numOfKeys) {
        for (Stats statsInstance : statsInstances) {
            statsInstance.changeCard(icsn, numOfKeys);
        }
    }
}
