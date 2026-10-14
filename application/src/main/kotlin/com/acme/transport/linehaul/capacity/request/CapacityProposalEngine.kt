package com.acme.transport.linehaul.capacity.request

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the capacity proposal calculation. */
interface CapacityProposalModifier {
    fun describe(): String
    fun adjust(item: CapacityProposal, value: Double): Double
}

data class CapacityProposalExpiresAtOffsetAdjuster(var weight: Double) : CapacityProposalModifier {
    override fun describe(): String = "plus expires at times $weight"

    override fun adjust(item: CapacityProposal, value: Double): Double = value + item.expiresAt.toDouble() * weight
}

data class FlaggedCapacityProposalAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CapacityProposalModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CapacityProposal, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingCapacityProposalAdjuster(var step: Double) : CapacityProposalModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CapacityProposal, value: Double): Double = roundTo(value, step)
}

/** Runs the capacity proposal steps in order and keeps a trace of each. */
data class CapacityProposalEngine(
    var steps: MutableList<CapacityProposalModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: CapacityProposal): Double {
        var current = item.projectedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: CapacityProposalModifier = steps[index]
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

    fun evaluateAll(items: MutableList<CapacityProposal>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultCapacityProposalEngine(flags: Registry): CapacityProposalEngine {
    return CapacityProposalEngine(
        steps = mutableListOf<CapacityProposalModifier>(
            CapacityProposalExpiresAtOffsetAdjuster(1.63),
            FlaggedCapacityProposalAdjuster(flags, "capacity-proposal-default", -0.1),
            RoundingCapacityProposalAdjuster(0.5),
        ),
        trace = mutableListOf<String>(),
        floor = 0.6,
        ceiling = 49523.1,
    )
}
