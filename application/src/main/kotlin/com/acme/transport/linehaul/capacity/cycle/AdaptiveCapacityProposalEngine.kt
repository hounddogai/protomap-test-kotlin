package com.acme.transport.linehaul.capacity.cycle

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the adaptive capacity proposal calculation. */
interface AdaptiveCapacityProposalAdjuster {
    fun describe(): String
    fun adjust(item: AdaptiveCapacityProposal, value: Double): Double
}

data class AdaptiveCapacityProposalAmountOffsetAdjuster(var weight: Double) : AdaptiveCapacityProposalAdjuster {
    override fun describe(): String = "plus amount times $weight"

    override fun adjust(item: AdaptiveCapacityProposal, value: Double): Double = value + item.amount * weight
}

data class RegionAdaptiveCapacityProposalAdjuster(
    var factors: MutableMap<String, Double>,
) : AdaptiveCapacityProposalAdjuster {
    override fun describe(): String = "by region"

    override fun adjust(item: AdaptiveCapacityProposal, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingAdaptiveCapacityProposalAdjuster(var step: Double) : AdaptiveCapacityProposalAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: AdaptiveCapacityProposal, value: Double): Double = roundTo(value, step)
}

data class CapAdaptiveCapacityProposalAdjuster(var cap: Double) : AdaptiveCapacityProposalAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AdaptiveCapacityProposal, value: Double): Double = minOf(value, cap)
}

data class FlaggedAdaptiveCapacityProposalAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : AdaptiveCapacityProposalAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: AdaptiveCapacityProposal, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumAdaptiveCapacityProposalAdjuster(var minimum: Double) : AdaptiveCapacityProposalAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AdaptiveCapacityProposal, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the adaptive capacity proposal steps in order and keeps a trace of each. */
data class AdaptiveCapacityProposalEngine(
    var steps: MutableList<AdaptiveCapacityProposalAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: AdaptiveCapacityProposal): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<AdaptiveCapacityProposal>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultAdaptiveCapacityProposalEngine(flags: Registry): AdaptiveCapacityProposalEngine {
    return AdaptiveCapacityProposalEngine(
        steps = mutableListOf<AdaptiveCapacityProposalAdjuster>(
            AdaptiveCapacityProposalAmountOffsetAdjuster(1.63),
            RegionAdaptiveCapacityProposalAdjuster(mutableMapOf<String, Double>("east" to 1.28, "west" to 1.13)),
            RoundingAdaptiveCapacityProposalAdjuster(0.1),
            CapAdaptiveCapacityProposalAdjuster(10810.0),
            FlaggedAdaptiveCapacityProposalAdjuster(flags, "adaptive-capacity-proposal-global", -9.3),
            MinimumAdaptiveCapacityProposalAdjuster(16.3),
        ),
        trace = mutableListOf<String>(),
        floor = 4.8,
        ceiling = 68533.2,
    )
}
