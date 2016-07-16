package de.fssd.parser

import com.google.gson.GsonBuilder
import de.fssd.dataobjects.FaultTree
import java.io.*
import kotlin.system.measureTimeMillis

/**
 * Parses the HTF in JSON Format
 */
object Parser {
    private val gson = GsonBuilder().create()

    @Throws(IOException::class)
    fun parse(fromFile: File?): FaultTree {
        var faultTree : FaultTree? = null
        val time = measureTimeMillis {
            val reader: InputStreamReader
            if (fromFile == null)
                reader = InputStreamReader(System.`in`)
            else
                reader = InputStreamReader(BufferedInputStream(FileInputStream(fromFile)))
            faultTree = gson.fromJson(reader, FaultTree::class.java)
            if (faultTree!!.missionTime <= 0f) {
                throw ParseException("The missionTime must be greater 0")
            } else if(faultTree!!.sampleCount <= 0) {
                throw ParseException("The sampleCount must be greater 0")
            } else if(faultTree!!.nodes.isEmpty()) {
                throw ParseException("No nodes found")
            } else if(faultTree!!.chain.isEmpty()) {
                throw ParseException("No states found")
            }
        }
        System.err.println("Parsing the HTF took $time milliseconds")
        return faultTree!!
    }
}
