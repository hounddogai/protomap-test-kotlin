package com.acme.transport.crewing.swap.swaprecord

import com.acme.transport.kit.flags.Registry

/** One step of the fallback swap group calculation. */
interface FallbackSwapGroupModifier {
    fun describe(): String
    fun adjust(item: FallbackSwapGroup, value: Double): Double
}

data class LabelFallbackSwapGroupAdjuster(var factors: MutableMap<String, Double>) : FallbackSwapGroupModifier {
    override fun describe(): String = "by label"

    override fun adjust(item: FallbackSwapGroup, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredFallbackSwapGroupAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : FallbackSwapGroupModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: FallbackSwapGroup, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FallbackSwapGroupEndsAtOffsetAdjuster(var weight: Double) : FallbackSwapGroupModifier {
    override fun describe(): String = "plus ends at times $weight"

    override fun adjust(item: FallbackSwapGroup, value: Double): Double = value + item.endsAt.toDouble() * weight
}

data class PercentFallbackSwapGroupAdjuster(var percent: Double) : FallbackSwapGroupModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: FallbackSwapGroup, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedFallbackSwapGroupAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : FallbackSwapGroupModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: FallbackSwapGroup, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapFallbackSwapGroupAdjuster(var cap: Double) : FallbackSwapGroupModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: FallbackSwapGroup, value: Double): Double = minOf(value, cap)
}

/** Runs the fallback swap group steps in order and keeps a trace of each. */
data class FallbackSwapGroupEngine(
    var steps: MutableList<FallbackSwapGroupModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: FallbackSwapGroup): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: FallbackSwapGroupModifier = steps[index]
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

    fun evaluateAll(items: MutableList<FallbackSwapGroup>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultFallbackSwapGroupEngine(flags: Registry): FallbackSwapGroupEngine {
    return FallbackSwapGroupEngine(
        steps = mutableListOf<FallbackSwapGroupModifier>(
            LabelFallbackSwapGroupAdjuster(
                mutableMapOf<String, Double>("south" to 0.86, "central" to 0.87, "retail" to 0.87),
            ),
            TieredFallbackSwapGroupAdjuster(
                mutableListOf<Double>(2161.0, 2940.0, 3193.0, 3866.0),
                mutableListOf<Double>(1.02, 0.96, 0.83, 1.04),
            ),
            FallbackSwapGroupEndsAtOffsetAdjuster(1.47),
            PercentFallbackSwapGroupAdjuster(-7.5),
            FlaggedFallbackSwapGroupAdjuster(flags, "fallback-swap-group-weighted", -1.3),
            CapFallbackSwapGroupAdjuster(9277.0),
        ),
        trace = mutableListOf<String>(),
        floor = 4.2,
        ceiling = 82052.6,
    )
}
