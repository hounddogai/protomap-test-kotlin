package com.acme.transport.crewing.swap.claim

import com.acme.transport.kit.flags.Registry

/** One step of the swap target calculation. */
interface SwapTargetFactor {
    fun describe(): String
    fun adjust(item: SwapTarget, value: Double): Double
}

data class SwapTargetBudgetOffsetAdjuster(var weight: Double) : SwapTargetFactor {
    override fun describe(): String = "plus budget times $weight"

    override fun adjust(item: SwapTarget, value: Double): Double = value + item.budget * weight
}

data class CapSwapTargetAdjuster(var cap: Double) : SwapTargetFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: SwapTarget, value: Double): Double = minOf(value, cap)
}

data class FlaggedSwapTargetAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : SwapTargetFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: SwapTarget, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentSwapTargetAdjuster(var percent: Double) : SwapTargetFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: SwapTarget, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class NameSwapTargetAdjuster(var factors: MutableMap<String, Double>) : SwapTargetFactor {
    override fun describe(): String = "by name"

    override fun adjust(item: SwapTarget, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredSwapTargetAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : SwapTargetFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: SwapTarget, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the swap target steps in order and keeps a trace of each. */
data class SwapTargetEngine(
    var steps: MutableList<SwapTargetFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: SwapTarget): Double {
        var current = item.projectedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: SwapTargetFactor = steps[index]
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

    fun evaluateAll(items: MutableList<SwapTarget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultSwapTargetEngine(flags: Registry): SwapTargetEngine {
    return SwapTargetEngine(
        steps = mutableListOf<SwapTargetFactor>(
            SwapTargetBudgetOffsetAdjuster(1.31),
            CapSwapTargetAdjuster(10729.0),
            FlaggedSwapTargetAdjuster(flags, "swap-target-lenient", -6.4),
            PercentSwapTargetAdjuster(9.5),
            NameSwapTargetAdjuster(mutableMapOf<String, Double>("retail" to 0.96)),
            TieredSwapTargetAdjuster(
                mutableListOf<Double>(2352.0, 4207.0, 6263.0, 8597.0),
                mutableListOf<Double>(1.0, 1.17, 0.95, 1.1),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 4.8,
        ceiling = 48744.9,
    )
}
