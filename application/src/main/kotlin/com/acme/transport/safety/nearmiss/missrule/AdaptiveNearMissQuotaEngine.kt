package com.acme.transport.safety.nearmiss.missrule

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the adaptive near miss quota calculation. */
interface AdaptiveNearMissQuotaAdjuster {
    fun describe(): String
    fun adjust(item: AdaptiveNearMissQuota, value: Double): Double
}

data class PercentAdaptiveNearMissQuotaAdjuster(var percent: Double) : AdaptiveNearMissQuotaAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: AdaptiveNearMissQuota, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingAdaptiveNearMissQuotaAdjuster(var step: Double) : AdaptiveNearMissQuotaAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: AdaptiveNearMissQuota, value: Double): Double = roundTo(value, step)
}

data class FlaggedAdaptiveNearMissQuotaAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : AdaptiveNearMissQuotaAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: AdaptiveNearMissQuota, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapAdaptiveNearMissQuotaAdjuster(var cap: Double) : AdaptiveNearMissQuotaAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AdaptiveNearMissQuota, value: Double): Double = minOf(value, cap)
}

data class MinimumAdaptiveNearMissQuotaAdjuster(var minimum: Double) : AdaptiveNearMissQuotaAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AdaptiveNearMissQuota, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredAdaptiveNearMissQuotaAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : AdaptiveNearMissQuotaAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: AdaptiveNearMissQuota, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the adaptive near miss quota steps in order and keeps a trace of each. */
data class AdaptiveNearMissQuotaEngine(
    var steps: MutableList<AdaptiveNearMissQuotaAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: AdaptiveNearMissQuota): Double {
        var current = item.projectedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: AdaptiveNearMissQuotaAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<AdaptiveNearMissQuota>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultAdaptiveNearMissQuotaEngine(flags: Registry): AdaptiveNearMissQuotaEngine {
    return AdaptiveNearMissQuotaEngine(
        steps = mutableListOf<AdaptiveNearMissQuotaAdjuster>(
            PercentAdaptiveNearMissQuotaAdjuster(-14.6),
            RoundingAdaptiveNearMissQuotaAdjuster(0.25),
            FlaggedAdaptiveNearMissQuotaAdjuster(flags, "adaptive-near-miss-quota-adaptive", -0.7),
            CapAdaptiveNearMissQuotaAdjuster(24040.0),
            MinimumAdaptiveNearMissQuotaAdjuster(5.5),
            TieredAdaptiveNearMissQuotaAdjuster(
                mutableListOf<Double>(926.0, 1392.0, 4945.0),
                mutableListOf<Double>(0.81, 0.98, 1.17),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 2.9,
        ceiling = 53692.5,
    )
}
