package com.acme.transport.fleetops.leasereturn.estimate

import com.acme.transport.kit.flags.Registry

/** One step of the lease return estimate calculation. */
interface LeaseReturnEstimateStep {
    fun describe(): String
    fun adjust(item: LeaseReturnEstimate, value: Double): Double
}

data class CategoryLeaseReturnEstimateAdjuster(
    var factors: MutableMap<String, Double>,
) : LeaseReturnEstimateStep {
    override fun describe(): String = "by category"

    override fun adjust(item: LeaseReturnEstimate, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class LeaseReturnEstimateFloorValueOffsetAdjuster(var weight: Double) : LeaseReturnEstimateStep {
    override fun describe(): String = "plus floor value times $weight"

    override fun adjust(item: LeaseReturnEstimate, value: Double): Double = value + item.floorValue * weight
}

data class CapLeaseReturnEstimateAdjuster(var cap: Double) : LeaseReturnEstimateStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LeaseReturnEstimate, value: Double): Double = minOf(value, cap)
}

data class FlaggedLeaseReturnEstimateAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LeaseReturnEstimateStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LeaseReturnEstimate, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumLeaseReturnEstimateAdjuster(var minimum: Double) : LeaseReturnEstimateStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LeaseReturnEstimate, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the lease return estimate steps in order and keeps a trace of each. */
data class LeaseReturnEstimateEngine(
    var steps: MutableList<LeaseReturnEstimateStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: LeaseReturnEstimate): Double {
        var current = item.weightedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LeaseReturnEstimateStep = steps[index]
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

    fun evaluateAll(items: MutableList<LeaseReturnEstimate>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultLeaseReturnEstimateEngine(flags: Registry): LeaseReturnEstimateEngine {
    return LeaseReturnEstimateEngine(
        steps = mutableListOf<LeaseReturnEstimateStep>(
            CategoryLeaseReturnEstimateAdjuster(
                mutableMapOf<String, Double>("retail" to 0.81, "east" to 0.77, "central" to 0.75),
            ),
            LeaseReturnEstimateFloorValueOffsetAdjuster(0.88),
            CapLeaseReturnEstimateAdjuster(48681.0),
            FlaggedLeaseReturnEstimateAdjuster(flags, "lease-return-estimate-pending", 4.8),
            MinimumLeaseReturnEstimateAdjuster(42.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.2,
        ceiling = 29023.0,
    )
}
