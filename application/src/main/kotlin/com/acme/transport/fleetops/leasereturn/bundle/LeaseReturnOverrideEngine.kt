package com.acme.transport.fleetops.leasereturn.bundle

import com.acme.transport.kit.numbers.roundTo

/** One step of the lease return override calculation. */
interface LeaseReturnOverrideModifier {
    fun describe(): String
    fun adjust(item: LeaseReturnOverride, value: Double): Double
}

data class PercentLeaseReturnOverrideAdjuster(var percent: Double) : LeaseReturnOverrideModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LeaseReturnOverride, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapLeaseReturnOverrideAdjuster(var cap: Double) : LeaseReturnOverrideModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LeaseReturnOverride, value: Double): Double = minOf(value, cap)
}

data class SegmentLeaseReturnOverrideAdjuster(
    var factors: MutableMap<String, Double>,
) : LeaseReturnOverrideModifier {
    override fun describe(): String = "by segment"

    override fun adjust(item: LeaseReturnOverride, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingLeaseReturnOverrideAdjuster(var step: Double) : LeaseReturnOverrideModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LeaseReturnOverride, value: Double): Double = roundTo(value, step)
}

data class TieredLeaseReturnOverrideAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LeaseReturnOverrideModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LeaseReturnOverride, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumLeaseReturnOverrideAdjuster(var minimum: Double) : LeaseReturnOverrideModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LeaseReturnOverride, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the lease return override steps in order and keeps a trace of each. */
data class LeaseReturnOverrideEngine(
    var steps: MutableList<LeaseReturnOverrideModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: LeaseReturnOverride): Double {
        var current = item.netLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LeaseReturnOverrideModifier = steps[index]
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

    fun evaluateAll(items: MutableList<LeaseReturnOverride>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultLeaseReturnOverrideEngine(): LeaseReturnOverrideEngine {
    return LeaseReturnOverrideEngine(
        steps = mutableListOf<LeaseReturnOverrideModifier>(
            PercentLeaseReturnOverrideAdjuster(-5.7),
            CapLeaseReturnOverrideAdjuster(46022.0),
            SegmentLeaseReturnOverrideAdjuster(mutableMapOf<String, Double>("retail" to 0.94, "east" to 0.81)),
            RoundingLeaseReturnOverrideAdjuster(5.0),
            TieredLeaseReturnOverrideAdjuster(
                mutableListOf<Double>(598.0, 3329.0, 7580.0),
                mutableListOf<Double>(1.07, 1.07, 0.94),
            ),
            MinimumLeaseReturnOverrideAdjuster(33.8),
        ),
        trace = mutableListOf<String>(),
        floor = 2.6,
        ceiling = 72964.1,
    )
}
