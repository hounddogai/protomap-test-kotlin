package com.acme.transport.fleetops.leasereturn.rule

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the lease return snapshot calculation. */
interface LeaseReturnSnapshotStep {
    fun describe(): String
    fun adjust(item: LeaseReturnSnapshot, value: Double): Double
}

data class FlaggedLeaseReturnSnapshotAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LeaseReturnSnapshotStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LeaseReturnSnapshot, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumLeaseReturnSnapshotAdjuster(var minimum: Double) : LeaseReturnSnapshotStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LeaseReturnSnapshot, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredLeaseReturnSnapshotAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LeaseReturnSnapshotStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LeaseReturnSnapshot, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class LeaseReturnSnapshotPriorityOffsetAdjuster(var weight: Double) : LeaseReturnSnapshotStep {
    override fun describe(): String = "plus priority times $weight"

    override fun adjust(item: LeaseReturnSnapshot, value: Double): Double = value + item.priority.toDouble() * weight
}

data class RoundingLeaseReturnSnapshotAdjuster(var step: Double) : LeaseReturnSnapshotStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LeaseReturnSnapshot, value: Double): Double = roundTo(value, step)
}

/** Runs the lease return snapshot steps in order and keeps a trace of each. */
data class LeaseReturnSnapshotEngine(
    var steps: MutableList<LeaseReturnSnapshotStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: LeaseReturnSnapshot): Double {
        var current = item.effectiveTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LeaseReturnSnapshotStep = steps[index]
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

    fun evaluateAll(items: MutableList<LeaseReturnSnapshot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultLeaseReturnSnapshotEngine(flags: Registry): LeaseReturnSnapshotEngine {
    return LeaseReturnSnapshotEngine(
        steps = mutableListOf<LeaseReturnSnapshotStep>(
            FlaggedLeaseReturnSnapshotAdjuster(flags, "lease-return-snapshot-fallback", -9.6),
            MinimumLeaseReturnSnapshotAdjuster(25.3),
            TieredLeaseReturnSnapshotAdjuster(
                mutableListOf<Double>(3571.0, 5166.0, 7631.0, 8595.0),
                mutableListOf<Double>(1.18, 1.19, 1.09, 1.01),
            ),
            LeaseReturnSnapshotPriorityOffsetAdjuster(1.02),
            RoundingLeaseReturnSnapshotAdjuster(0.5),
        ),
        trace = mutableListOf<String>(),
        floor = 0.5,
        ceiling = 59835.5,
    )
}
