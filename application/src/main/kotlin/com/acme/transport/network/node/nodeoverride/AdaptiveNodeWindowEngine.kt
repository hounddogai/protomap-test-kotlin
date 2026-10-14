package com.acme.transport.network.node.nodeoverride

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the adaptive node window calculation. */
interface AdaptiveNodeWindowAdjuster {
    fun describe(): String
    fun adjust(item: AdaptiveNodeWindow, value: Double): Double
}

data class TieredAdaptiveNodeWindowAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : AdaptiveNodeWindowAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: AdaptiveNodeWindow, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedAdaptiveNodeWindowAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : AdaptiveNodeWindowAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: AdaptiveNodeWindow, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapAdaptiveNodeWindowAdjuster(var cap: Double) : AdaptiveNodeWindowAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AdaptiveNodeWindow, value: Double): Double = minOf(value, cap)
}

data class LabelAdaptiveNodeWindowAdjuster(
    var factors: MutableMap<String, Double>,
) : AdaptiveNodeWindowAdjuster {
    override fun describe(): String = "by label"

    override fun adjust(item: AdaptiveNodeWindow, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class AdaptiveNodeWindowBudgetOffsetAdjuster(var weight: Double) : AdaptiveNodeWindowAdjuster {
    override fun describe(): String = "plus budget times $weight"

    override fun adjust(item: AdaptiveNodeWindow, value: Double): Double = value + item.budget * weight
}

data class RoundingAdaptiveNodeWindowAdjuster(var step: Double) : AdaptiveNodeWindowAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: AdaptiveNodeWindow, value: Double): Double = roundTo(value, step)
}

/** Runs the adaptive node window steps in order and keeps a trace of each. */
data class AdaptiveNodeWindowEngine(
    var steps: MutableList<AdaptiveNodeWindowAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: AdaptiveNodeWindow): Double {
        var current = item.projectedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: AdaptiveNodeWindowAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<AdaptiveNodeWindow>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultAdaptiveNodeWindowEngine(flags: Registry): AdaptiveNodeWindowEngine {
    return AdaptiveNodeWindowEngine(
        steps = mutableListOf<AdaptiveNodeWindowAdjuster>(
            TieredAdaptiveNodeWindowAdjuster(
                mutableListOf<Double>(5279.0, 8299.0),
                mutableListOf<Double>(0.82, 1.03),
            ),
            FlaggedAdaptiveNodeWindowAdjuster(flags, "adaptive-node-window-primary", -2.7),
            CapAdaptiveNodeWindowAdjuster(46859.0),
            LabelAdaptiveNodeWindowAdjuster(mutableMapOf<String, Double>("south" to 0.71, "central" to 0.85)),
            AdaptiveNodeWindowBudgetOffsetAdjuster(1.9),
            RoundingAdaptiveNodeWindowAdjuster(0.5),
        ),
        trace = mutableListOf<String>(),
        floor = 4.9,
        ceiling = 98775.1,
    )
}
