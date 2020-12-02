package com.telenav.osv.data.collector.datatype.datatypes

import com.telenav.osv.data.collector.datatype.util.LibraryUtil

/**
 * Get the estimated horizontal accuracy of this location, radial, in meters.
 * We define horizontal accuracy as the radius of 68% confidence. In other words, if you draw a circle centered at this location's latitude and longitude, and with a radius
 * equal to the accuracy, then there is a 68% probability that the true location is inside the circle.
 * This accuracy estimation is only concerned with horizontal accuracy, and does not indicate the accuracy of bearing, velocity or altitude if those are included in this Location.
 * If this location does not have a horizontal accuracy, then 0.0 is returned. All locations generated by the LocationManager include horizontal accuracy.
 */
class AccuracyObject(accuracy: Float, statusCode: Int) : BaseObject<Float?>(accuracy, statusCode, LibraryUtil.PHONE_GPS_ACCURACY) {
    constructor(statusCode: Int) : this(Float.MIN_VALUE, statusCode) {}

    /**
     * Returns the estimated accuracy of the location, in meters
     */
    val accuracy: Float?
        get() = actualValue

    init {
        dataSource = LibraryUtil.PHONE_SOURCE
    }
}