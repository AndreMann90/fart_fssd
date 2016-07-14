package de.fssd

import de.fssd.evaluation.Evaluation
import de.fssd.model.BDDBuilder
import de.fssd.output.Output
import de.fssd.parser.Parser
import java.io.File
import java.io.IOException

object Main {

    @JvmStatic fun main(args: Array<String>) {
        if (args.size != 1) {
            println("One Argument with the file location of the HFT expected")
        } else {
            try {
                val faultTree = Parser.parse(if (args[0] == "-") null else File(args[0]))
                val result = BDDBuilder().build(faultTree)
                val evaluation = Evaluation(result.markov)
                val topEventResults = evaluation.evaluateMultipleRootNodes(result.rootNodes)
                Output.writeOutput(faultTree.sampleCount, faultTree.missionTime / (faultTree.sampleCount - 1), result.markov.variables, topEventResults.values)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

    }
}
