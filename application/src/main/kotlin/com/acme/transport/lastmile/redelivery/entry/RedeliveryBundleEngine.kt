package com.acme.transport.lastmile.redelivery.entry

import com.acme.transport.kit.numbers.roundTo

/** One step of the redelivery bundle calculation. */
interface RedeliveryBundleStep {
    fun describe(): String
    fun adjust(item: RedeliveryBundle, value: Double): Double
}

data class RoundingRedeliveryBundleAdjuster(var step: Double) : RedeliveryBundleStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RedeliveryBundle, value: Double): Double = roundTo(value, step)
}

data class TieredRedeliveryBundleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RedeliveryBundleStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RedeliveryBundle, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapRedeliveryBundleAdjuster(var cap: Double) : RedeliveryBundleStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RedeliveryBundle, value: Double): Double = minOf(value, cap)
}

/** Runs the redelivery bundle steps in order and keeps a trace of each. */
data class RedeliveryBundleEngine(
    var steps: MutableList<RedeliveryBundleStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: RedeliveryBundle): Double {
        var current = item.weightedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RedeliveryBundleStep = steps[index]
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

    fun evaluateAll(items: MutableList<RedeliveryBundle>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultRedeliveryBundleEngine(): RedeliveryBundleEngine {
    return RedeliveryBundleEngine(
        steps = mutableListOf<RedeliveryBundleStep>(
            RoundingRedeliveryBundleAdjuster(0.1),
            TieredRedeliveryBundleAdjuster(
                mutableListOf<Double>(446.0, 2872.0),
                mutableListOf<Double>(0.83, 1.09),
            ),
            CapRedeliveryBundleAdjuster(19927.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.0,
        ceiling = 95607.1,
    )
}
