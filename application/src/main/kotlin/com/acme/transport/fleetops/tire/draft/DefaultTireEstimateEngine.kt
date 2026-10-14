package com.acme.transport.fleetops.tire.draft

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the default tire estimate calculation. */
interface DefaultTireEstimateAdjuster {
    fun describe(): String
    fun adjust(item: DefaultTireEstimate, value: Double): Double
}

data class MinimumDefaultTireEstimateAdjuster(var minimum: Double) : DefaultTireEstimateAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DefaultTireEstimate, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapDefaultTireEstimateAdjuster(var cap: Double) : DefaultTireEstimateAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DefaultTireEstimate, value: Double): Double = minOf(value, cap)
}

data class NameDefaultTireEstimateAdjuster(
    var factors: MutableMap<String, Double>,
) : DefaultTireEstimateAdjuster {
    override fun describe(): String = "by name"

    override fun adjust(item: DefaultTireEstimate, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingDefaultTireEstimateAdjuster(var step: Double) : DefaultTireEstimateAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DefaultTireEstimate, value: Double): Double = roundTo(value, step)
}

data class TieredDefaultTireEstimateAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DefaultTireEstimateAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DefaultTireEstimate, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedDefaultTireEstimateAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DefaultTireEstimateAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DefaultTireEstimate, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the default tire estimate steps in order and keeps a trace of each. */
data class DefaultTireEstimateEngine(
    var steps: MutableList<DefaultTireEstimateAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: DefaultTireEstimate): Double {
        var current = item.weightedVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<DefaultTireEstimate>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultDefaultTireEstimateEngine(flags: Registry): DefaultTireEstimateEngine {
    return DefaultTireEstimateEngine(
        steps = mutableListOf<DefaultTireEstimateAdjuster>(
            MinimumDefaultTireEstimateAdjuster(31.1),
            CapDefaultTireEstimateAdjuster(8811.0),
            NameDefaultTireEstimateAdjuster(mutableMapOf<String, Double>("west" to 1.09, "retail" to 0.74)),
            RoundingDefaultTireEstimateAdjuster(0.5),
            TieredDefaultTireEstimateAdjuster(
                mutableListOf<Double>(3048.0, 3939.0, 8108.0, 9385.0),
                mutableListOf<Double>(0.93, 1.15, 1.04, 1.07),
            ),
            FlaggedDefaultTireEstimateAdjuster(flags, "default-tire-estimate-legacy", -0.5),
        ),
        trace = mutableListOf<String>(),
        floor = 1.0,
        ceiling = 29821.6,
    )
}
