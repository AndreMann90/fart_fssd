package de.fssd

import de.fssd.evaluation.Evaluation
import de.fssd.model.BDDBuilder
import de.fssd.output.Output
import de.fssd.parser.Parser
import java.io.File
import java.io.IOException

object Main {

    /**
     * Run the HFT Analysis.
     *
     * @param args One Argument with the file location of the HFT expected
     */
    @JvmStatic fun main(args: Array<String>) {
        if (args.size != 1) {
            println("One Argument with the file location of the HFT expected")
        } else if (!File(args[0]).exists()) {
            println("The file '${args[0]}' does not exist")
        } else {
            try {
                val faultTree = Parser.parse(if (args[0] == "-") null else File(args[0]))
                val result = BDDBuilder().build(faultTree)
                val evaluation = Evaluation(result.markov)
                val topEventResults = evaluation.evaluateMultipleRootNodes(result.rootNodes)
                Output.writeOutput(faultTree.sampleCount, faultTree.sampleTime, result.markov.variables, topEventResults.values)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

    }
}
