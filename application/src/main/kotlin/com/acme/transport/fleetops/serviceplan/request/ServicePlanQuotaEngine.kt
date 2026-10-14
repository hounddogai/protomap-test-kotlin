package com.acme.transport.fleetops.serviceplan.request

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the service plan quota calculation. */
interface ServicePlanQuotaStep {
    fun describe(): String
    fun adjust(item: ServicePlanQuota, value: Double): Double
}

data class PercentServicePlanQuotaAdjuster(var percent: Double) : ServicePlanQuotaStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ServicePlanQuota, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumServicePlanQuotaAdjuster(var minimum: Double) : ServicePlanQuotaStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ServicePlanQuota, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapServicePlanQuotaAdjuster(var cap: Double) : ServicePlanQuotaStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ServicePlanQuota, value: Double): Double = minOf(value, cap)
}

data class ServicePlanQuotaVersionOffsetAdjuster(var weight: Double) : ServicePlanQuotaStep {
    override fun describe(): String = "plus version times $weight"

    override fun adjust(item: ServicePlanQuota, value: Double): Double = value + item.version.toDouble() * weight
}

/** Runs the service plan quota steps in order and keeps a trace of each. */
data class ServicePlanQuotaEngine(
    var steps: MutableList<ServicePlanQuotaStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: ServicePlanQuota): Double {
        var current = item.weightedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ServicePlanQuota>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultServicePlanQuotaEngine(): ServicePlanQuotaEngine {
    return ServicePlanQuotaEngine(
        steps = mutableListOf<ServicePlanQuotaStep>(
            PercentServicePlanQuotaAdjuster(-22.8),
            MinimumServicePlanQuotaAdjuster(11.1),
            CapServicePlanQuotaAdjuster(7223.0),
            ServicePlanQuotaVersionOffsetAdjuster(1.49),
        ),
        trace = mutableListOf<String>(),
        floor = 1.7,
        ceiling = 54776.7,
    )
}
