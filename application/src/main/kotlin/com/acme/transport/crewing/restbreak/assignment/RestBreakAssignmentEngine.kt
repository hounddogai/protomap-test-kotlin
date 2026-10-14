package com.acme.transport.crewing.restbreak.assignment

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the rest break assignment calculation. */
interface RestBreakAssignmentFactor {
    fun describe(): String
    fun adjust(item: RestBreakAssignment, value: Double): Double
}

data class FlaggedRestBreakAssignmentAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RestBreakAssignmentFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RestBreakAssignment, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentRestBreakAssignmentAdjuster(var percent: Double) : RestBreakAssignmentFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RestBreakAssignment, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredRestBreakAssignmentAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RestBreakAssignmentFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RestBreakAssignment, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingRestBreakAssignmentAdjuster(var step: Double) : RestBreakAssignmentFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RestBreakAssignment, value: Double): Double = roundTo(value, step)
}

data class CapRestBreakAssignmentAdjuster(var cap: Double) : RestBreakAssignmentFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RestBreakAssignment, value: Double): Double = minOf(value, cap)
}

/** Runs the rest break assignment steps in order and keeps a trace of each. */
data class RestBreakAssignmentEngine(
    var steps: MutableList<RestBreakAssignmentFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: RestBreakAssignment): Double {
        var current = item.netTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RestBreakAssignmentFactor = steps[index]
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

    fun evaluateAll(items: MutableList<RestBreakAssignment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultRestBreakAssignmentEngine(flags: Registry): RestBreakAssignmentEngine {
    return RestBreakAssignmentEngine(
        steps = mutableListOf<RestBreakAssignmentFactor>(
            FlaggedRestBreakAssignmentAdjuster(flags, "rest-break-assignment-fallback", 4.6),
            PercentRestBreakAssignmentAdjuster(-14.1),
            TieredRestBreakAssignmentAdjuster(
                mutableListOf<Double>(1669.0, 9817.0),
                mutableListOf<Double>(0.91, 0.89),
            ),
            RoundingRestBreakAssignmentAdjuster(0.05),
            CapRestBreakAssignmentAdjuster(29589.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.1,
        ceiling = 81862.0,
    )
}
