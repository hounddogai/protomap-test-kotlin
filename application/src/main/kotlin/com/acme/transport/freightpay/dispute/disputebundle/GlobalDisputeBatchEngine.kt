package com.acme.transport.freightpay.dispute.disputebundle

import com.acme.transport.kit.numbers.roundTo

/** One step of the global dispute batch calculation. */
interface GlobalDisputeBatchModifier {
    fun describe(): String
    fun adjust(item: GlobalDisputeBatch, value: Double): Double
}

data class PercentGlobalDisputeBatchAdjuster(var percent: Double) : GlobalDisputeBatchModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: GlobalDisputeBatch, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredGlobalDisputeBatchAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : GlobalDisputeBatchModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: GlobalDisputeBatch, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingGlobalDisputeBatchAdjuster(var step: Double) : GlobalDisputeBatchModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: GlobalDisputeBatch, value: Double): Double = roundTo(value, step)
}

data class CapGlobalDisputeBatchAdjuster(var cap: Double) : GlobalDisputeBatchModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: GlobalDisputeBatch, value: Double): Double = minOf(value, cap)
}

data class TitleGlobalDisputeBatchAdjuster(
    var factors: MutableMap<String, Double>,
) : GlobalDisputeBatchModifier {
    override fun describe(): String = "by title"

    override fun adjust(item: GlobalDisputeBatch, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumGlobalDisputeBatchAdjuster(var minimum: Double) : GlobalDisputeBatchModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: GlobalDisputeBatch, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the global dispute batch steps in order and keeps a trace of each. */
data class GlobalDisputeBatchEngine(
    var steps: MutableList<GlobalDisputeBatchModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: GlobalDisputeBatch): Double {
        var current = item.netValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: GlobalDisputeBatchModifier = steps[index]
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

    fun evaluateAll(items: MutableList<GlobalDisputeBatch>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultGlobalDisputeBatchEngine(): GlobalDisputeBatchEngine {
    return GlobalDisputeBatchEngine(
        steps = mutableListOf<GlobalDisputeBatchModifier>(
            PercentGlobalDisputeBatchAdjuster(20.0),
            TieredGlobalDisputeBatchAdjuster(
                mutableListOf<Double>(5945.0, 8065.0),
                mutableListOf<Double>(1.04, 1.18),
            ),
            RoundingGlobalDisputeBatchAdjuster(0.1),
            CapGlobalDisputeBatchAdjuster(14762.0),
            TitleGlobalDisputeBatchAdjuster(mutableMapOf<String, Double>("west" to 0.8, "central" to 1.15)),
            MinimumGlobalDisputeBatchAdjuster(26.6),
        ),
        trace = mutableListOf<String>(),
        floor = 3.2,
        ceiling = 40151.2,
    )
}
