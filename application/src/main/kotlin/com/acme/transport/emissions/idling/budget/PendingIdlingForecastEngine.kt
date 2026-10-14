package com.acme.transport.emissions.idling.budget

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the pending idling forecast calculation. */
interface PendingIdlingForecastAdjuster {
    fun describe(): String
    fun adjust(item: PendingIdlingForecast, value: Double): Double
}

data class PercentPendingIdlingForecastAdjuster(var percent: Double) : PendingIdlingForecastAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: PendingIdlingForecast, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumPendingIdlingForecastAdjuster(var minimum: Double) : PendingIdlingForecastAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PendingIdlingForecast, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class UnitPendingIdlingForecastAdjuster(
    var factors: MutableMap<String, Double>,
) : PendingIdlingForecastAdjuster {
    override fun describe(): String = "by unit"

    override fun adjust(item: PendingIdlingForecast, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedPendingIdlingForecastAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PendingIdlingForecastAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PendingIdlingForecast, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingPendingIdlingForecastAdjuster(var step: Double) : PendingIdlingForecastAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: PendingIdlingForecast, value: Double): Double = roundTo(value, step)
}

/** Runs the pending idling forecast steps in order and keeps a trace of each. */
data class PendingIdlingForecastEngine(
    var steps: MutableList<PendingIdlingForecastAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: PendingIdlingForecast): Double {
        var current = item.effectiveTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<PendingIdlingForecast>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultPendingIdlingForecastEngine(flags: Registry): PendingIdlingForecastEngine {
    return PendingIdlingForecastEngine(
        steps = mutableListOf<PendingIdlingForecastAdjuster>(
            PercentPendingIdlingForecastAdjuster(16.3),
            MinimumPendingIdlingForecastAdjuster(40.6),
            UnitPendingIdlingForecastAdjuster(mutableMapOf<String, Double>("central" to 1.34, "north" to 1.22)),
            FlaggedPendingIdlingForecastAdjuster(flags, "pending-idling-forecast-weighted", -9.0),
            RoundingPendingIdlingForecastAdjuster(0.5),
        ),
        trace = mutableListOf<String>(),
        floor = 4.7,
        ceiling = 39454.5,
    )
}
