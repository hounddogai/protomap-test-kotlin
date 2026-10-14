package com.acme.transport.crewing.restbreak.regionalbreakpreference

import com.acme.transport.kit.numbers.roundTo

/** One step of the fallback rest break assignment calculation. */
interface FallbackRestBreakAssignmentAdjuster {
    fun describe(): String
    fun adjust(item: FallbackRestBreakAssignment, value: Double): Double
}

data class NameFallbackRestBreakAssignmentAdjuster(
    var factors: MutableMap<String, Double>,
) : FallbackRestBreakAssignmentAdjuster {
    override fun describe(): String = "by name"

    override fun adjust(item: FallbackRestBreakAssignment, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapFallbackRestBreakAssignmentAdjuster(var cap: Double) : FallbackRestBreakAssignmentAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: FallbackRestBreakAssignment, value: Double): Double = minOf(value, cap)
}

data class RoundingFallbackRestBreakAssignmentAdjuster(var step: Double) : FallbackRestBreakAssignmentAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: FallbackRestBreakAssignment, value: Double): Double = roundTo(value, step)
}

data class PercentFallbackRestBreakAssignmentAdjuster(
    var percent: Double,
) : FallbackRestBreakAssignmentAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: FallbackRestBreakAssignment, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FallbackRestBreakAssignmentRateOffsetAdjuster(
    var weight: Double,
) : FallbackRestBreakAssignmentAdjuster {
    override fun describe(): String = "plus rate times $weight"

    override fun adjust(item: FallbackRestBreakAssignment, value: Double): Double = value + item.rate * weight
}

data class TieredFallbackRestBreakAssignmentAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : FallbackRestBreakAssignmentAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: FallbackRestBreakAssignment, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the fallback rest break assignment steps in order and keeps a trace of each. */
data class FallbackRestBreakAssignmentEngine(
    var steps: MutableList<FallbackRestBreakAssignmentAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: FallbackRestBreakAssignment): Double {
        var current = item.weightedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: FallbackRestBreakAssignmentAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<FallbackRestBreakAssignment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultFallbackRestBreakAssignmentEngine(): FallbackRestBreakAssignmentEngine {
    return FallbackRestBreakAssignmentEngine(
        steps = mutableListOf<FallbackRestBreakAssignmentAdjuster>(
            NameFallbackRestBreakAssignmentAdjuster(
                mutableMapOf<String, Double>("online" to 1.05, "west" to 1.19, "east" to 1.26),
            ),
            CapFallbackRestBreakAssignmentAdjuster(22200.0),
            RoundingFallbackRestBreakAssignmentAdjuster(0.5),
            PercentFallbackRestBreakAssignmentAdjuster(-13.2),
            FallbackRestBreakAssignmentRateOffsetAdjuster(0.35),
            TieredFallbackRestBreakAssignmentAdjuster(
                mutableListOf<Double>(138.0, 5389.0, 6754.0),
                mutableListOf<Double>(0.88, 1.09, 0.97),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.6,
        ceiling = 74736.8,
    )
}
