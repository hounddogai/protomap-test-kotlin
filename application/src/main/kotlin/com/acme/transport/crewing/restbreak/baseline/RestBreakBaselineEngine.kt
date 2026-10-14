package com.acme.transport.crewing.restbreak.baseline

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the rest break baseline calculation. */
interface RestBreakBaselineFactor {
    fun describe(): String
    fun adjust(item: RestBreakBaseline, value: Double): Double
}

data class MinimumRestBreakBaselineAdjuster(var minimum: Double) : RestBreakBaselineFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RestBreakBaseline, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class DepotRestBreakBaselineAdjuster(var factors: MutableMap<String, Double>) : RestBreakBaselineFactor {
    override fun describe(): String = "by depot"

    override fun adjust(item: RestBreakBaseline, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedRestBreakBaselineAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RestBreakBaselineFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RestBreakBaseline, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RestBreakBaselineRatioOffsetAdjuster(var weight: Double) : RestBreakBaselineFactor {
    override fun describe(): String = "plus ratio times $weight"

    override fun adjust(item: RestBreakBaseline, value: Double): Double = value + item.ratio * weight
}

data class RoundingRestBreakBaselineAdjuster(var step: Double) : RestBreakBaselineFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RestBreakBaseline, value: Double): Double = roundTo(value, step)
}

/** Runs the rest break baseline steps in order and keeps a trace of each. */
data class RestBreakBaselineEngine(
    var steps: MutableList<RestBreakBaselineFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: RestBreakBaseline): Double {
        var current = item.effectiveVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RestBreakBaselineFactor = steps[index]
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

    fun evaluateAll(items: MutableList<RestBreakBaseline>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultRestBreakBaselineEngine(flags: Registry): RestBreakBaselineEngine {
    return RestBreakBaselineEngine(
        steps = mutableListOf<RestBreakBaselineFactor>(
            MinimumRestBreakBaselineAdjuster(9.8),
            DepotRestBreakBaselineAdjuster(mutableMapOf<String, Double>("west" to 1.04)),
            FlaggedRestBreakBaselineAdjuster(flags, "rest-break-baseline-capped", -6.3),
            RestBreakBaselineRatioOffsetAdjuster(0.14),
            RoundingRestBreakBaselineAdjuster(0.25),
        ),
        trace = mutableListOf<String>(),
        floor = 0.1,
        ceiling = 93688.5,
    )
}
