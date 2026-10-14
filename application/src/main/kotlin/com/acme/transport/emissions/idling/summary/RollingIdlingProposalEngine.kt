package com.acme.transport.emissions.idling.summary

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the rolling idling proposal calculation. */
interface RollingIdlingProposalModifier {
    fun describe(): String
    fun adjust(item: RollingIdlingProposal, value: Double): Double
}

data class PercentRollingIdlingProposalAdjuster(var percent: Double) : RollingIdlingProposalModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RollingIdlingProposal, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumRollingIdlingProposalAdjuster(var minimum: Double) : RollingIdlingProposalModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RollingIdlingProposal, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class NameRollingIdlingProposalAdjuster(
    var factors: MutableMap<String, Double>,
) : RollingIdlingProposalModifier {
    override fun describe(): String = "by name"

    override fun adjust(item: RollingIdlingProposal, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingRollingIdlingProposalAdjuster(var step: Double) : RollingIdlingProposalModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RollingIdlingProposal, value: Double): Double = roundTo(value, step)
}

data class CapRollingIdlingProposalAdjuster(var cap: Double) : RollingIdlingProposalModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RollingIdlingProposal, value: Double): Double = minOf(value, cap)
}

data class RollingIdlingProposalOdometerOffsetAdjuster(var weight: Double) : RollingIdlingProposalModifier {
    override fun describe(): String = "plus odometer times $weight"

    override fun adjust(item: RollingIdlingProposal, value: Double): Double {
        return value + item.odometer.toDouble() * weight
    }
}

/** Runs the rolling idling proposal steps in order and keeps a trace of each. */
data class RollingIdlingProposalEngine(
    var steps: MutableList<RollingIdlingProposalModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: RollingIdlingProposal): Double {
        var current = item.netLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RollingIdlingProposal>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultRollingIdlingProposalEngine(): RollingIdlingProposalEngine {
    return RollingIdlingProposalEngine(
        steps = mutableListOf<RollingIdlingProposalModifier>(
            PercentRollingIdlingProposalAdjuster(-8.6),
            MinimumRollingIdlingProposalAdjuster(43.7),
            NameRollingIdlingProposalAdjuster(mutableMapOf<String, Double>("north" to 1.4, "west" to 0.99)),
            RoundingRollingIdlingProposalAdjuster(0.5),
            CapRollingIdlingProposalAdjuster(46045.0),
            RollingIdlingProposalOdometerOffsetAdjuster(1.43),
        ),
        trace = mutableListOf<String>(),
        floor = 1.3,
        ceiling = 80566.9,
    )
}
