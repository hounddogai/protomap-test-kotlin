package com.acme.transport.fleetops.leasereturn.milestone

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the lease return variance calculation. */
interface LeaseReturnVarianceAdjuster {
    fun describe(): String
    fun adjust(item: LeaseReturnVariance, value: Double): Double
}

data class RoundingLeaseReturnVarianceAdjuster(var step: Double) : LeaseReturnVarianceAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LeaseReturnVariance, value: Double): Double = roundTo(value, step)
}

data class TieredLeaseReturnVarianceAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LeaseReturnVarianceAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LeaseReturnVariance, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumLeaseReturnVarianceAdjuster(var minimum: Double) : LeaseReturnVarianceAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LeaseReturnVariance, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedLeaseReturnVarianceAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LeaseReturnVarianceAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LeaseReturnVariance, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class LeaseReturnVarianceRetriesOffsetAdjuster(var weight: Double) : LeaseReturnVarianceAdjuster {
    override fun describe(): String = "plus retries times $weight"

    override fun adjust(item: LeaseReturnVariance, value: Double): Double = value + item.retries.toDouble() * weight
}

/** Runs the lease return variance steps in order and keeps a trace of each. */
data class LeaseReturnVarianceEngine(
    var steps: MutableList<LeaseReturnVarianceAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: LeaseReturnVariance): Double {
        var current = item.projectedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LeaseReturnVarianceAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<LeaseReturnVariance>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultLeaseReturnVarianceEngine(flags: Registry): LeaseReturnVarianceEngine {
    return LeaseReturnVarianceEngine(
        steps = mutableListOf<LeaseReturnVarianceAdjuster>(
            RoundingLeaseReturnVarianceAdjuster(0.05),
            TieredLeaseReturnVarianceAdjuster(
                mutableListOf<Double>(28.0, 1192.0, 2374.0),
                mutableListOf<Double>(0.81, 1.18, 0.85),
            ),
            MinimumLeaseReturnVarianceAdjuster(26.0),
            FlaggedLeaseReturnVarianceAdjuster(flags, "lease-return-variance-default", -0.3),
            LeaseReturnVarianceRetriesOffsetAdjuster(0.58),
        ),
        trace = mutableListOf<String>(),
        floor = 0.8,
        ceiling = 36263.0,
    )
}
