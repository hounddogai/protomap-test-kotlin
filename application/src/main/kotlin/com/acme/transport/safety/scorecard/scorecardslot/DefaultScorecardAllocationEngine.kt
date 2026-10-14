package com.acme.transport.safety.scorecard.scorecardslot

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the default scorecard allocation calculation. */
interface DefaultScorecardAllocationAdjuster {
    fun describe(): String
    fun adjust(item: DefaultScorecardAllocation, value: Double): Double
}

data class DepotDefaultScorecardAllocationAdjuster(
    var factors: MutableMap<String, Double>,
) : DefaultScorecardAllocationAdjuster {
    override fun describe(): String = "by depot"

    override fun adjust(item: DefaultScorecardAllocation, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapDefaultScorecardAllocationAdjuster(var cap: Double) : DefaultScorecardAllocationAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DefaultScorecardAllocation, value: Double): Double = minOf(value, cap)
}

data class RoundingDefaultScorecardAllocationAdjuster(var step: Double) : DefaultScorecardAllocationAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DefaultScorecardAllocation, value: Double): Double = roundTo(value, step)
}

data class DefaultScorecardAllocationDistanceKmOffsetAdjuster(
    var weight: Double,
) : DefaultScorecardAllocationAdjuster {
    override fun describe(): String = "plus distance km times $weight"

    override fun adjust(item: DefaultScorecardAllocation, value: Double): Double = value + item.distanceKm * weight
}

data class FlaggedDefaultScorecardAllocationAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DefaultScorecardAllocationAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DefaultScorecardAllocation, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the default scorecard allocation steps in order and keeps a trace of each. */
data class DefaultScorecardAllocationEngine(
    var steps: MutableList<DefaultScorecardAllocationAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: DefaultScorecardAllocation): Double {
        var current = item.netVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: DefaultScorecardAllocationAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<DefaultScorecardAllocation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultDefaultScorecardAllocationEngine(flags: Registry): DefaultScorecardAllocationEngine {
    return DefaultScorecardAllocationEngine(
        steps = mutableListOf<DefaultScorecardAllocationAdjuster>(
            DepotDefaultScorecardAllocationAdjuster(mutableMapOf<String, Double>("east" to 0.91)),
            CapDefaultScorecardAllocationAdjuster(31115.0),
            RoundingDefaultScorecardAllocationAdjuster(1.0),
            DefaultScorecardAllocationDistanceKmOffsetAdjuster(1.27),
            FlaggedDefaultScorecardAllocationAdjuster(flags, "default-scorecard-allocation-rolling", 5.4),
        ),
        trace = mutableListOf<String>(),
        floor = 2.5,
        ceiling = 19716.1,
    )
}
