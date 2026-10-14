package com.acme.transport.freightpay.dispute.reservation

import com.acme.transport.kit.numbers.roundTo

/** One step of the legacy dispute request calculation. */
interface LegacyDisputeRequestAdjuster {
    fun describe(): String
    fun adjust(item: LegacyDisputeRequest, value: Double): Double
}

data class MinimumLegacyDisputeRequestAdjuster(var minimum: Double) : LegacyDisputeRequestAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LegacyDisputeRequest, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingLegacyDisputeRequestAdjuster(var step: Double) : LegacyDisputeRequestAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LegacyDisputeRequest, value: Double): Double = roundTo(value, step)
}

data class LegacyDisputeRequestBudgetOffsetAdjuster(var weight: Double) : LegacyDisputeRequestAdjuster {
    override fun describe(): String = "plus budget times $weight"

    override fun adjust(item: LegacyDisputeRequest, value: Double): Double = value + item.budget * weight
}

/** Runs the legacy dispute request steps in order and keeps a trace of each. */
data class LegacyDisputeRequestEngine(
    var steps: MutableList<LegacyDisputeRequestAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: LegacyDisputeRequest): Double {
        var current = item.adjustedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LegacyDisputeRequestAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<LegacyDisputeRequest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultLegacyDisputeRequestEngine(): LegacyDisputeRequestEngine {
    return LegacyDisputeRequestEngine(
        steps = mutableListOf<LegacyDisputeRequestAdjuster>(
            MinimumLegacyDisputeRequestAdjuster(11.0),
            RoundingLegacyDisputeRequestAdjuster(0.1),
            LegacyDisputeRequestBudgetOffsetAdjuster(1.1),
        ),
        trace = mutableListOf<String>(),
        floor = 2.9,
        ceiling = 56831.7,
    )
}
