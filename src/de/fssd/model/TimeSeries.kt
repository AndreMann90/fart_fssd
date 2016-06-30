package de.fssd.model


interface TimeSeries {

    /**
     * Returns the number of timestamps in the series
     * @return number of timestamps
     */
    val samplePointsCount: Int

    /**
     * Returns the timeseries for a given variable id
     * @param varID variable id
     * *
     * @return timeseries
     */
    fun getProbabilitySeries(varID: Int): List<Float>
}