package com.acme.transport.network.node.adjustment

import com.acme.transport.kit.numbers.roundTo

/** One step of the fallback node adjustment calculation. */
interface FallbackNodeAdjustmentAdjuster {
    fun describe(): String
    fun adjust(item: FallbackNodeAdjustment, value: Double): Double
}

data class CapFallbackNodeAdjustmentAdjuster(var cap: Double) : FallbackNodeAdjustmentAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: FallbackNodeAdjustment, value: Double): Double = minOf(value, cap)
}

data class FallbackNodeAdjustmentCapacityOffsetAdjuster(var weight: Double) : FallbackNodeAdjustmentAdjuster {
    override fun describe(): String = "plus capacity times $weight"

    override fun adjust(item: FallbackNodeAdjustment, value: Double): Double {
        return value + item.capacity.toDouble() * weight
    }
}

data class RoundingFallbackNodeAdjustmentAdjuster(var step: Double) : FallbackNodeAdjustmentAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: FallbackNodeAdjustment, value: Double): Double = roundTo(value, step)
}

data class TieredFallbackNodeAdjustmentAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : FallbackNodeAdjustmentAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: FallbackNodeAdjustment, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumFallbackNodeAdjustmentAdjuster(var minimum: Double) : FallbackNodeAdjustmentAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: FallbackNodeAdjustment, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the fallback node adjustment steps in order and keeps a trace of each. */
data class FallbackNodeAdjustmentEngine(
    var steps: MutableList<FallbackNodeAdjustmentAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: FallbackNodeAdjustment): Double {
        var current = item.effectiveValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: FallbackNodeAdjustmentAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<FallbackNodeAdjustment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultFallbackNodeAdjustmentEngine(): FallbackNodeAdjustmentEngine {
    return FallbackNodeAdjustmentEngine(
        steps = mutableListOf<FallbackNodeAdjustmentAdjuster>(
            CapFallbackNodeAdjustmentAdjuster(4162.0),
            FallbackNodeAdjustmentCapacityOffsetAdjuster(1.66),
            RoundingFallbackNodeAdjustmentAdjuster(0.25),
            TieredFallbackNodeAdjustmentAdjuster(
                mutableListOf<Double>(2411.0, 5615.0, 7068.0, 8545.0),
                mutableListOf<Double>(1.07, 1.14, 0.92, 1.19),
            ),
            MinimumFallbackNodeAdjustmentAdjuster(10.5),
        ),
        trace = mutableListOf<String>(),
        floor = 2.2,
        ceiling = 97867.3,
    )
}
