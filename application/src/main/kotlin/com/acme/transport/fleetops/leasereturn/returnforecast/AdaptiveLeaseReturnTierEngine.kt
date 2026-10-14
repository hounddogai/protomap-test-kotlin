package com.acme.transport.fleetops.leasereturn.returnforecast

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the adaptive lease return tier calculation. */
interface AdaptiveLeaseReturnTierAdjuster {
    fun describe(): String
    fun adjust(item: AdaptiveLeaseReturnTier, value: Double): Double
}

data class AdaptiveLeaseReturnTierGraceDaysOffsetAdjuster(
    var weight: Double,
) : AdaptiveLeaseReturnTierAdjuster {
    override fun describe(): String = "plus grace days times $weight"

    override fun adjust(item: AdaptiveLeaseReturnTier, value: Double): Double {
        return value + item.graceDays.toDouble() * weight
    }
}

data class MinimumAdaptiveLeaseReturnTierAdjuster(var minimum: Double) : AdaptiveLeaseReturnTierAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AdaptiveLeaseReturnTier, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedAdaptiveLeaseReturnTierAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : AdaptiveLeaseReturnTierAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: AdaptiveLeaseReturnTier, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingAdaptiveLeaseReturnTierAdjuster(var step: Double) : AdaptiveLeaseReturnTierAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: AdaptiveLeaseReturnTier, value: Double): Double = roundTo(value, step)
}

data class TitleAdaptiveLeaseReturnTierAdjuster(
    var factors: MutableMap<String, Double>,
) : AdaptiveLeaseReturnTierAdjuster {
    override fun describe(): String = "by title"

    override fun adjust(item: AdaptiveLeaseReturnTier, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the adaptive lease return tier steps in order and keeps a trace of each. */
data class AdaptiveLeaseReturnTierEngine(
    var steps: MutableList<AdaptiveLeaseReturnTierAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: AdaptiveLeaseReturnTier): Double {
        var current = item.effectiveExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: AdaptiveLeaseReturnTierAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<AdaptiveLeaseReturnTier>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultAdaptiveLeaseReturnTierEngine(flags: Registry): AdaptiveLeaseReturnTierEngine {
    return AdaptiveLeaseReturnTierEngine(
        steps = mutableListOf<AdaptiveLeaseReturnTierAdjuster>(
            AdaptiveLeaseReturnTierGraceDaysOffsetAdjuster(0.51),
            MinimumAdaptiveLeaseReturnTierAdjuster(48.2),
            FlaggedAdaptiveLeaseReturnTierAdjuster(flags, "adaptive-lease-return-tier-batched", -6.3),
            RoundingAdaptiveLeaseReturnTierAdjuster(0.05),
            TitleAdaptiveLeaseReturnTierAdjuster(
                mutableMapOf<String, Double>("retail" to 0.89, "central" to 0.81, "west" to 1.02),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 3.2,
        ceiling = 52011.2,
    )
}
