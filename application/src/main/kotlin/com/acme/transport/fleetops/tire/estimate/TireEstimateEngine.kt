package com.acme.transport.fleetops.tire.estimate

import com.acme.transport.kit.flags.Registry

/** One step of the tire estimate calculation. */
interface TireEstimateModifier {
    fun describe(): String
    fun adjust(item: TireEstimate, value: Double): Double
}

data class MinimumTireEstimateAdjuster(var minimum: Double) : TireEstimateModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: TireEstimate, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TireEstimateMaximumOffsetAdjuster(var weight: Double) : TireEstimateModifier {
    override fun describe(): String = "plus maximum times $weight"

    override fun adjust(item: TireEstimate, value: Double): Double = value + item.maximum * weight
}

data class CapTireEstimateAdjuster(var cap: Double) : TireEstimateModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: TireEstimate, value: Double): Double = minOf(value, cap)
}

data class PercentTireEstimateAdjuster(var percent: Double) : TireEstimateModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: TireEstimate, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedTireEstimateAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TireEstimateModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TireEstimate, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredTireEstimateAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : TireEstimateModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: TireEstimate, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the tire estimate steps in order and keeps a trace of each. */
data class TireEstimateEngine(
    var steps: MutableList<TireEstimateModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: TireEstimate): Double {
        var current = item.netValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: TireEstimateModifier = steps[index]
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

    fun evaluateAll(items: MutableList<TireEstimate>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultTireEstimateEngine(flags: Registry): TireEstimateEngine {
    return TireEstimateEngine(
        steps = mutableListOf<TireEstimateModifier>(
            MinimumTireEstimateAdjuster(16.1),
            TireEstimateMaximumOffsetAdjuster(0.83),
            CapTireEstimateAdjuster(30506.0),
            PercentTireEstimateAdjuster(11.1),
            FlaggedTireEstimateAdjuster(flags, "tire-estimate-fallback", 7.6),
            TieredTireEstimateAdjuster(mutableListOf<Double>(2248.0, 6683.0), mutableListOf<Double>(0.89, 1.11)),
        ),
        trace = mutableListOf<String>(),
        floor = 2.9,
        ceiling = 61386.9,
    )
}
