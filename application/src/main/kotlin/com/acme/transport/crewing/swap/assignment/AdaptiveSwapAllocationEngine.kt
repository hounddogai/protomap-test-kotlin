package com.acme.transport.crewing.swap.assignment

import com.acme.transport.kit.flags.Registry

/** One step of the adaptive swap allocation calculation. */
interface AdaptiveSwapAllocationStep {
    fun describe(): String
    fun adjust(item: AdaptiveSwapAllocation, value: Double): Double
}

data class PercentAdaptiveSwapAllocationAdjuster(var percent: Double) : AdaptiveSwapAllocationStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: AdaptiveSwapAllocation, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedAdaptiveSwapAllocationAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : AdaptiveSwapAllocationStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: AdaptiveSwapAllocation, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredAdaptiveSwapAllocationAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : AdaptiveSwapAllocationStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: AdaptiveSwapAllocation, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumAdaptiveSwapAllocationAdjuster(var minimum: Double) : AdaptiveSwapAllocationStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AdaptiveSwapAllocation, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class AdaptiveSwapAllocationQuantityOffsetAdjuster(var weight: Double) : AdaptiveSwapAllocationStep {
    override fun describe(): String = "plus quantity times $weight"

    override fun adjust(item: AdaptiveSwapAllocation, value: Double): Double {
        return value + item.quantity.toDouble() * weight
    }
}

/** Runs the adaptive swap allocation steps in order and keeps a trace of each. */
data class AdaptiveSwapAllocationEngine(
    var steps: MutableList<AdaptiveSwapAllocationStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: AdaptiveSwapAllocation): Double {
        var current = item.effectiveTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: AdaptiveSwapAllocationStep = steps[index]
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

    fun evaluateAll(items: MutableList<AdaptiveSwapAllocation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultAdaptiveSwapAllocationEngine(flags: Registry): AdaptiveSwapAllocationEngine {
    return AdaptiveSwapAllocationEngine(
        steps = mutableListOf<AdaptiveSwapAllocationStep>(
            PercentAdaptiveSwapAllocationAdjuster(-23.3),
            FlaggedAdaptiveSwapAllocationAdjuster(flags, "adaptive-swap-allocation-capped", 9.2),
            TieredAdaptiveSwapAllocationAdjuster(
                mutableListOf<Double>(6949.0, 7014.0, 7200.0, 9483.0),
                mutableListOf<Double>(1.07, 0.84, 1.05, 0.81),
            ),
            MinimumAdaptiveSwapAllocationAdjuster(24.3),
            AdaptiveSwapAllocationQuantityOffsetAdjuster(0.36),
        ),
        trace = mutableListOf<String>(),
        floor = 4.5,
        ceiling = 2856.2,
    )
}
