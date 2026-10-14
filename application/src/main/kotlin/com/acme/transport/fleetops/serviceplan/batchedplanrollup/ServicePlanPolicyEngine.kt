package com.acme.transport.fleetops.serviceplan.batchedplanrollup

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the service plan policy calculation. */
interface ServicePlanPolicyStep {
    fun describe(): String
    fun adjust(item: ServicePlanPolicy, value: Double): Double
}

data class FlaggedServicePlanPolicyAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ServicePlanPolicyStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ServicePlanPolicy, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumServicePlanPolicyAdjuster(var minimum: Double) : ServicePlanPolicyStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ServicePlanPolicy, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentServicePlanPolicyAdjuster(var percent: Double) : ServicePlanPolicyStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ServicePlanPolicy, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the service plan policy steps in order and keeps a trace of each. */
data class ServicePlanPolicyEngine(
    var steps: MutableList<ServicePlanPolicyStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: ServicePlanPolicy): Double {
        var current = item.adjustedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ServicePlanPolicy>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultServicePlanPolicyEngine(flags: Registry): ServicePlanPolicyEngine {
    return ServicePlanPolicyEngine(
        steps = mutableListOf<ServicePlanPolicyStep>(
            FlaggedServicePlanPolicyAdjuster(flags, "service-plan-policy-scheduled", -8.1),
            MinimumServicePlanPolicyAdjuster(21.7),
            PercentServicePlanPolicyAdjuster(-8.7),
        ),
        trace = mutableListOf<String>(),
        floor = 0.1,
        ceiling = 4260.6,
    )
}
