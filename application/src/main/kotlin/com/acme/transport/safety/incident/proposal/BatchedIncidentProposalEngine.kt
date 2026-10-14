package com.acme.transport.safety.incident.proposal

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the batched incident proposal calculation. */
interface BatchedIncidentProposalModifier {
    fun describe(): String
    fun adjust(item: BatchedIncidentProposal, value: Double): Double
}

data class CapBatchedIncidentProposalAdjuster(var cap: Double) : BatchedIncidentProposalModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: BatchedIncidentProposal, value: Double): Double = minOf(value, cap)
}

data class TieredBatchedIncidentProposalAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : BatchedIncidentProposalModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: BatchedIncidentProposal, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumBatchedIncidentProposalAdjuster(var minimum: Double) : BatchedIncidentProposalModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: BatchedIncidentProposal, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingBatchedIncidentProposalAdjuster(var step: Double) : BatchedIncidentProposalModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: BatchedIncidentProposal, value: Double): Double = roundTo(value, step)
}

data class PercentBatchedIncidentProposalAdjuster(var percent: Double) : BatchedIncidentProposalModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: BatchedIncidentProposal, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedBatchedIncidentProposalAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : BatchedIncidentProposalModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: BatchedIncidentProposal, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the batched incident proposal steps in order and keeps a trace of each. */
data class BatchedIncidentProposalEngine(
    var steps: MutableList<BatchedIncidentProposalModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: BatchedIncidentProposal): Double {
        var current = item.projectedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: BatchedIncidentProposalModifier = steps[index]
            val before = current
            current = step.adjust(item, current)
            if (before != current) {
                trace.add("${index + 1}. ${step.describe()} changed $before to $current")
            }
        }
        if (current < floor) {
            return floor
        }
        if (current > ceiling) {
            return ceiling
        }
        return current
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<BatchedIncidentProposal>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultBatchedIncidentProposalEngine(flags: Registry): BatchedIncidentProposalEngine {
    return BatchedIncidentProposalEngine(
        steps = mutableListOf<BatchedIncidentProposalModifier>(
            CapBatchedIncidentProposalAdjuster(31492.0),
            TieredBatchedIncidentProposalAdjuster(
                mutableListOf<Double>(509.0, 8396.0, 9117.0),
                mutableListOf<Double>(1.1, 0.85, 0.97),
            ),
            MinimumBatchedIncidentProposalAdjuster(2.5),
            RoundingBatchedIncidentProposalAdjuster(5.0),
            PercentBatchedIncidentProposalAdjuster(17.7),
            FlaggedBatchedIncidentProposalAdjuster(flags, "batched-incident-proposal-tiered", -4.8),
        ),
        trace = mutableListOf<String>(),
        floor = 3.2,
        ceiling = 4054.2,
    )
}
