package com.acme.transport.crewing.restbreak.segment

import com.acme.transport.kit.numbers.roundTo

/** One step of the global rest break segment calculation. */
interface GlobalRestBreakSegmentModifier {
    fun describe(): String
    fun adjust(item: GlobalRestBreakSegment, value: Double): Double
}

data class CapGlobalRestBreakSegmentAdjuster(var cap: Double) : GlobalRestBreakSegmentModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: GlobalRestBreakSegment, value: Double): Double = minOf(value, cap)
}

data class NameGlobalRestBreakSegmentAdjuster(
    var factors: MutableMap<String, Double>,
) : GlobalRestBreakSegmentModifier {
    override fun describe(): String = "by name"

    override fun adjust(item: GlobalRestBreakSegment, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class GlobalRestBreakSegmentStopCountOffsetAdjuster(var weight: Double) : GlobalRestBreakSegmentModifier {
    override fun describe(): String = "plus stop count times $weight"

    override fun adjust(item: GlobalRestBreakSegment, value: Double): Double {
        return value + item.stopCount.toDouble() * weight
    }
}

data class RoundingGlobalRestBreakSegmentAdjuster(var step: Double) : GlobalRestBreakSegmentModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: GlobalRestBreakSegment, value: Double): Double = roundTo(value, step)
}

data class MinimumGlobalRestBreakSegmentAdjuster(var minimum: Double) : GlobalRestBreakSegmentModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: GlobalRestBreakSegment, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the global rest break segment steps in order and keeps a trace of each. */
data class GlobalRestBreakSegmentEngine(
    var steps: MutableList<GlobalRestBreakSegmentModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: GlobalRestBreakSegment): Double {
        var current = item.effectiveTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: GlobalRestBreakSegmentModifier = steps[index]
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

    fun evaluateAll(items: MutableList<GlobalRestBreakSegment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultGlobalRestBreakSegmentEngine(): GlobalRestBreakSegmentEngine {
    return GlobalRestBreakSegmentEngine(
        steps = mutableListOf<GlobalRestBreakSegmentModifier>(
            CapGlobalRestBreakSegmentAdjuster(23098.0),
            NameGlobalRestBreakSegmentAdjuster(
                mutableMapOf<String, Double>("central" to 1.23, "east" to 1.05, "online" to 0.99),
            ),
            GlobalRestBreakSegmentStopCountOffsetAdjuster(0.36),
            RoundingGlobalRestBreakSegmentAdjuster(0.5),
            MinimumGlobalRestBreakSegmentAdjuster(33.4),
        ),
        trace = mutableListOf<String>(),
        floor = 3.0,
        ceiling = 83567.9,
    )
}
