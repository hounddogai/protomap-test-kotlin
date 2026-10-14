package com.acme.transport.freightpay.dispute.slot

import com.acme.transport.kit.numbers.roundTo

/** One step of the fallback dispute plan calculation. */
interface FallbackDisputePlanStep {
    fun describe(): String
    fun adjust(item: FallbackDisputePlan, value: Double): Double
}

data class RoundingFallbackDisputePlanAdjuster(var step: Double) : FallbackDisputePlanStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: FallbackDisputePlan, value: Double): Double = roundTo(value, step)
}

data class PercentFallbackDisputePlanAdjuster(var percent: Double) : FallbackDisputePlanStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: FallbackDisputePlan, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumFallbackDisputePlanAdjuster(var minimum: Double) : FallbackDisputePlanStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: FallbackDisputePlan, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class LabelFallbackDisputePlanAdjuster(var factors: MutableMap<String, Double>) : FallbackDisputePlanStep {
    override fun describe(): String = "by label"

    override fun adjust(item: FallbackDisputePlan, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the fallback dispute plan steps in order and keeps a trace of each. */
data class FallbackDisputePlanEngine(
    var steps: MutableList<FallbackDisputePlanStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: FallbackDisputePlan): Double {
        var current = item.weightedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: FallbackDisputePlanStep = steps[index]
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

    fun evaluateAll(items: MutableList<FallbackDisputePlan>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultFallbackDisputePlanEngine(): FallbackDisputePlanEngine {
    return FallbackDisputePlanEngine(
        steps = mutableListOf<FallbackDisputePlanStep>(
            RoundingFallbackDisputePlanAdjuster(0.25),
            PercentFallbackDisputePlanAdjuster(16.8),
            MinimumFallbackDisputePlanAdjuster(7.3),
            LabelFallbackDisputePlanAdjuster(mutableMapOf<String, Double>("central" to 1.11, "retail" to 1.07)),
        ),
        trace = mutableListOf<String>(),
        floor = 0.5,
        ceiling = 75340.4,
    )
}
