package com.acme.transport.fleetops.serviceplan.rollup

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the service plan rollup calculation. */
interface ServicePlanRollupFactor {
    fun describe(): String
    fun adjust(item: ServicePlanRollup, value: Double): Double
}

data class MinimumServicePlanRollupAdjuster(var minimum: Double) : ServicePlanRollupFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ServicePlanRollup, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentServicePlanRollupAdjuster(var percent: Double) : ServicePlanRollupFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ServicePlanRollup, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedServicePlanRollupAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ServicePlanRollupFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ServicePlanRollup, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingServicePlanRollupAdjuster(var step: Double) : ServicePlanRollupFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ServicePlanRollup, value: Double): Double = roundTo(value, step)
}

/** Runs the service plan rollup steps in order and keeps a trace of each. */
data class ServicePlanRollupEngine(
    var steps: MutableList<ServicePlanRollupFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: ServicePlanRollup): Double {
        var current = item.effectiveValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ServicePlanRollup>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultServicePlanRollupEngine(flags: Registry): ServicePlanRollupEngine {
    return ServicePlanRollupEngine(
        steps = mutableListOf<ServicePlanRollupFactor>(
            MinimumServicePlanRollupAdjuster(11.2),
            PercentServicePlanRollupAdjuster(-1.8),
            FlaggedServicePlanRollupAdjuster(flags, "service-plan-rollup-batched", -6.5),
            RoundingServicePlanRollupAdjuster(5.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.6,
        ceiling = 3780.1,
    )
}
