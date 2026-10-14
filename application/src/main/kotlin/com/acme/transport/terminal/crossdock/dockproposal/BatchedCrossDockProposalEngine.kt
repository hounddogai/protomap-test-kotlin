package com.acme.transport.terminal.crossdock.dockproposal

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the batched cross dock proposal calculation. */
interface BatchedCrossDockProposalFactor {
    fun describe(): String
    fun adjust(item: BatchedCrossDockProposal, value: Double): Double
}

data class FlaggedBatchedCrossDockProposalAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : BatchedCrossDockProposalFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: BatchedCrossDockProposal, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingBatchedCrossDockProposalAdjuster(var step: Double) : BatchedCrossDockProposalFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: BatchedCrossDockProposal, value: Double): Double = roundTo(value, step)
}

data class PercentBatchedCrossDockProposalAdjuster(var percent: Double) : BatchedCrossDockProposalFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: BatchedCrossDockProposal, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the batched cross dock proposal steps in order and keeps a trace of each. */
data class BatchedCrossDockProposalEngine(
    var steps: MutableList<BatchedCrossDockProposalFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: BatchedCrossDockProposal): Double {
        var current = item.netExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<BatchedCrossDockProposal>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultBatchedCrossDockProposalEngine(flags: Registry): BatchedCrossDockProposalEngine {
    return BatchedCrossDockProposalEngine(
        steps = mutableListOf<BatchedCrossDockProposalFactor>(
            FlaggedBatchedCrossDockProposalAdjuster(flags, "batched-cross-dock-proposal-lenient", -6.2),
            RoundingBatchedCrossDockProposalAdjuster(1.0),
            PercentBatchedCrossDockProposalAdjuster(24.7),
        ),
        trace = mutableListOf<String>(),
        floor = 0.7,
        ceiling = 70599.5,
    )
}
