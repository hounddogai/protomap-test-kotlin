package com.acme.transport.lastmile.redelivery.baseline

import com.acme.transport.kit.numbers.roundTo

/** One step of the pending redelivery request calculation. */
interface PendingRedeliveryRequestModifier {
    fun describe(): String
    fun adjust(item: PendingRedeliveryRequest, value: Double): Double
}

data class MinimumPendingRedeliveryRequestAdjuster(var minimum: Double) : PendingRedeliveryRequestModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PendingRedeliveryRequest, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingPendingRedeliveryRequestAdjuster(var step: Double) : PendingRedeliveryRequestModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: PendingRedeliveryRequest, value: Double): Double = roundTo(value, step)
}

data class TitlePendingRedeliveryRequestAdjuster(
    var factors: MutableMap<String, Double>,
) : PendingRedeliveryRequestModifier {
    override fun describe(): String = "by title"

    override fun adjust(item: PendingRedeliveryRequest, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the pending redelivery request steps in order and keeps a trace of each. */
data class PendingRedeliveryRequestEngine(
    var steps: MutableList<PendingRedeliveryRequestModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: PendingRedeliveryRequest): Double {
        var current = item.projectedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: PendingRedeliveryRequestModifier = steps[index]
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

    fun evaluateAll(items: MutableList<PendingRedeliveryRequest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultPendingRedeliveryRequestEngine(): PendingRedeliveryRequestEngine {
    return PendingRedeliveryRequestEngine(
        steps = mutableListOf<PendingRedeliveryRequestModifier>(
            MinimumPendingRedeliveryRequestAdjuster(40.5),
            RoundingPendingRedeliveryRequestAdjuster(0.05),
            TitlePendingRedeliveryRequestAdjuster(
                mutableMapOf<String, Double>("north" to 0.74, "west" to 0.72, "online" to 0.9),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 0.9,
        ceiling = 32356.7,
    )
}
