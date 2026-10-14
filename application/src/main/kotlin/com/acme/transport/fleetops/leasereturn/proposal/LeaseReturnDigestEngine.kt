package com.acme.transport.fleetops.leasereturn.proposal

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the lease return digest calculation. */
interface LeaseReturnDigestModifier {
    fun describe(): String
    fun adjust(item: LeaseReturnDigest, value: Double): Double
}

data class RoundingLeaseReturnDigestAdjuster(var step: Double) : LeaseReturnDigestModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LeaseReturnDigest, value: Double): Double = roundTo(value, step)
}

data class TieredLeaseReturnDigestAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LeaseReturnDigestModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LeaseReturnDigest, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapLeaseReturnDigestAdjuster(var cap: Double) : LeaseReturnDigestModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LeaseReturnDigest, value: Double): Double = minOf(value, cap)
}

data class LeaseReturnDigestThresholdOffsetAdjuster(var weight: Double) : LeaseReturnDigestModifier {
    override fun describe(): String = "plus threshold times $weight"

    override fun adjust(item: LeaseReturnDigest, value: Double): Double = value + item.threshold * weight
}

data class FlaggedLeaseReturnDigestAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LeaseReturnDigestModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LeaseReturnDigest, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class LocaleLeaseReturnDigestAdjuster(
    var factors: MutableMap<String, Double>,
) : LeaseReturnDigestModifier {
    override fun describe(): String = "by locale"

    override fun adjust(item: LeaseReturnDigest, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the lease return digest steps in order and keeps a trace of each. */
data class LeaseReturnDigestEngine(
    var steps: MutableList<LeaseReturnDigestModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: LeaseReturnDigest): Double {
        var current = item.weightedVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<LeaseReturnDigest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultLeaseReturnDigestEngine(flags: Registry): LeaseReturnDigestEngine {
    return LeaseReturnDigestEngine(
        steps = mutableListOf<LeaseReturnDigestModifier>(
            RoundingLeaseReturnDigestAdjuster(0.25),
            TieredLeaseReturnDigestAdjuster(
                mutableListOf<Double>(130.0, 212.0, 4117.0, 4230.0),
                mutableListOf<Double>(0.99, 1.02, 1.05, 0.87),
            ),
            CapLeaseReturnDigestAdjuster(4985.0),
            LeaseReturnDigestThresholdOffsetAdjuster(0.61),
            FlaggedLeaseReturnDigestAdjuster(flags, "lease-return-digest-manual", 7.5),
            LocaleLeaseReturnDigestAdjuster(mutableMapOf<String, Double>("central" to 1.32)),
        ),
        trace = mutableListOf<String>(),
        floor = 4.3,
        ceiling = 10569.1,
    )
}
