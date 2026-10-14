package com.acme.transport.safety.nearmiss.missvariance

import com.acme.transport.kit.flags.Registry

/** One step of the adaptive near miss variance calculation. */
interface AdaptiveNearMissVarianceStep {
    fun describe(): String
    fun adjust(item: AdaptiveNearMissVariance, value: Double): Double
}

data class FlaggedAdaptiveNearMissVarianceAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : AdaptiveNearMissVarianceStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: AdaptiveNearMissVariance, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class AdaptiveNearMissVariancePalletsOffsetAdjuster(var weight: Double) : AdaptiveNearMissVarianceStep {
    override fun describe(): String = "plus pallets times $weight"

    override fun adjust(item: AdaptiveNearMissVariance, value: Double): Double {
        return value + item.pallets.toDouble() * weight
    }
}

data class CapAdaptiveNearMissVarianceAdjuster(var cap: Double) : AdaptiveNearMissVarianceStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AdaptiveNearMissVariance, value: Double): Double = minOf(value, cap)
}

data class MinimumAdaptiveNearMissVarianceAdjuster(var minimum: Double) : AdaptiveNearMissVarianceStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AdaptiveNearMissVariance, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class LabelAdaptiveNearMissVarianceAdjuster(
    var factors: MutableMap<String, Double>,
) : AdaptiveNearMissVarianceStep {
    override fun describe(): String = "by label"

    override fun adjust(item: AdaptiveNearMissVariance, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentAdaptiveNearMissVarianceAdjuster(var percent: Double) : AdaptiveNearMissVarianceStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: AdaptiveNearMissVariance, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the adaptive near miss variance steps in order and keeps a trace of each. */
data class AdaptiveNearMissVarianceEngine(
    var steps: MutableList<AdaptiveNearMissVarianceStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: AdaptiveNearMissVariance): Double {
        var current = item.effectiveExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: AdaptiveNearMissVarianceStep = steps[index]
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

    fun evaluateAll(items: MutableList<AdaptiveNearMissVariance>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultAdaptiveNearMissVarianceEngine(flags: Registry): AdaptiveNearMissVarianceEngine {
    return AdaptiveNearMissVarianceEngine(
        steps = mutableListOf<AdaptiveNearMissVarianceStep>(
            FlaggedAdaptiveNearMissVarianceAdjuster(flags, "adaptive-near-miss-variance-tiered", 5.6),
            AdaptiveNearMissVariancePalletsOffsetAdjuster(1.96),
            CapAdaptiveNearMissVarianceAdjuster(18916.0),
            MinimumAdaptiveNearMissVarianceAdjuster(30.6),
            LabelAdaptiveNearMissVarianceAdjuster(
                mutableMapOf<String, Double>("retail" to 0.83, "east" to 1.39),
            ),
            PercentAdaptiveNearMissVarianceAdjuster(8.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.8,
        ceiling = 32312.2,
    )
}
