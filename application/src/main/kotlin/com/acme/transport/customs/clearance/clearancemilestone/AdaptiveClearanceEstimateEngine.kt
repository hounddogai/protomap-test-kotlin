package com.acme.transport.customs.clearance.clearancemilestone

import com.acme.transport.kit.numbers.roundTo

/** One step of the adaptive clearance estimate calculation. */
interface AdaptiveClearanceEstimateStep {
    fun describe(): String
    fun adjust(item: AdaptiveClearanceEstimate, value: Double): Double
}

data class LaneAdaptiveClearanceEstimateAdjuster(
    var factors: MutableMap<String, Double>,
) : AdaptiveClearanceEstimateStep {
    override fun describe(): String = "by lane"

    override fun adjust(item: AdaptiveClearanceEstimate, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingAdaptiveClearanceEstimateAdjuster(var step: Double) : AdaptiveClearanceEstimateStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: AdaptiveClearanceEstimate, value: Double): Double = roundTo(value, step)
}

data class AdaptiveClearanceEstimateMarginOffsetAdjuster(var weight: Double) : AdaptiveClearanceEstimateStep {
    override fun describe(): String = "plus margin times $weight"

    override fun adjust(item: AdaptiveClearanceEstimate, value: Double): Double = value + item.margin * weight
}

/** Runs the adaptive clearance estimate steps in order and keeps a trace of each. */
data class AdaptiveClearanceEstimateEngine(
    var steps: MutableList<AdaptiveClearanceEstimateStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: AdaptiveClearanceEstimate): Double {
        var current = item.weightedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: AdaptiveClearanceEstimateStep = steps[index]
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

    fun evaluateAll(items: MutableList<AdaptiveClearanceEstimate>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultAdaptiveClearanceEstimateEngine(): AdaptiveClearanceEstimateEngine {
    return AdaptiveClearanceEstimateEngine(
        steps = mutableListOf<AdaptiveClearanceEstimateStep>(
            LaneAdaptiveClearanceEstimateAdjuster(
                mutableMapOf<String, Double>("south" to 0.99, "central" to 0.97, "north" to 0.88),
            ),
            RoundingAdaptiveClearanceEstimateAdjuster(0.25),
            AdaptiveClearanceEstimateMarginOffsetAdjuster(0.93),
        ),
        trace = mutableListOf<String>(),
        floor = 1.7,
        ceiling = 87934.3,
    )
}
