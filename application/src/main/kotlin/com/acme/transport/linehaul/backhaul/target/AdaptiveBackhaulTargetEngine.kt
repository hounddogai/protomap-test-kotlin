package com.acme.transport.linehaul.backhaul.target

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the adaptive backhaul target calculation. */
interface AdaptiveBackhaulTargetAdjuster {
    fun describe(): String
    fun adjust(item: AdaptiveBackhaulTarget, value: Double): Double
}

data class TieredAdaptiveBackhaulTargetAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : AdaptiveBackhaulTargetAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: AdaptiveBackhaulTarget, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentAdaptiveBackhaulTargetAdjuster(var percent: Double) : AdaptiveBackhaulTargetAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: AdaptiveBackhaulTarget, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapAdaptiveBackhaulTargetAdjuster(var cap: Double) : AdaptiveBackhaulTargetAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AdaptiveBackhaulTarget, value: Double): Double = minOf(value, cap)
}

data class FlaggedAdaptiveBackhaulTargetAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : AdaptiveBackhaulTargetAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: AdaptiveBackhaulTarget, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingAdaptiveBackhaulTargetAdjuster(var step: Double) : AdaptiveBackhaulTargetAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: AdaptiveBackhaulTarget, value: Double): Double = roundTo(value, step)
}

/** Runs the adaptive backhaul target steps in order and keeps a trace of each. */
data class AdaptiveBackhaulTargetEngine(
    var steps: MutableList<AdaptiveBackhaulTargetAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: AdaptiveBackhaulTarget): Double {
        var current = item.effectiveLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: AdaptiveBackhaulTargetAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<AdaptiveBackhaulTarget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultAdaptiveBackhaulTargetEngine(flags: Registry): AdaptiveBackhaulTargetEngine {
    return AdaptiveBackhaulTargetEngine(
        steps = mutableListOf<AdaptiveBackhaulTargetAdjuster>(
            TieredAdaptiveBackhaulTargetAdjuster(
                mutableListOf<Double>(4197.0, 8728.0),
                mutableListOf<Double>(0.97, 1.07),
            ),
            PercentAdaptiveBackhaulTargetAdjuster(14.0),
            CapAdaptiveBackhaulTargetAdjuster(3051.0),
            FlaggedAdaptiveBackhaulTargetAdjuster(flags, "adaptive-backhaul-target-manual", 0.6),
            RoundingAdaptiveBackhaulTargetAdjuster(5.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.7,
        ceiling = 86566.3,
    )
}
