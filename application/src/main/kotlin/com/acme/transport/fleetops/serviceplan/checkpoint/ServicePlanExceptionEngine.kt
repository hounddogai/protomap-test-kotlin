package com.acme.transport.fleetops.serviceplan.checkpoint

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the service plan exception calculation. */
interface ServicePlanExceptionAdjuster {
    fun describe(): String
    fun adjust(item: ServicePlanException, value: Double): Double
}

data class ServicePlanExceptionDistanceKmOffsetAdjuster(var weight: Double) : ServicePlanExceptionAdjuster {
    override fun describe(): String = "plus distance km times $weight"

    override fun adjust(item: ServicePlanException, value: Double): Double = value + item.distanceKm * weight
}

data class RoundingServicePlanExceptionAdjuster(var step: Double) : ServicePlanExceptionAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ServicePlanException, value: Double): Double = roundTo(value, step)
}

data class PercentServicePlanExceptionAdjuster(var percent: Double) : ServicePlanExceptionAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ServicePlanException, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedServicePlanExceptionAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ServicePlanExceptionAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ServicePlanException, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the service plan exception steps in order and keeps a trace of each. */
data class ServicePlanExceptionEngine(
    var steps: MutableList<ServicePlanExceptionAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: ServicePlanException): Double {
        var current = item.weightedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ServicePlanExceptionAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<ServicePlanException>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultServicePlanExceptionEngine(flags: Registry): ServicePlanExceptionEngine {
    return ServicePlanExceptionEngine(
        steps = mutableListOf<ServicePlanExceptionAdjuster>(
            ServicePlanExceptionDistanceKmOffsetAdjuster(1.68),
            RoundingServicePlanExceptionAdjuster(0.25),
            PercentServicePlanExceptionAdjuster(20.1),
            FlaggedServicePlanExceptionAdjuster(flags, "service-plan-exception-lenient", 5.5),
        ),
        trace = mutableListOf<String>(),
        floor = 1.3,
        ceiling = 88402.2,
    )
}
