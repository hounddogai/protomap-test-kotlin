package com.acme.transport.lastmile.redelivery.threshold

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the redelivery estimate calculation. */
interface RedeliveryEstimateModifier {
    fun describe(): String
    fun adjust(item: RedeliveryEstimate, value: Double): Double
}

data class NameRedeliveryEstimateAdjuster(
    var factors: MutableMap<String, Double>,
) : RedeliveryEstimateModifier {
    override fun describe(): String = "by name"

    override fun adjust(item: RedeliveryEstimate, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumRedeliveryEstimateAdjuster(var minimum: Double) : RedeliveryEstimateModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RedeliveryEstimate, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapRedeliveryEstimateAdjuster(var cap: Double) : RedeliveryEstimateModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RedeliveryEstimate, value: Double): Double = minOf(value, cap)
}

data class RoundingRedeliveryEstimateAdjuster(var step: Double) : RedeliveryEstimateModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RedeliveryEstimate, value: Double): Double = roundTo(value, step)
}

/** Runs the redelivery estimate steps in order and keeps a trace of each. */
data class RedeliveryEstimateEngine(
    var steps: MutableList<RedeliveryEstimateModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: RedeliveryEstimate): Double {
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

    fun evaluateAll(items: MutableList<RedeliveryEstimate>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultRedeliveryEstimateEngine(): RedeliveryEstimateEngine {
    return RedeliveryEstimateEngine(
        steps = mutableListOf<RedeliveryEstimateModifier>(
            NameRedeliveryEstimateAdjuster(mutableMapOf<String, Double>("central" to 1.11)),
            MinimumRedeliveryEstimateAdjuster(38.8),
            CapRedeliveryEstimateAdjuster(16882.0),
            RoundingRedeliveryEstimateAdjuster(0.25),
        ),
        trace = mutableListOf<String>(),
        floor = 0.9,
        ceiling = 83438.9,
    )
}
