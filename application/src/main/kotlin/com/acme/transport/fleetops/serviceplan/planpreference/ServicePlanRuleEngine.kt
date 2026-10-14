package com.acme.transport.fleetops.serviceplan.planpreference

import com.acme.transport.kit.numbers.roundTo

/** One step of the service plan rule calculation. */
interface ServicePlanRuleStep {
    fun describe(): String
    fun adjust(item: ServicePlanRule, value: Double): Double
}

data class LaneServicePlanRuleAdjuster(var factors: MutableMap<String, Double>) : ServicePlanRuleStep {
    override fun describe(): String = "by lane"

    override fun adjust(item: ServicePlanRule, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumServicePlanRuleAdjuster(var minimum: Double) : ServicePlanRuleStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ServicePlanRule, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingServicePlanRuleAdjuster(var step: Double) : ServicePlanRuleStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ServicePlanRule, value: Double): Double = roundTo(value, step)
}

/** Runs the service plan rule steps in order and keeps a trace of each. */
data class ServicePlanRuleEngine(
    var steps: MutableList<ServicePlanRuleStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: ServicePlanRule): Double {
        var current = item.projectedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ServicePlanRuleStep = steps[index]
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

    fun evaluateAll(items: MutableList<ServicePlanRule>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultServicePlanRuleEngine(): ServicePlanRuleEngine {
    return ServicePlanRuleEngine(
        steps = mutableListOf<ServicePlanRuleStep>(
            LaneServicePlanRuleAdjuster(
                mutableMapOf<String, Double>("online" to 1.07, "retail" to 1.35, "south" to 1.27),
            ),
            MinimumServicePlanRuleAdjuster(21.3),
            RoundingServicePlanRuleAdjuster(5.0),
        ),
        trace = mutableListOf<String>(),
        floor = 1.6,
        ceiling = 6639.5,
    )
}
