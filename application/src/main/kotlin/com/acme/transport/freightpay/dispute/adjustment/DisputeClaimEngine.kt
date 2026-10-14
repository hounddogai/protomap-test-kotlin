package com.acme.transport.freightpay.dispute.adjustment

import com.acme.transport.kit.numbers.roundTo

/** One step of the dispute claim calculation. */
interface DisputeClaimFactor {
    fun describe(): String
    fun adjust(item: DisputeClaim, value: Double): Double
}

data class RoundingDisputeClaimAdjuster(var step: Double) : DisputeClaimFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DisputeClaim, value: Double): Double = roundTo(value, step)
}

data class MinimumDisputeClaimAdjuster(var minimum: Double) : DisputeClaimFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DisputeClaim, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentDisputeClaimAdjuster(var percent: Double) : DisputeClaimFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DisputeClaim, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the dispute claim steps in order and keeps a trace of each. */
data class DisputeClaimEngine(
    var steps: MutableList<DisputeClaimFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: DisputeClaim): Double {
        var current = item.adjustedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: DisputeClaimFactor = steps[index]
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

    fun evaluateAll(items: MutableList<DisputeClaim>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultDisputeClaimEngine(): DisputeClaimEngine {
    return DisputeClaimEngine(
        steps = mutableListOf<DisputeClaimFactor>(
            RoundingDisputeClaimAdjuster(0.25),
            MinimumDisputeClaimAdjuster(30.0),
            PercentDisputeClaimAdjuster(-11.5),
        ),
        trace = mutableListOf<String>(),
        floor = 4.3,
        ceiling = 29173.3,
    )
}
