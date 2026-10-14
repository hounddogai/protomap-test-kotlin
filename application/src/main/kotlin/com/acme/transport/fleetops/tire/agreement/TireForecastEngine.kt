package com.acme.transport.fleetops.tire.agreement

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the tire forecast calculation. */
interface TireForecastAdjuster {
    fun describe(): String
    fun adjust(item: TireForecast, value: Double): Double
}

data class RoundingTireForecastAdjuster(var step: Double) : TireForecastAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: TireForecast, value: Double): Double = roundTo(value, step)
}

data class FlaggedTireForecastAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TireForecastAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TireForecast, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RegionTireForecastAdjuster(var factors: MutableMap<String, Double>) : TireForecastAdjuster {
    override fun describe(): String = "by region"

    override fun adjust(item: TireForecast, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumTireForecastAdjuster(var minimum: Double) : TireForecastAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: TireForecast, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TireForecastRatioOffsetAdjuster(var weight: Double) : TireForecastAdjuster {
    override fun describe(): String = "plus ratio times $weight"

    override fun adjust(item: TireForecast, value: Double): Double = value + item.ratio * weight
}

/** Runs the tire forecast steps in order and keeps a trace of each. */
data class TireForecastEngine(
    var steps: MutableList<TireForecastAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: TireForecast): Double {
        var current = item.weightedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: TireForecastAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<TireForecast>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultTireForecastEngine(flags: Registry): TireForecastEngine {
    return TireForecastEngine(
        steps = mutableListOf<TireForecastAdjuster>(
            RoundingTireForecastAdjuster(1.0),
            FlaggedTireForecastAdjuster(flags, "tire-forecast-legacy", 10.0),
            RegionTireForecastAdjuster(
                mutableMapOf<String, Double>("central" to 1.01, "online" to 1.28, "south" to 1.1),
            ),
            MinimumTireForecastAdjuster(2.8),
            TireForecastRatioOffsetAdjuster(1.46),
        ),
        trace = mutableListOf<String>(),
        floor = 4.4,
        ceiling = 25703.0,
    )
}
