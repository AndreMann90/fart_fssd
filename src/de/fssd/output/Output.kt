package de.fssd.output

import de.fssd.model.McVariable
import org.supercsv.io.CsvListWriter
import org.supercsv.prefs.CsvPreference
import java.io.BufferedWriter
import java.io.FileWriter

object Output {

    fun writeOutput(sampleCount: Int, sampleTime: Float, mcVariables: List<McVariable>, topEvents: Collection<List<Float>>) {
        for (v in mcVariables) {
            assert(v.timeSeries.size == sampleCount)
        }
        for (te in topEvents) {
            assert(te.size == sampleCount)
        }
        toCsv(sampleCount, sampleTime, mcVariables, topEvents)
        plot()
    }

    private fun toCsv(sampleCount: Int, sampleTime: Float, mcVariables: List<McVariable>, topEvents: Collection<List<Float>>) {
        BufferedWriter(FileWriter("data.csv")).use { br ->
            val writer = CsvListWriter(br, CsvPreference.STANDARD_PREFERENCE)
            val header = mutableListOf("time")
            header.addAll(mcVariables.map { v -> v.name })
            header.addAll((0..topEvents.size).map { i -> "topEvent$i" })
            writer.writeHeader(*header.toTypedArray())
            for(time in 0..sampleCount) {
                val row = mutableListOf(time * sampleTime)
                row.addAll(mcVariables.map { v -> v.timeSeries[time] })
                row.addAll(topEvents.map { te -> te[time] })
                writer.write(row)
            }
        }
    }

    private fun plot() {
        Runtime.getRuntime().exec(arrayOf("python", "TimeseriesPlotter.py"));  // TODO pass file location
    }
}