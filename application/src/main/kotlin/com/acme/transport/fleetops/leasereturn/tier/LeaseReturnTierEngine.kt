package com.acme.transport.fleetops.leasereturn.tier

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the lease return tier calculation. */
interface LeaseReturnTierModifier {
    fun describe(): String
    fun adjust(item: LeaseReturnTier, value: Double): Double
}

data class RoundingLeaseReturnTierAdjuster(var step: Double) : LeaseReturnTierModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LeaseReturnTier, value: Double): Double = roundTo(value, step)
}

data class CapLeaseReturnTierAdjuster(var cap: Double) : LeaseReturnTierModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LeaseReturnTier, value: Double): Double = minOf(value, cap)
}

data class MinimumLeaseReturnTierAdjuster(var minimum: Double) : LeaseReturnTierModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LeaseReturnTier, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedLeaseReturnTierAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LeaseReturnTierModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LeaseReturnTier, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the lease return tier steps in order and keeps a trace of each. */
data class LeaseReturnTierEngine(
    var steps: MutableList<LeaseReturnTierModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: LeaseReturnTier): Double {
        var current = item.effectiveExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LeaseReturnTierModifier = steps[index]
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

    fun evaluateAll(items: MutableList<LeaseReturnTier>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultLeaseReturnTierEngine(flags: Registry): LeaseReturnTierEngine {
    return LeaseReturnTierEngine(
        steps = mutableListOf<LeaseReturnTierModifier>(
            RoundingLeaseReturnTierAdjuster(0.5),
            CapLeaseReturnTierAdjuster(10322.0),
            MinimumLeaseReturnTierAdjuster(6.2),
            FlaggedLeaseReturnTierAdjuster(flags, "lease-return-tier-capped", -1.3),
        ),
        trace = mutableListOf<String>(),
        floor = 2.2,
        ceiling = 32047.8,
    )
}
