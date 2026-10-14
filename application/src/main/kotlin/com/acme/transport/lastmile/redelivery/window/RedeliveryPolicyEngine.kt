package com.acme.transport.lastmile.redelivery.window

import com.acme.transport.kit.numbers.roundTo

/** One step of the redelivery policy calculation. */
interface RedeliveryPolicyModifier {
    fun describe(): String
    fun adjust(item: RedeliveryPolicy, value: Double): Double
}

data class RoundingRedeliveryPolicyAdjuster(var step: Double) : RedeliveryPolicyModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RedeliveryPolicy, value: Double): Double = roundTo(value, step)
}

data class CapRedeliveryPolicyAdjuster(var cap: Double) : RedeliveryPolicyModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RedeliveryPolicy, value: Double): Double = minOf(value, cap)
}

data class PercentRedeliveryPolicyAdjuster(var percent: Double) : RedeliveryPolicyModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RedeliveryPolicy, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredRedeliveryPolicyAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RedeliveryPolicyModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RedeliveryPolicy, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the redelivery policy steps in order and keeps a trace of each. */
data class RedeliveryPolicyEngine(
    var steps: MutableList<RedeliveryPolicyModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: RedeliveryPolicy): Double {
        var current = item.weightedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RedeliveryPolicyModifier = steps[index]
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

    fun evaluateAll(items: MutableList<RedeliveryPolicy>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultRedeliveryPolicyEngine(): RedeliveryPolicyEngine {
    return RedeliveryPolicyEngine(
        steps = mutableListOf<RedeliveryPolicyModifier>(
            RoundingRedeliveryPolicyAdjuster(0.1),
            CapRedeliveryPolicyAdjuster(40212.0),
            PercentRedeliveryPolicyAdjuster(15.6),
            TieredRedeliveryPolicyAdjuster(
                mutableListOf<Double>(2292.0, 2930.0),
                mutableListOf<Double>(0.95, 0.92),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.1,
        ceiling = 61928.6,
    )
}
