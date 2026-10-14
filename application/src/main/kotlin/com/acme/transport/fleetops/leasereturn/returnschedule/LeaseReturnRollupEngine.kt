package com.acme.transport.fleetops.leasereturn.returnschedule

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the lease return rollup calculation. */
interface LeaseReturnRollupFactor {
    fun describe(): String
    fun adjust(item: LeaseReturnRollup, value: Double): Double
}

data class FlaggedLeaseReturnRollupAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LeaseReturnRollupFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LeaseReturnRollup, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class NameLeaseReturnRollupAdjuster(var factors: MutableMap<String, Double>) : LeaseReturnRollupFactor {
    override fun describe(): String = "by name"

    override fun adjust(item: LeaseReturnRollup, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumLeaseReturnRollupAdjuster(var minimum: Double) : LeaseReturnRollupFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LeaseReturnRollup, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapLeaseReturnRollupAdjuster(var cap: Double) : LeaseReturnRollupFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LeaseReturnRollup, value: Double): Double = minOf(value, cap)
}

data class RoundingLeaseReturnRollupAdjuster(var step: Double) : LeaseReturnRollupFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LeaseReturnRollup, value: Double): Double = roundTo(value, step)
}

/** Runs the lease return rollup steps in order and keeps a trace of each. */
data class LeaseReturnRollupEngine(
    var steps: MutableList<LeaseReturnRollupFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: LeaseReturnRollup): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LeaseReturnRollupFactor = steps[index]
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

    fun evaluateAll(items: MutableList<LeaseReturnRollup>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultLeaseReturnRollupEngine(flags: Registry): LeaseReturnRollupEngine {
    return LeaseReturnRollupEngine(
        steps = mutableListOf<LeaseReturnRollupFactor>(
            FlaggedLeaseReturnRollupAdjuster(flags, "lease-return-rollup-batched", 8.2),
            NameLeaseReturnRollupAdjuster(mutableMapOf<String, Double>("east" to 1.12)),
            MinimumLeaseReturnRollupAdjuster(0.8),
            CapLeaseReturnRollupAdjuster(7971.0),
            RoundingLeaseReturnRollupAdjuster(0.25),
        ),
        trace = mutableListOf<String>(),
        floor = 1.0,
        ceiling = 81117.9,
    )
}
