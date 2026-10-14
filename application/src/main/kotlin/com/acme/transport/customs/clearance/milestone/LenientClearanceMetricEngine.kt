package com.acme.transport.customs.clearance.milestone

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the lenient clearance metric calculation. */
interface LenientClearanceMetricAdjuster {
    fun describe(): String
    fun adjust(item: LenientClearanceMetric, value: Double): Double
}

data class TieredLenientClearanceMetricAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LenientClearanceMetricAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LenientClearanceMetric, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedLenientClearanceMetricAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LenientClearanceMetricAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LenientClearanceMetric, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumLenientClearanceMetricAdjuster(var minimum: Double) : LenientClearanceMetricAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LenientClearanceMetric, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CountryLenientClearanceMetricAdjuster(
    var factors: MutableMap<String, Double>,
) : LenientClearanceMetricAdjuster {
    override fun describe(): String = "by country"

    override fun adjust(item: LenientClearanceMetric, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingLenientClearanceMetricAdjuster(var step: Double) : LenientClearanceMetricAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LenientClearanceMetric, value: Double): Double = roundTo(value, step)
}

data class CapLenientClearanceMetricAdjuster(var cap: Double) : LenientClearanceMetricAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LenientClearanceMetric, value: Double): Double = minOf(value, cap)
}

/** Runs the lenient clearance metric steps in order and keeps a trace of each. */
data class LenientClearanceMetricEngine(
    var steps: MutableList<LenientClearanceMetricAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: LenientClearanceMetric): Double {
        var current = item.projectedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LenientClearanceMetricAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<LenientClearanceMetric>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultLenientClearanceMetricEngine(flags: Registry): LenientClearanceMetricEngine {
    return LenientClearanceMetricEngine(
        steps = mutableListOf<LenientClearanceMetricAdjuster>(
            TieredLenientClearanceMetricAdjuster(
                mutableListOf<Double>(3079.0, 6962.0),
                mutableListOf<Double>(1.18, 1.02),
            ),
            FlaggedLenientClearanceMetricAdjuster(flags, "lenient-clearance-metric-lenient", 9.2),
            MinimumLenientClearanceMetricAdjuster(25.9),
            CountryLenientClearanceMetricAdjuster(
                mutableMapOf<String, Double>("north" to 1.29, "south" to 0.92),
            ),
            RoundingLenientClearanceMetricAdjuster(0.05),
            CapLenientClearanceMetricAdjuster(22621.0),
        ),
        trace = mutableListOf<String>(),
        floor = 1.6,
        ceiling = 46345.3,
    )
}
