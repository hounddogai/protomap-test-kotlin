package com.acme.transport.fleetops.utilization.rollup

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the scheduled utilization forecast calculation. */
interface ScheduledUtilizationForecastModifier {
    fun describe(): String
    fun adjust(item: ScheduledUtilizationForecast, value: Double): Double
}

data class RegionScheduledUtilizationForecastAdjuster(
    var factors: MutableMap<String, Double>,
) : ScheduledUtilizationForecastModifier {
    override fun describe(): String = "by region"

    override fun adjust(item: ScheduledUtilizationForecast, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumScheduledUtilizationForecastAdjuster(
    var minimum: Double,
) : ScheduledUtilizationForecastModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ScheduledUtilizationForecast, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredScheduledUtilizationForecastAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ScheduledUtilizationForecastModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ScheduledUtilizationForecast, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapScheduledUtilizationForecastAdjuster(var cap: Double) : ScheduledUtilizationForecastModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ScheduledUtilizationForecast, value: Double): Double = minOf(value, cap)
}

data class RoundingScheduledUtilizationForecastAdjuster(
    var step: Double,
) : ScheduledUtilizationForecastModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ScheduledUtilizationForecast, value: Double): Double = roundTo(value, step)
}

data class FlaggedScheduledUtilizationForecastAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ScheduledUtilizationForecastModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ScheduledUtilizationForecast, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the scheduled utilization forecast steps in order and keeps a trace of each. */
data class ScheduledUtilizationForecastEngine(
    var steps: MutableList<ScheduledUtilizationForecastModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: ScheduledUtilizationForecast): Double {
        var current = item.adjustedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ScheduledUtilizationForecastModifier = steps[index]
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

    fun evaluateAll(items: MutableList<ScheduledUtilizationForecast>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultScheduledUtilizationForecastEngine(flags: Registry): ScheduledUtilizationForecastEngine {
    return ScheduledUtilizationForecastEngine(
        steps = mutableListOf<ScheduledUtilizationForecastModifier>(
            RegionScheduledUtilizationForecastAdjuster(mutableMapOf<String, Double>("online" to 1.31)),
            MinimumScheduledUtilizationForecastAdjuster(26.6),
            TieredScheduledUtilizationForecastAdjuster(
                mutableListOf<Double>(3848.0, 5380.0),
                mutableListOf<Double>(1.0, 0.94),
            ),
            CapScheduledUtilizationForecastAdjuster(48207.0),
            RoundingScheduledUtilizationForecastAdjuster(0.05),
            FlaggedScheduledUtilizationForecastAdjuster(flags, "scheduled-utilization-forecast-pending", -2.2),
        ),
        trace = mutableListOf<String>(),
        floor = 2.6,
        ceiling = 36981.1,
    )
}
