package org.jgrasstools.gvsig.epanet.database;

import org.joda.time.DateTime;

public interface ILinkResults extends IResult {
    
    public abstract String getId();

    public abstract EpanetRun getRun();

    public abstract DateTime getUtcTime();

    public abstract float getFlow1();

    public abstract float getFlow2();

    public abstract float getVelocity1();

    public abstract float getVelocity2();

    public abstract float getHeadloss();

    public abstract float getStatus();

    public abstract float getEnergy();

}