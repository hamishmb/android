package com.telenav.osv.data.location.model;

import android.location.Location;

import com.telenav.osv.data.KVBaseModel;

/**
 * Custom location model which encapsulates the location entity from the persistence.
 * <p>
 * The model include an {@code Android} location object. This is not used to replace the location but only to addChild specific information for the app.
 *
 * @author horatiuf
 */
public class KVLocation extends KVBaseModel {

    /**
     * The {@code Android} location which is encapsulated into the custom location model of the KV app.
     */
    private Location location;

    /**
     * The sequence identifier for which the location corresponds to.
     */
    private String sequenceId;

    /**
     * Default constructor for the current class.
     */
    public KVLocation(String ID, Location location, String sequenceId) {
        super(ID);
        this.location = location;
        this.sequenceId = sequenceId;
    }

    /**
     * @return {@code Location} representing {@link #location}.
     */
    public Location getLocation() {
        return location;
    }

    /**
     * @return {@code String} representing {@link #sequenceId}.
     */
    public String getSequenceId() {
        return sequenceId;
    }
}
