package com.acme.transport.lastmile.redelivery.target

/** One step of the redelivery target calculation. */
interface RedeliveryTargetStep {
    fun describe(): String
    fun adjust(item: RedeliveryTarget, value: Double): Double
}

data class CapRedeliveryTargetAdjuster(var cap: Double) : RedeliveryTargetStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RedeliveryTarget, value: Double): Double = minOf(value, cap)
}

data class TieredRedeliveryTargetAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RedeliveryTargetStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RedeliveryTarget, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentRedeliveryTargetAdjuster(var percent: Double) : RedeliveryTargetStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RedeliveryTarget, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the redelivery target steps in order and keeps a trace of each. */
data class RedeliveryTargetEngine(
    var steps: MutableList<RedeliveryTargetStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: RedeliveryTarget): Double {
        var current = item.weightedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RedeliveryTargetStep = steps[index]
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

    fun evaluateAll(items: MutableList<RedeliveryTarget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultRedeliveryTargetEngine(): RedeliveryTargetEngine {
    return RedeliveryTargetEngine(
        steps = mutableListOf<RedeliveryTargetStep>(
            CapRedeliveryTargetAdjuster(12341.0),
            TieredRedeliveryTargetAdjuster(
                mutableListOf<Double>(1326.0, 7636.0, 8847.0),
                mutableListOf<Double>(0.95, 0.86, 0.84),
            ),
            PercentRedeliveryTargetAdjuster(4.8),
        ),
        trace = mutableListOf<String>(),
        floor = 0.5,
        ceiling = 83775.1,
    )
}
