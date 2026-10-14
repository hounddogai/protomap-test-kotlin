package com.acme.transport.fleetops.leasereturn.tier

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the lease return baseline calculation. */
interface LeaseReturnBaselineFactor {
    fun describe(): String
    fun adjust(item: LeaseReturnBaseline, value: Double): Double
}

data class MinimumLeaseReturnBaselineAdjuster(var minimum: Double) : LeaseReturnBaselineFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LeaseReturnBaseline, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedLeaseReturnBaselineAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LeaseReturnBaselineFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LeaseReturnBaseline, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapLeaseReturnBaselineAdjuster(var cap: Double) : LeaseReturnBaselineFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LeaseReturnBaseline, value: Double): Double = minOf(value, cap)
}

data class TieredLeaseReturnBaselineAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LeaseReturnBaselineFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LeaseReturnBaseline, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingLeaseReturnBaselineAdjuster(var step: Double) : LeaseReturnBaselineFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LeaseReturnBaseline, value: Double): Double = roundTo(value, step)
}

/** Runs the lease return baseline steps in order and keeps a trace of each. */
data class LeaseReturnBaselineEngine(
    var steps: MutableList<LeaseReturnBaselineFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: LeaseReturnBaseline): Double {
        var current = item.netExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LeaseReturnBaselineFactor = steps[index]
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

    fun evaluateAll(items: MutableList<LeaseReturnBaseline>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultLeaseReturnBaselineEngine(flags: Registry): LeaseReturnBaselineEngine {
    return LeaseReturnBaselineEngine(
        steps = mutableListOf<LeaseReturnBaselineFactor>(
            MinimumLeaseReturnBaselineAdjuster(10.8),
            FlaggedLeaseReturnBaselineAdjuster(flags, "lease-return-baseline-rolling", 7.1),
            CapLeaseReturnBaselineAdjuster(18230.0),
            TieredLeaseReturnBaselineAdjuster(
                mutableListOf<Double>(1100.0, 3454.0),
                mutableListOf<Double>(0.99, 1.1),
            ),
            RoundingLeaseReturnBaselineAdjuster(5.0),
        ),
        trace = mutableListOf<String>(),
        floor = 4.8,
        ceiling = 93174.2,
    )
}
