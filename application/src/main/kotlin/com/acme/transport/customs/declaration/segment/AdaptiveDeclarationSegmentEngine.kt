package com.acme.transport.customs.declaration.segment

import com.acme.transport.kit.flags.Registry

/** One step of the adaptive declaration segment calculation. */
interface AdaptiveDeclarationSegmentModifier {
    fun describe(): String
    fun adjust(item: AdaptiveDeclarationSegment, value: Double): Double
}

data class MinimumAdaptiveDeclarationSegmentAdjuster(var minimum: Double) : AdaptiveDeclarationSegmentModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AdaptiveDeclarationSegment, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentAdaptiveDeclarationSegmentAdjuster(var percent: Double) : AdaptiveDeclarationSegmentModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: AdaptiveDeclarationSegment, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapAdaptiveDeclarationSegmentAdjuster(var cap: Double) : AdaptiveDeclarationSegmentModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AdaptiveDeclarationSegment, value: Double): Double = minOf(value, cap)
}

data class FlaggedAdaptiveDeclarationSegmentAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : AdaptiveDeclarationSegmentModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: AdaptiveDeclarationSegment, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredAdaptiveDeclarationSegmentAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : AdaptiveDeclarationSegmentModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: AdaptiveDeclarationSegment, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the adaptive declaration segment steps in order and keeps a trace of each. */
data class AdaptiveDeclarationSegmentEngine(
    var steps: MutableList<AdaptiveDeclarationSegmentModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: AdaptiveDeclarationSegment): Double {
        var current = item.netExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: AdaptiveDeclarationSegmentModifier = steps[index]
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

    fun evaluateAll(items: MutableList<AdaptiveDeclarationSegment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultAdaptiveDeclarationSegmentEngine(flags: Registry): AdaptiveDeclarationSegmentEngine {
    return AdaptiveDeclarationSegmentEngine(
        steps = mutableListOf<AdaptiveDeclarationSegmentModifier>(
            MinimumAdaptiveDeclarationSegmentAdjuster(10.5),
            PercentAdaptiveDeclarationSegmentAdjuster(7.4),
            CapAdaptiveDeclarationSegmentAdjuster(14035.0),
            FlaggedAdaptiveDeclarationSegmentAdjuster(flags, "adaptive-declaration-segment-strict", -8.8),
            TieredAdaptiveDeclarationSegmentAdjuster(
                mutableListOf<Double>(3767.0, 6853.0, 8113.0, 8396.0),
                mutableListOf<Double>(1.11, 0.97, 0.82, 1.14),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 0.1,
        ceiling = 23649.9,
    )
}
