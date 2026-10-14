package com.acme.transport.lastmile.drop.rollup

import com.acme.transport.kit.numbers.roundTo

/** One step of the regional drop target calculation. */
interface RegionalDropTargetAdjuster {
    fun describe(): String
    fun adjust(item: RegionalDropTarget, value: Double): Double
}

data class RoundingRegionalDropTargetAdjuster(var step: Double) : RegionalDropTargetAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RegionalDropTarget, value: Double): Double = roundTo(value, step)
}

data class MinimumRegionalDropTargetAdjuster(var minimum: Double) : RegionalDropTargetAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RegionalDropTarget, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RegionalDropTargetMinimumOffsetAdjuster(var weight: Double) : RegionalDropTargetAdjuster {
    override fun describe(): String = "plus minimum times $weight"

    override fun adjust(item: RegionalDropTarget, value: Double): Double = value + item.minimum * weight
}

data class PercentRegionalDropTargetAdjuster(var percent: Double) : RegionalDropTargetAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RegionalDropTarget, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredRegionalDropTargetAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RegionalDropTargetAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RegionalDropTarget, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the regional drop target steps in order and keeps a trace of each. */
data class RegionalDropTargetEngine(
    var steps: MutableList<RegionalDropTargetAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: RegionalDropTarget): Double {
        var current = item.projectedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RegionalDropTargetAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<RegionalDropTarget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultRegionalDropTargetEngine(): RegionalDropTargetEngine {
    return RegionalDropTargetEngine(
        steps = mutableListOf<RegionalDropTargetAdjuster>(
            RoundingRegionalDropTargetAdjuster(0.05),
            MinimumRegionalDropTargetAdjuster(33.0),
            RegionalDropTargetMinimumOffsetAdjuster(0.33),
            PercentRegionalDropTargetAdjuster(0.8),
            TieredRegionalDropTargetAdjuster(
                mutableListOf<Double>(683.0, 2467.0, 3268.0, 4643.0),
                mutableListOf<Double>(1.06, 0.91, 1.14, 1.12),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 2.6,
        ceiling = 12302.0,
    )
}
