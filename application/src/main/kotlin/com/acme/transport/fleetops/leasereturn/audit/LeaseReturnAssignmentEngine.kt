package com.acme.transport.fleetops.leasereturn.audit

import com.acme.transport.kit.flags.Registry

/** One step of the lease return assignment calculation. */
interface LeaseReturnAssignmentFactor {
    fun describe(): String
    fun adjust(item: LeaseReturnAssignment, value: Double): Double
}

data class DepotLeaseReturnAssignmentAdjuster(
    var factors: MutableMap<String, Double>,
) : LeaseReturnAssignmentFactor {
    override fun describe(): String = "by depot"

    override fun adjust(item: LeaseReturnAssignment, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedLeaseReturnAssignmentAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LeaseReturnAssignmentFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LeaseReturnAssignment, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredLeaseReturnAssignmentAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LeaseReturnAssignmentFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LeaseReturnAssignment, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class LeaseReturnAssignmentCreatedAtOffsetAdjuster(var weight: Double) : LeaseReturnAssignmentFactor {
    override fun describe(): String = "plus created at times $weight"

    override fun adjust(item: LeaseReturnAssignment, value: Double): Double {
        return value + item.createdAt.toDouble() * weight
    }
}

data class MinimumLeaseReturnAssignmentAdjuster(var minimum: Double) : LeaseReturnAssignmentFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LeaseReturnAssignment, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the lease return assignment steps in order and keeps a trace of each. */
data class LeaseReturnAssignmentEngine(
    var steps: MutableList<LeaseReturnAssignmentFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: LeaseReturnAssignment): Double {
        var current = item.projectedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LeaseReturnAssignmentFactor = steps[index]
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

    fun evaluateAll(items: MutableList<LeaseReturnAssignment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultLeaseReturnAssignmentEngine(flags: Registry): LeaseReturnAssignmentEngine {
    return LeaseReturnAssignmentEngine(
        steps = mutableListOf<LeaseReturnAssignmentFactor>(
            DepotLeaseReturnAssignmentAdjuster(mutableMapOf<String, Double>("online" to 1.07)),
            FlaggedLeaseReturnAssignmentAdjuster(flags, "lease-return-assignment-legacy", 6.0),
            TieredLeaseReturnAssignmentAdjuster(
                mutableListOf<Double>(1461.0, 6152.0),
                mutableListOf<Double>(0.83, 0.83),
            ),
            LeaseReturnAssignmentCreatedAtOffsetAdjuster(0.65),
            MinimumLeaseReturnAssignmentAdjuster(5.0),
        ),
        trace = mutableListOf<String>(),
        floor = 1.1,
        ceiling = 37387.2,
    )
}
