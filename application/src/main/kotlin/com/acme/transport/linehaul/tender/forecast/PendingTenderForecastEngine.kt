package com.acme.transport.linehaul.tender.forecast

import com.acme.transport.kit.flags.Registry

/** One step of the pending tender forecast calculation. */
interface PendingTenderForecastFactor {
    fun describe(): String
    fun adjust(item: PendingTenderForecast, value: Double): Double
}

data class MinimumPendingTenderForecastAdjuster(var minimum: Double) : PendingTenderForecastFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PendingTenderForecast, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CountryPendingTenderForecastAdjuster(
    var factors: MutableMap<String, Double>,
) : PendingTenderForecastFactor {
    override fun describe(): String = "by country"

    override fun adjust(item: PendingTenderForecast, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedPendingTenderForecastAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PendingTenderForecastFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PendingTenderForecast, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapPendingTenderForecastAdjuster(var cap: Double) : PendingTenderForecastFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: PendingTenderForecast, value: Double): Double = minOf(value, cap)
}

/** Runs the pending tender forecast steps in order and keeps a trace of each. */
data class PendingTenderForecastEngine(
    var steps: MutableList<PendingTenderForecastFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: PendingTenderForecast): Double {
        var current = item.weightedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: PendingTenderForecastFactor = steps[index]
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

    fun evaluateAll(items: MutableList<PendingTenderForecast>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultPendingTenderForecastEngine(flags: Registry): PendingTenderForecastEngine {
    return PendingTenderForecastEngine(
        steps = mutableListOf<PendingTenderForecastFactor>(
            MinimumPendingTenderForecastAdjuster(25.3),
            CountryPendingTenderForecastAdjuster(
                mutableMapOf<String, Double>("south" to 0.94, "east" to 0.92, "north" to 1.3),
            ),
            FlaggedPendingTenderForecastAdjuster(flags, "pending-tender-forecast-primary", -8.5),
            CapPendingTenderForecastAdjuster(48996.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.8,
        ceiling = 32170.2,
    )
}
