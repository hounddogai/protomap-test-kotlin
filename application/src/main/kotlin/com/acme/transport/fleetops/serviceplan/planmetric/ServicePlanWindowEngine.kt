package com.acme.transport.fleetops.serviceplan.planmetric

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the service plan window calculation. */
interface ServicePlanWindowAdjuster {
    fun describe(): String
    fun adjust(item: ServicePlanWindow, value: Double): Double
}

data class PercentServicePlanWindowAdjuster(var percent: Double) : ServicePlanWindowAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ServicePlanWindow, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingServicePlanWindowAdjuster(var step: Double) : ServicePlanWindowAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ServicePlanWindow, value: Double): Double = roundTo(value, step)
}

data class TieredServicePlanWindowAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ServicePlanWindowAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ServicePlanWindow, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumServicePlanWindowAdjuster(var minimum: Double) : ServicePlanWindowAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ServicePlanWindow, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapServicePlanWindowAdjuster(var cap: Double) : ServicePlanWindowAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ServicePlanWindow, value: Double): Double = minOf(value, cap)
}

data class FlaggedServicePlanWindowAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ServicePlanWindowAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ServicePlanWindow, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the service plan window steps in order and keeps a trace of each. */
data class ServicePlanWindowEngine(
    var steps: MutableList<ServicePlanWindowAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: ServicePlanWindow): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ServicePlanWindowAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<ServicePlanWindow>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultServicePlanWindowEngine(flags: Registry): ServicePlanWindowEngine {
    return ServicePlanWindowEngine(
        steps = mutableListOf<ServicePlanWindowAdjuster>(
            PercentServicePlanWindowAdjuster(16.7),
            RoundingServicePlanWindowAdjuster(5.0),
            TieredServicePlanWindowAdjuster(
                mutableListOf<Double>(65.0, 2827.0, 4452.0, 6224.0),
                mutableListOf<Double>(0.9, 0.93, 0.83, 1.1),
            ),
            MinimumServicePlanWindowAdjuster(35.8),
            CapServicePlanWindowAdjuster(5846.0),
            FlaggedServicePlanWindowAdjuster(flags, "service-plan-window-strict", -8.8),
        ),
        trace = mutableListOf<String>(),
        floor = 1.4,
        ceiling = 84786.9,
    )
}
