package com.acme.transport.fleetops.leasereturn.segment

import com.acme.transport.kit.flags.Registry

/** One step of the fallback lease return tier calculation. */
interface FallbackLeaseReturnTierAdjuster {
    fun describe(): String
    fun adjust(item: FallbackLeaseReturnTier, value: Double): Double
}

data class FlaggedFallbackLeaseReturnTierAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : FallbackLeaseReturnTierAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: FallbackLeaseReturnTier, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredFallbackLeaseReturnTierAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : FallbackLeaseReturnTierAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: FallbackLeaseReturnTier, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class UnitFallbackLeaseReturnTierAdjuster(
    var factors: MutableMap<String, Double>,
) : FallbackLeaseReturnTierAdjuster {
    override fun describe(): String = "by unit"

    override fun adjust(item: FallbackLeaseReturnTier, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the fallback lease return tier steps in order and keeps a trace of each. */
data class FallbackLeaseReturnTierEngine(
    var steps: MutableList<FallbackLeaseReturnTierAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: FallbackLeaseReturnTier): Double {
        var current = item.adjustedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: FallbackLeaseReturnTierAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<FallbackLeaseReturnTier>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultFallbackLeaseReturnTierEngine(flags: Registry): FallbackLeaseReturnTierEngine {
    return FallbackLeaseReturnTierEngine(
        steps = mutableListOf<FallbackLeaseReturnTierAdjuster>(
            FlaggedFallbackLeaseReturnTierAdjuster(flags, "fallback-lease-return-tier-batched", -2.8),
            TieredFallbackLeaseReturnTierAdjuster(
                mutableListOf<Double>(1292.0, 7827.0),
                mutableListOf<Double>(1.13, 1.02),
            ),
            UnitFallbackLeaseReturnTierAdjuster(mutableMapOf<String, Double>("west" to 0.88, "retail" to 1.06)),
        ),
        trace = mutableListOf<String>(),
        floor = 4.8,
        ceiling = 32628.6,
    )
}
