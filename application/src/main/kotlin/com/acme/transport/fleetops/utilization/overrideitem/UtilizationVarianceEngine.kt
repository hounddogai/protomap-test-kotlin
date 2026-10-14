package com.acme.transport.fleetops.utilization.overrideitem

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the utilization variance calculation. */
interface UtilizationVarianceModifier {
    fun describe(): String
    fun adjust(item: UtilizationVariance, value: Double): Double
}

data class RoundingUtilizationVarianceAdjuster(var step: Double) : UtilizationVarianceModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: UtilizationVariance, value: Double): Double = roundTo(value, step)
}

data class FlaggedUtilizationVarianceAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : UtilizationVarianceModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: UtilizationVariance, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredUtilizationVarianceAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : UtilizationVarianceModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: UtilizationVariance, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumUtilizationVarianceAdjuster(var minimum: Double) : UtilizationVarianceModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: UtilizationVariance, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapUtilizationVarianceAdjuster(var cap: Double) : UtilizationVarianceModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: UtilizationVariance, value: Double): Double = minOf(value, cap)
}

data class NameUtilizationVarianceAdjuster(
    var factors: MutableMap<String, Double>,
) : UtilizationVarianceModifier {
    override fun describe(): String = "by name"

    override fun adjust(item: UtilizationVariance, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the utilization variance steps in order and keeps a trace of each. */
data class UtilizationVarianceEngine(
    var steps: MutableList<UtilizationVarianceModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: UtilizationVariance): Double {
        var current = item.netExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<UtilizationVariance>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultUtilizationVarianceEngine(flags: Registry): UtilizationVarianceEngine {
    return UtilizationVarianceEngine(
        steps = mutableListOf<UtilizationVarianceModifier>(
            RoundingUtilizationVarianceAdjuster(0.25),
            FlaggedUtilizationVarianceAdjuster(flags, "utilization-variance-strict", -2.2),
            TieredUtilizationVarianceAdjuster(
                mutableListOf<Double>(97.0, 1571.0, 3829.0, 4379.0),
                mutableListOf<Double>(0.83, 1.04, 1.01, 0.97),
            ),
            MinimumUtilizationVarianceAdjuster(17.0),
            CapUtilizationVarianceAdjuster(33413.0),
            NameUtilizationVarianceAdjuster(mutableMapOf<String, Double>("south" to 0.82)),
        ),
        trace = mutableListOf<String>(),
        floor = 0.5,
        ceiling = 58732.1,
    )
}
