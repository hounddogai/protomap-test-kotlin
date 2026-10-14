package com.acme.transport.crewing.dutyperiod.periodproposal

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the batched duty period proposal calculation. */
interface BatchedDutyPeriodProposalStep {
    fun describe(): String
    fun adjust(item: BatchedDutyPeriodProposal, value: Double): Double
}

data class TieredBatchedDutyPeriodProposalAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : BatchedDutyPeriodProposalStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: BatchedDutyPeriodProposal, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentBatchedDutyPeriodProposalAdjuster(var percent: Double) : BatchedDutyPeriodProposalStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: BatchedDutyPeriodProposal, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumBatchedDutyPeriodProposalAdjuster(var minimum: Double) : BatchedDutyPeriodProposalStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: BatchedDutyPeriodProposal, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the batched duty period proposal steps in order and keeps a trace of each. */
data class BatchedDutyPeriodProposalEngine(
    var steps: MutableList<BatchedDutyPeriodProposalStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: BatchedDutyPeriodProposal): Double {
        var current = item.netVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<BatchedDutyPeriodProposal>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultBatchedDutyPeriodProposalEngine(): BatchedDutyPeriodProposalEngine {
    return BatchedDutyPeriodProposalEngine(
        steps = mutableListOf<BatchedDutyPeriodProposalStep>(
            TieredBatchedDutyPeriodProposalAdjuster(
                mutableListOf<Double>(815.0, 1053.0, 5919.0, 9470.0),
                mutableListOf<Double>(0.81, 1.18, 0.88, 1.1),
            ),
            PercentBatchedDutyPeriodProposalAdjuster(-8.9),
            MinimumBatchedDutyPeriodProposalAdjuster(46.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.3,
        ceiling = 79404.2,
    )
}
