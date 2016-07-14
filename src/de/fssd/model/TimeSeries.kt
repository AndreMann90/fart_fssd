package de.fssd.model


/**
 * This interface decouples the [Markov] and [Evaluation]. Inject this Interface in [Evaluation].
 *
 * Since this interface is implemented by [Markov] and [TimeSeriesFromCSV], the [Evaluation] can be tested with the
 * CSV data instead and the [Markov] can be easily compared with the CSV Data.
 */
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
    fun getProbabilitySeries(varID: Int): List<Float>?
}