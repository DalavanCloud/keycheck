package cz.muni.fi.keycheck.helpers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Peter Sekan, peter.sekan@mail.muni.cz
 * @version 07.11.2015
 */
public class ArgumentsPair {
    private ArrayList<String> files = new ArrayList<>();
    private Set<String> params = new HashSet<>();

    public ArrayList<String> getFiles() {
        return files;
    }

    public Set<String> getParams() {
        return params;
    }

    public void paramsAdd(String param) {
        params.add(param);
    }

    public void paramsAddAll(Set<String> params) {
        this.params.addAll(params);
    }

    public void filesAdd(String fileName) {
        files.add(fileName);
    }
}
