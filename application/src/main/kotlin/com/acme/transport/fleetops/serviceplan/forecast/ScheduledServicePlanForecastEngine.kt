package com.acme.transport.fleetops.serviceplan.forecast

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the scheduled service plan forecast calculation. */
interface ScheduledServicePlanForecastModifier {
    fun describe(): String
    fun adjust(item: ScheduledServicePlanForecast, value: Double): Double
}

data class CapScheduledServicePlanForecastAdjuster(var cap: Double) : ScheduledServicePlanForecastModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ScheduledServicePlanForecast, value: Double): Double = minOf(value, cap)
}

data class CountryScheduledServicePlanForecastAdjuster(
    var factors: MutableMap<String, Double>,
) : ScheduledServicePlanForecastModifier {
    override fun describe(): String = "by country"

    override fun adjust(item: ScheduledServicePlanForecast, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedScheduledServicePlanForecastAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ScheduledServicePlanForecastModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ScheduledServicePlanForecast, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingScheduledServicePlanForecastAdjuster(
    var step: Double,
) : ScheduledServicePlanForecastModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ScheduledServicePlanForecast, value: Double): Double = roundTo(value, step)
}

data class ScheduledServicePlanForecastAttemptsOffsetAdjuster(
    var weight: Double,
) : ScheduledServicePlanForecastModifier {
    override fun describe(): String = "plus attempts times $weight"

    override fun adjust(item: ScheduledServicePlanForecast, value: Double): Double {
        return value + item.attempts.toDouble() * weight
    }
}

/** Runs the scheduled service plan forecast steps in order and keeps a trace of each. */
data class ScheduledServicePlanForecastEngine(
    var steps: MutableList<ScheduledServicePlanForecastModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: ScheduledServicePlanForecast): Double {
        var current = item.adjustedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ScheduledServicePlanForecast>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultScheduledServicePlanForecastEngine(flags: Registry): ScheduledServicePlanForecastEngine {
    return ScheduledServicePlanForecastEngine(
        steps = mutableListOf<ScheduledServicePlanForecastModifier>(
            CapScheduledServicePlanForecastAdjuster(47179.0),
            CountryScheduledServicePlanForecastAdjuster(
                mutableMapOf<String, Double>("online" to 1.37, "central" to 1.15, "north" to 0.99),
            ),
            FlaggedScheduledServicePlanForecastAdjuster(flags, "scheduled-service-plan-forecast-adaptive", 3.4),
            RoundingScheduledServicePlanForecastAdjuster(0.1),
            ScheduledServicePlanForecastAttemptsOffsetAdjuster(1.13),
        ),
        trace = mutableListOf<String>(),
        floor = 2.8,
        ceiling = 16212.7,
    )
}
