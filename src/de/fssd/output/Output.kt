package de.fssd.output

import de.fssd.model.McVariable
import org.supercsv.io.CsvListWriter
import org.supercsv.prefs.CsvPreference
import java.io.File
import kotlin.system.measureTimeMillis

/**
 * Concerned with writing the Output.
 *
 * Writes a temporary CSV file and then executes Python. Requires Python 3 to be installed.
 */
object Output {

    fun writeOutput(sampleCount: Int, sampleTime: Float, mcVariables: List<McVariable>, topEvents: Collection<List<Float>>) {
        for (v in mcVariables) {
            assert(v.timeSeries.size == sampleCount)
        }
        for (te in topEvents) {
            assert(te.size == sampleCount)
        }
        val time = measureTimeMillis {
            val csv = toCsv(sampleCount, sampleTime, mcVariables, topEvents)
            plot(csv)
        }
        System.err.println("Writing output took $time milliseconds")
    }

    private fun toCsv(sampleCount: Int, sampleTime: Float, mcVariables: List<McVariable>, topEvents: Collection<List<Float>>): File {
        val tmpf = File.createTempFile("htfa", ".csv")
        tmpf.bufferedWriter().use { bw ->
            val writer = CsvListWriter(bw, CsvPreference.STANDARD_PREFERENCE)
            val header = mutableListOf("time")
            header.addAll(mcVariables.map { v -> v.name })
            header.addAll((0..topEvents.size - 1).map { i -> "topEvent$i" })
            writer.writeHeader(*header.toTypedArray())
            for(time in 0..sampleCount-1) {
                val row = mutableListOf(time * sampleTime)
                row.addAll(mcVariables.map { v -> v.timeSeries[time] })
                row.addAll(topEvents.map { te -> te[time] })
                writer.write(row)
            }
            writer.flush()
        }

        return tmpf
    }

    private fun plot(csv: File) {
        val s = javaClass.classLoader.getResourceAsStream("TimeseriesPlotter.py")
        val tmpf = File.createTempFile("htfa", ".py")
        tmpf.bufferedWriter().use { w ->
            for (l in s.bufferedReader().lines()) {
                w.append(l)
                w.newLine()
            }
            w.flush()
        }

        val tmphtml = File.createTempFile("htfa", ".html")
        val pb = ProcessBuilder("python", tmpf.absolutePath, csv.absolutePath, tmphtml.absolutePath)

        val p = pb.start()
        p.errorStream.bufferedReader().use { r ->
            for (l in r.readLines())
                System.err.println(l)
        }
        p.waitFor()

        tmpf.delete()

        csv.bufferedReader().use { r ->
            for (l in r.readLines())
                System.out.println(l)
        }
        csv.delete()
    }
}