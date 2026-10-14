package com.acme.transport.fleetops.leasereturn.budget

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the lease return threshold calculation. */
interface LeaseReturnThresholdModifier {
    fun describe(): String
    fun adjust(item: LeaseReturnThreshold, value: Double): Double
}

data class PercentLeaseReturnThresholdAdjuster(var percent: Double) : LeaseReturnThresholdModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LeaseReturnThreshold, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedLeaseReturnThresholdAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LeaseReturnThresholdModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LeaseReturnThreshold, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RegionLeaseReturnThresholdAdjuster(
    var factors: MutableMap<String, Double>,
) : LeaseReturnThresholdModifier {
    override fun describe(): String = "by region"

    override fun adjust(item: LeaseReturnThreshold, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredLeaseReturnThresholdAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LeaseReturnThresholdModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LeaseReturnThreshold, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumLeaseReturnThresholdAdjuster(var minimum: Double) : LeaseReturnThresholdModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LeaseReturnThreshold, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingLeaseReturnThresholdAdjuster(var step: Double) : LeaseReturnThresholdModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LeaseReturnThreshold, value: Double): Double = roundTo(value, step)
}

/** Runs the lease return threshold steps in order and keeps a trace of each. */
data class LeaseReturnThresholdEngine(
    var steps: MutableList<LeaseReturnThresholdModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: LeaseReturnThreshold): Double {
        var current = item.netLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<LeaseReturnThreshold>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultLeaseReturnThresholdEngine(flags: Registry): LeaseReturnThresholdEngine {
    return LeaseReturnThresholdEngine(
        steps = mutableListOf<LeaseReturnThresholdModifier>(
            PercentLeaseReturnThresholdAdjuster(7.5),
            FlaggedLeaseReturnThresholdAdjuster(flags, "lease-return-threshold-legacy", 4.3),
            RegionLeaseReturnThresholdAdjuster(mutableMapOf<String, Double>("online" to 0.82, "south" to 1.31)),
            TieredLeaseReturnThresholdAdjuster(
                mutableListOf<Double>(3430.0, 4520.0, 6334.0),
                mutableListOf<Double>(1.16, 0.86, 0.94),
            ),
            MinimumLeaseReturnThresholdAdjuster(32.6),
            RoundingLeaseReturnThresholdAdjuster(0.1),
        ),
        trace = mutableListOf<String>(),
        floor = 1.6,
        ceiling = 63954.2,
    )
}
