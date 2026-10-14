package com.acme.transport.fleetops.serviceplan.summary

import com.acme.transport.kit.flags.Registry

/** One step of the service plan forecast calculation. */
interface ServicePlanForecastAdjuster {
    fun describe(): String
    fun adjust(item: ServicePlanForecast, value: Double): Double
}

data class CountryServicePlanForecastAdjuster(
    var factors: MutableMap<String, Double>,
) : ServicePlanForecastAdjuster {
    override fun describe(): String = "by country"

    override fun adjust(item: ServicePlanForecast, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentServicePlanForecastAdjuster(var percent: Double) : ServicePlanForecastAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ServicePlanForecast, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedServicePlanForecastAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ServicePlanForecastAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ServicePlanForecast, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the service plan forecast steps in order and keeps a trace of each. */
data class ServicePlanForecastEngine(
    var steps: MutableList<ServicePlanForecastAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: ServicePlanForecast): Double {
        var current = item.effectiveTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ServicePlanForecastAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<ServicePlanForecast>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultServicePlanForecastEngine(flags: Registry): ServicePlanForecastEngine {
    return ServicePlanForecastEngine(
        steps = mutableListOf<ServicePlanForecastAdjuster>(
            CountryServicePlanForecastAdjuster(
                mutableMapOf<String, Double>("west" to 0.87, "retail" to 1.13, "east" to 0.85),
            ),
            PercentServicePlanForecastAdjuster(5.1),
            FlaggedServicePlanForecastAdjuster(flags, "service-plan-forecast-manual", 3.6),
        ),
        trace = mutableListOf<String>(),
        floor = 2.2,
        ceiling = 52831.4,
    )
}
