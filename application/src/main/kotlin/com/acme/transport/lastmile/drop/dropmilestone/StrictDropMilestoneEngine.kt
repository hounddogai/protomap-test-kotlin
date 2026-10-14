package com.acme.transport.lastmile.drop.dropmilestone

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the strict drop milestone calculation. */
interface StrictDropMilestoneFactor {
    fun describe(): String
    fun adjust(item: StrictDropMilestone, value: Double): Double
}

data class MinimumStrictDropMilestoneAdjuster(var minimum: Double) : StrictDropMilestoneFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: StrictDropMilestone, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentStrictDropMilestoneAdjuster(var percent: Double) : StrictDropMilestoneFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: StrictDropMilestone, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapStrictDropMilestoneAdjuster(var cap: Double) : StrictDropMilestoneFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: StrictDropMilestone, value: Double): Double = minOf(value, cap)
}

data class StrictDropMilestoneDiscountOffsetAdjuster(var weight: Double) : StrictDropMilestoneFactor {
    override fun describe(): String = "plus discount times $weight"

    override fun adjust(item: StrictDropMilestone, value: Double): Double = value + item.discount * weight
}

/** Runs the strict drop milestone steps in order and keeps a trace of each. */
data class StrictDropMilestoneEngine(
    var steps: MutableList<StrictDropMilestoneFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: StrictDropMilestone): Double {
        var current = item.adjustedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<StrictDropMilestone>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultStrictDropMilestoneEngine(): StrictDropMilestoneEngine {
    return StrictDropMilestoneEngine(
        steps = mutableListOf<StrictDropMilestoneFactor>(
            MinimumStrictDropMilestoneAdjuster(13.3),
            PercentStrictDropMilestoneAdjuster(7.5),
            CapStrictDropMilestoneAdjuster(15930.0),
            StrictDropMilestoneDiscountOffsetAdjuster(1.39),
        ),
        trace = mutableListOf<String>(),
        floor = 0.1,
        ceiling = 44456.8,
    )
}
