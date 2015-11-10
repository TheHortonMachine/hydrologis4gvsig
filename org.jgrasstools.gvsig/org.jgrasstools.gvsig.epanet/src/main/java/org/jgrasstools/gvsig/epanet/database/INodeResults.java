package org.jgrasstools.gvsig.epanet.database;

import org.joda.time.DateTime;

public interface INodeResults extends IResult {

    public abstract String getId();

    public abstract EpanetRun getRun();

    public abstract DateTime getUtcTime();

    public abstract float getDemand();

    public abstract float getHead();

    public abstract float getPressure();

    public abstract float getQuality();

}