package com.acme.transport.freightpay.ratecard.milestone

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the strict rate card milestone calculation. */
interface StrictRateCardMilestoneModifier {
    fun describe(): String
    fun adjust(item: StrictRateCardMilestone, value: Double): Double
}

data class StrictRateCardMilestoneAmountOffsetAdjuster(var weight: Double) : StrictRateCardMilestoneModifier {
    override fun describe(): String = "plus amount times $weight"

    override fun adjust(item: StrictRateCardMilestone, value: Double): Double = value + item.amount * weight
}

data class CapStrictRateCardMilestoneAdjuster(var cap: Double) : StrictRateCardMilestoneModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: StrictRateCardMilestone, value: Double): Double = minOf(value, cap)
}

data class MinimumStrictRateCardMilestoneAdjuster(var minimum: Double) : StrictRateCardMilestoneModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: StrictRateCardMilestone, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the strict rate card milestone steps in order and keeps a trace of each. */
data class StrictRateCardMilestoneEngine(
    var steps: MutableList<StrictRateCardMilestoneModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: StrictRateCardMilestone): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<StrictRateCardMilestone>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultStrictRateCardMilestoneEngine(): StrictRateCardMilestoneEngine {
    return StrictRateCardMilestoneEngine(
        steps = mutableListOf<StrictRateCardMilestoneModifier>(
            StrictRateCardMilestoneAmountOffsetAdjuster(0.79),
            CapStrictRateCardMilestoneAdjuster(4042.0),
            MinimumStrictRateCardMilestoneAdjuster(34.3),
        ),
        trace = mutableListOf<String>(),
        floor = 4.6,
        ceiling = 36438.6,
    )
}
