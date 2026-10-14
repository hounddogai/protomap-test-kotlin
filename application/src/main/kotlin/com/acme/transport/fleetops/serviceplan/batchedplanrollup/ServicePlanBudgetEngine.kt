package com.acme.transport.fleetops.serviceplan.batchedplanrollup

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the service plan budget calculation. */
interface ServicePlanBudgetStep {
    fun describe(): String
    fun adjust(item: ServicePlanBudget, value: Double): Double
}

data class MinimumServicePlanBudgetAdjuster(var minimum: Double) : ServicePlanBudgetStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ServicePlanBudget, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentServicePlanBudgetAdjuster(var percent: Double) : ServicePlanBudgetStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ServicePlanBudget, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedServicePlanBudgetAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ServicePlanBudgetStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ServicePlanBudget, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingServicePlanBudgetAdjuster(var step: Double) : ServicePlanBudgetStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ServicePlanBudget, value: Double): Double = roundTo(value, step)
}

/** Runs the service plan budget steps in order and keeps a trace of each. */
data class ServicePlanBudgetEngine(
    var steps: MutableList<ServicePlanBudgetStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: ServicePlanBudget): Double {
        var current = item.projectedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ServicePlanBudget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultServicePlanBudgetEngine(flags: Registry): ServicePlanBudgetEngine {
    return ServicePlanBudgetEngine(
        steps = mutableListOf<ServicePlanBudgetStep>(
            MinimumServicePlanBudgetAdjuster(43.5),
            PercentServicePlanBudgetAdjuster(-16.9),
            FlaggedServicePlanBudgetAdjuster(flags, "service-plan-budget-batched", 3.7),
            RoundingServicePlanBudgetAdjuster(1.0),
        ),
        trace = mutableListOf<String>(),
        floor = 4.1,
        ceiling = 22611.9,
    )
}
