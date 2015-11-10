package cz.muni.fi.keycheck.exports;

import cz.muni.fi.keycheck.base.ExportContainer;
import cz.muni.fi.keycheck.base.Params;

/**
 * @author Peter Sekan, peter.sekan@mail.muni.cz
 * @version 02.11.2015
 */
public class TimeExport extends ExportContainer {
    @Override
    protected String[] getFilesName() {
        return new String[]{
                "time.export.dat"
        };
    }

    @Override
    protected String[] getTransformed(Params params) {
        return new String[]{
                Long.toString(params.getTime())
        };
    }
}
