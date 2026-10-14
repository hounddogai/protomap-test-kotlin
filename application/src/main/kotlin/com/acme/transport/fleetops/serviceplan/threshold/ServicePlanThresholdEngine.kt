package com.acme.transport.fleetops.serviceplan.threshold

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the service plan threshold calculation. */
interface ServicePlanThresholdModifier {
    fun describe(): String
    fun adjust(item: ServicePlanThreshold, value: Double): Double
}

data class FlaggedServicePlanThresholdAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ServicePlanThresholdModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ServicePlanThreshold, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapServicePlanThresholdAdjuster(var cap: Double) : ServicePlanThresholdModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ServicePlanThreshold, value: Double): Double = minOf(value, cap)
}

data class MinimumServicePlanThresholdAdjuster(var minimum: Double) : ServicePlanThresholdModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ServicePlanThreshold, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the service plan threshold steps in order and keeps a trace of each. */
data class ServicePlanThresholdEngine(
    var steps: MutableList<ServicePlanThresholdModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: ServicePlanThreshold): Double {
        var current = item.projectedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ServicePlanThreshold>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultServicePlanThresholdEngine(flags: Registry): ServicePlanThresholdEngine {
    return ServicePlanThresholdEngine(
        steps = mutableListOf<ServicePlanThresholdModifier>(
            FlaggedServicePlanThresholdAdjuster(flags, "service-plan-threshold-seasonal", 9.2),
            CapServicePlanThresholdAdjuster(45771.0),
            MinimumServicePlanThresholdAdjuster(30.6),
        ),
        trace = mutableListOf<String>(),
        floor = 0.8,
        ceiling = 69921.4,
    )
}
