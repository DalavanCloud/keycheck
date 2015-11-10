package cz.muni.fi.keycheck.base;

/**
 * @author David Formanek
 */
public interface Stats {
    void process(Params params);

    void print();

    void changeCard(String icsn);

    void changeCard(String icsn, long numOfKeys);
}
