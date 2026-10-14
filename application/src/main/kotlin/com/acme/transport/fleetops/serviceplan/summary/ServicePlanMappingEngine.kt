package com.acme.transport.fleetops.serviceplan.summary

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the service plan mapping calculation. */
interface ServicePlanMappingStep {
    fun describe(): String
    fun adjust(item: ServicePlanMapping, value: Double): Double
}

data class PercentServicePlanMappingAdjuster(var percent: Double) : ServicePlanMappingStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ServicePlanMapping, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedServicePlanMappingAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ServicePlanMappingStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ServicePlanMapping, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingServicePlanMappingAdjuster(var step: Double) : ServicePlanMappingStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ServicePlanMapping, value: Double): Double = roundTo(value, step)
}

/** Runs the service plan mapping steps in order and keeps a trace of each. */
data class ServicePlanMappingEngine(
    var steps: MutableList<ServicePlanMappingStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: ServicePlanMapping): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ServicePlanMappingStep = steps[index]
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

    fun evaluateAll(items: MutableList<ServicePlanMapping>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultServicePlanMappingEngine(flags: Registry): ServicePlanMappingEngine {
    return ServicePlanMappingEngine(
        steps = mutableListOf<ServicePlanMappingStep>(
            PercentServicePlanMappingAdjuster(12.6),
            FlaggedServicePlanMappingAdjuster(flags, "service-plan-mapping-regional", -4.6),
            RoundingServicePlanMappingAdjuster(0.25),
        ),
        trace = mutableListOf<String>(),
        floor = 3.6,
        ceiling = 36790.0,
    )
}
