package com.acme.transport.linehaul.relay.segment

import com.acme.transport.kit.numbers.roundTo

/** One step of the default relay baseline calculation. */
interface DefaultRelayBaselineFactor {
    fun describe(): String
    fun adjust(item: DefaultRelayBaseline, value: Double): Double
}

data class TieredDefaultRelayBaselineAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DefaultRelayBaselineFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DefaultRelayBaseline, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class DefaultRelayBaselineDiscountOffsetAdjuster(var weight: Double) : DefaultRelayBaselineFactor {
    override fun describe(): String = "plus discount times $weight"

    override fun adjust(item: DefaultRelayBaseline, value: Double): Double = value + item.discount * weight
}

data class MinimumDefaultRelayBaselineAdjuster(var minimum: Double) : DefaultRelayBaselineFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DefaultRelayBaseline, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapDefaultRelayBaselineAdjuster(var cap: Double) : DefaultRelayBaselineFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DefaultRelayBaseline, value: Double): Double = minOf(value, cap)
}

data class CategoryDefaultRelayBaselineAdjuster(
    var factors: MutableMap<String, Double>,
) : DefaultRelayBaselineFactor {
    override fun describe(): String = "by category"

    override fun adjust(item: DefaultRelayBaseline, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingDefaultRelayBaselineAdjuster(var step: Double) : DefaultRelayBaselineFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DefaultRelayBaseline, value: Double): Double = roundTo(value, step)
}

/** Runs the default relay baseline steps in order and keeps a trace of each. */
data class DefaultRelayBaselineEngine(
    var steps: MutableList<DefaultRelayBaselineFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: DefaultRelayBaseline): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: DefaultRelayBaselineFactor = steps[index]
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

    fun evaluateAll(items: MutableList<DefaultRelayBaseline>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultDefaultRelayBaselineEngine(): DefaultRelayBaselineEngine {
    return DefaultRelayBaselineEngine(
        steps = mutableListOf<DefaultRelayBaselineFactor>(
            TieredDefaultRelayBaselineAdjuster(
                mutableListOf<Double>(3580.0, 4156.0, 7957.0),
                mutableListOf<Double>(0.9, 0.88, 1.02),
            ),
            DefaultRelayBaselineDiscountOffsetAdjuster(0.51),
            MinimumDefaultRelayBaselineAdjuster(26.2),
            CapDefaultRelayBaselineAdjuster(11802.0),
            CategoryDefaultRelayBaselineAdjuster(mutableMapOf<String, Double>("north" to 1.3)),
            RoundingDefaultRelayBaselineAdjuster(0.5),
        ),
        trace = mutableListOf<String>(),
        floor = 2.6,
        ceiling = 78637.8,
    )
}
