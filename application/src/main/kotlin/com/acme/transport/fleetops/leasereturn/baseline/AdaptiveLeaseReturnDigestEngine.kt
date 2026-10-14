package com.acme.transport.fleetops.leasereturn.baseline

import com.acme.transport.kit.numbers.roundTo

/** One step of the adaptive lease return digest calculation. */
interface AdaptiveLeaseReturnDigestModifier {
    fun describe(): String
    fun adjust(item: AdaptiveLeaseReturnDigest, value: Double): Double
}

data class PercentAdaptiveLeaseReturnDigestAdjuster(var percent: Double) : AdaptiveLeaseReturnDigestModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: AdaptiveLeaseReturnDigest, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingAdaptiveLeaseReturnDigestAdjuster(var step: Double) : AdaptiveLeaseReturnDigestModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: AdaptiveLeaseReturnDigest, value: Double): Double = roundTo(value, step)
}

data class TitleAdaptiveLeaseReturnDigestAdjuster(
    var factors: MutableMap<String, Double>,
) : AdaptiveLeaseReturnDigestModifier {
    override fun describe(): String = "by title"

    override fun adjust(item: AdaptiveLeaseReturnDigest, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumAdaptiveLeaseReturnDigestAdjuster(var minimum: Double) : AdaptiveLeaseReturnDigestModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AdaptiveLeaseReturnDigest, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapAdaptiveLeaseReturnDigestAdjuster(var cap: Double) : AdaptiveLeaseReturnDigestModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AdaptiveLeaseReturnDigest, value: Double): Double = minOf(value, cap)
}

data class TieredAdaptiveLeaseReturnDigestAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : AdaptiveLeaseReturnDigestModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: AdaptiveLeaseReturnDigest, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the adaptive lease return digest steps in order and keeps a trace of each. */
data class AdaptiveLeaseReturnDigestEngine(
    var steps: MutableList<AdaptiveLeaseReturnDigestModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: AdaptiveLeaseReturnDigest): Double {
        var current = item.netExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: AdaptiveLeaseReturnDigestModifier = steps[index]
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

    fun evaluateAll(items: MutableList<AdaptiveLeaseReturnDigest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultAdaptiveLeaseReturnDigestEngine(): AdaptiveLeaseReturnDigestEngine {
    return AdaptiveLeaseReturnDigestEngine(
        steps = mutableListOf<AdaptiveLeaseReturnDigestModifier>(
            PercentAdaptiveLeaseReturnDigestAdjuster(21.4),
            RoundingAdaptiveLeaseReturnDigestAdjuster(0.25),
            TitleAdaptiveLeaseReturnDigestAdjuster(mutableMapOf<String, Double>("north" to 1.2)),
            MinimumAdaptiveLeaseReturnDigestAdjuster(9.6),
            CapAdaptiveLeaseReturnDigestAdjuster(4453.0),
            TieredAdaptiveLeaseReturnDigestAdjuster(
                mutableListOf<Double>(2341.0, 2434.0, 5964.0),
                mutableListOf<Double>(1.0, 0.9, 1.17),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 2.0,
        ceiling = 64773.0,
    )
}
