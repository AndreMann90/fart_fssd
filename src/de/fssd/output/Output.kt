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
        val vars = mcVariables.map({ v -> v.name})
        println("vars: $vars")

        BufferedWriter(FileWriter("data.csv")).use { br ->
            val writer = CsvListWriter(br, CsvPreference.STANDARD_PREFERENCE)
            val header = mutableListOf("time")
            header.addAll(mcVariables.map { v -> v.name })
            header.addAll((0..topEvents.size - 1).map { i -> "topEvent$i" })
            writer.writeHeader(*header.toTypedArray())
            for(time in 0..sampleCount) {
                val row = mutableListOf(time * sampleTime)
                row.addAll(mcVariables.map { v -> v.timeSeries[time] })
                row.addAll(topEvents.map { te -> te[time] })
                writer.write(row)
            }
            writer.flush()
        }
    }

    private fun plot() {
        println("plotting stuff")
        val pb = ProcessBuilder("python", "src/de/fssd/output/TimeseriesPlotter.py", "data.csv")
        // val pb = ProcessBuilder("python3.4", "-c", "import os; print(os.getcwd())")
        pb.redirectErrorStream(true)
        val p = pb.start()
        val r = p.inputStream.bufferedReader()
        for (l in r.lines()) {
            println("Child said: $l")
        }
        println("Waiting for plotter to exit")
        p.waitFor()
    }
}