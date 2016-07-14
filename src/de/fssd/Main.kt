package de.fssd

import de.fssd.evaluation.Evaluation
import de.fssd.model.BDDBuilder
import de.fssd.parser.Parser
import java.io.File
import java.io.IOException

object Main {

    @JvmStatic fun main(args: Array<String>) {
        if (args.size != 0) {
            println("One Argument with the file location of the HTF expected")
        } else {
            val file_location = args[0]
            try {
                val parser = Parser()
                val faultTree = parser.parse(File(file_location))
                val result = BDDBuilder().build(faultTree)
                val evaluation = Evaluation(result.markov)
                val topEventResults = evaluation.evaluateMultipleRootNodes(result.rootNodes)
                for ((rootNode, teSeries) in topEventResults) {
                    println("Event series for root node $rootNode: $teSeries")
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

    }
}
