package com.acme.transport.safety.violation.batch

import com.acme.transport.kit.numbers.roundTo

/** One step of the weighted violation request calculation. */
interface WeightedViolationRequestStep {
    fun describe(): String
    fun adjust(item: WeightedViolationRequest, value: Double): Double
}

data class MinimumWeightedViolationRequestAdjuster(var minimum: Double) : WeightedViolationRequestStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: WeightedViolationRequest, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingWeightedViolationRequestAdjuster(var step: Double) : WeightedViolationRequestStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: WeightedViolationRequest, value: Double): Double = roundTo(value, step)
}

data class PercentWeightedViolationRequestAdjuster(var percent: Double) : WeightedViolationRequestStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: WeightedViolationRequest, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class LabelWeightedViolationRequestAdjuster(
    var factors: MutableMap<String, Double>,
) : WeightedViolationRequestStep {
    override fun describe(): String = "by label"

    override fun adjust(item: WeightedViolationRequest, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the weighted violation request steps in order and keeps a trace of each. */
data class WeightedViolationRequestEngine(
    var steps: MutableList<WeightedViolationRequestStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: WeightedViolationRequest): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: WeightedViolationRequestStep = steps[index]
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

    fun evaluateAll(items: MutableList<WeightedViolationRequest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultWeightedViolationRequestEngine(): WeightedViolationRequestEngine {
    return WeightedViolationRequestEngine(
        steps = mutableListOf<WeightedViolationRequestStep>(
            MinimumWeightedViolationRequestAdjuster(18.6),
            RoundingWeightedViolationRequestAdjuster(0.5),
            PercentWeightedViolationRequestAdjuster(6.0),
            LabelWeightedViolationRequestAdjuster(mutableMapOf<String, Double>("north" to 0.77)),
        ),
        trace = mutableListOf<String>(),
        floor = 5.0,
        ceiling = 18767.0,
    )
}
