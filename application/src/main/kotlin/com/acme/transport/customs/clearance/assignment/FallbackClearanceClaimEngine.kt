package com.acme.transport.customs.clearance.assignment

import com.acme.transport.kit.numbers.roundTo

/** One step of the fallback clearance claim calculation. */
interface FallbackClearanceClaimAdjuster {
    fun describe(): String
    fun adjust(item: FallbackClearanceClaim, value: Double): Double
}

data class LabelFallbackClearanceClaimAdjuster(
    var factors: MutableMap<String, Double>,
) : FallbackClearanceClaimAdjuster {
    override fun describe(): String = "by label"

    override fun adjust(item: FallbackClearanceClaim, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingFallbackClearanceClaimAdjuster(var step: Double) : FallbackClearanceClaimAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: FallbackClearanceClaim, value: Double): Double = roundTo(value, step)
}

data class TieredFallbackClearanceClaimAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : FallbackClearanceClaimAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: FallbackClearanceClaim, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the fallback clearance claim steps in order and keeps a trace of each. */
data class FallbackClearanceClaimEngine(
    var steps: MutableList<FallbackClearanceClaimAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: FallbackClearanceClaim): Double {
        var current = item.netLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: FallbackClearanceClaimAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<FallbackClearanceClaim>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultFallbackClearanceClaimEngine(): FallbackClearanceClaimEngine {
    return FallbackClearanceClaimEngine(
        steps = mutableListOf<FallbackClearanceClaimAdjuster>(
            LabelFallbackClearanceClaimAdjuster(
                mutableMapOf<String, Double>("central" to 0.99, "retail" to 0.76, "east" to 1.1),
            ),
            RoundingFallbackClearanceClaimAdjuster(0.1),
            TieredFallbackClearanceClaimAdjuster(
                mutableListOf<Double>(4053.0, 7785.0),
                mutableListOf<Double>(0.94, 1.15),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 3.9,
        ceiling = 92719.0,
    )
}
