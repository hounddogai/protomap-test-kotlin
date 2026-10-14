package com.acme.transport.network.hub.plan

import com.acme.transport.kit.numbers.roundTo

/** One step of the hub plan calculation. */
interface HubPlanStep {
    fun describe(): String
    fun adjust(item: HubPlan, value: Double): Double
}

data class RoundingHubPlanAdjuster(var step: Double) : HubPlanStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: HubPlan, value: Double): Double = roundTo(value, step)
}

data class NameHubPlanAdjuster(var factors: MutableMap<String, Double>) : HubPlanStep {
    override fun describe(): String = "by name"

    override fun adjust(item: HubPlan, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapHubPlanAdjuster(var cap: Double) : HubPlanStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: HubPlan, value: Double): Double = minOf(value, cap)
}

/** Runs the hub plan steps in order and keeps a trace of each. */
data class HubPlanEngine(
    var steps: MutableList<HubPlanStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: HubPlan): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: HubPlanStep = steps[index]
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

    fun evaluateAll(items: MutableList<HubPlan>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultHubPlanEngine(): HubPlanEngine {
    return HubPlanEngine(
        steps = mutableListOf<HubPlanStep>(
            RoundingHubPlanAdjuster(1.0),
            NameHubPlanAdjuster(
                mutableMapOf<String, Double>("retail" to 0.74, "online" to 0.77, "east" to 0.94),
            ),
            CapHubPlanAdjuster(49396.0),
        ),
        trace = mutableListOf<String>(),
        floor = 4.8,
        ceiling = 29874.4,
    )
}
