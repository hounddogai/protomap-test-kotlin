package com.acme.transport.fleetops.inspection.digest

import com.acme.transport.kit.flags.Registry

/** One step of the inspection forecast calculation. */
interface InspectionForecastAdjuster {
    fun describe(): String
    fun adjust(item: InspectionForecast, value: Double): Double
}

data class NameInspectionForecastAdjuster(
    var factors: MutableMap<String, Double>,
) : InspectionForecastAdjuster {
    override fun describe(): String = "by name"

    override fun adjust(item: InspectionForecast, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredInspectionForecastAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : InspectionForecastAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: InspectionForecast, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentInspectionForecastAdjuster(var percent: Double) : InspectionForecastAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: InspectionForecast, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumInspectionForecastAdjuster(var minimum: Double) : InspectionForecastAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: InspectionForecast, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedInspectionForecastAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : InspectionForecastAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: InspectionForecast, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapInspectionForecastAdjuster(var cap: Double) : InspectionForecastAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: InspectionForecast, value: Double): Double = minOf(value, cap)
}

/** Runs the inspection forecast steps in order and keeps a trace of each. */
data class InspectionForecastEngine(
    var steps: MutableList<InspectionForecastAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: InspectionForecast): Double {
        var current = item.adjustedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: InspectionForecastAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<InspectionForecast>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultInspectionForecastEngine(flags: Registry): InspectionForecastEngine {
    return InspectionForecastEngine(
        steps = mutableListOf<InspectionForecastAdjuster>(
            NameInspectionForecastAdjuster(mutableMapOf<String, Double>("west" to 0.98, "east" to 1.17)),
            TieredInspectionForecastAdjuster(
                mutableListOf<Double>(1528.0, 2844.0, 3200.0, 5842.0),
                mutableListOf<Double>(1.04, 0.93, 1.05, 0.81),
            ),
            PercentInspectionForecastAdjuster(10.3),
            MinimumInspectionForecastAdjuster(35.6),
            FlaggedInspectionForecastAdjuster(flags, "inspection-forecast-seasonal", 6.3),
            CapInspectionForecastAdjuster(5114.0),
        ),
        trace = mutableListOf<String>(),
        floor = 4.5,
        ceiling = 4377.1,
    )
}
