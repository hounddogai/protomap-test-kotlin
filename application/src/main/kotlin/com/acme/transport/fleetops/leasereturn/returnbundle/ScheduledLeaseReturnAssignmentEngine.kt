package com.acme.transport.fleetops.leasereturn.returnbundle

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the scheduled lease return assignment calculation. */
interface ScheduledLeaseReturnAssignmentFactor {
    fun describe(): String
    fun adjust(item: ScheduledLeaseReturnAssignment, value: Double): Double
}

data class TieredScheduledLeaseReturnAssignmentAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ScheduledLeaseReturnAssignmentFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ScheduledLeaseReturnAssignment, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentScheduledLeaseReturnAssignmentAdjuster(
    var percent: Double,
) : ScheduledLeaseReturnAssignmentFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ScheduledLeaseReturnAssignment, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedScheduledLeaseReturnAssignmentAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ScheduledLeaseReturnAssignmentFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ScheduledLeaseReturnAssignment, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class DepotScheduledLeaseReturnAssignmentAdjuster(
    var factors: MutableMap<String, Double>,
) : ScheduledLeaseReturnAssignmentFactor {
    override fun describe(): String = "by depot"

    override fun adjust(item: ScheduledLeaseReturnAssignment, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingScheduledLeaseReturnAssignmentAdjuster(
    var step: Double,
) : ScheduledLeaseReturnAssignmentFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ScheduledLeaseReturnAssignment, value: Double): Double = roundTo(value, step)
}

data class MinimumScheduledLeaseReturnAssignmentAdjuster(
    var minimum: Double,
) : ScheduledLeaseReturnAssignmentFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ScheduledLeaseReturnAssignment, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the scheduled lease return assignment steps in order and keeps a trace of each. */
data class ScheduledLeaseReturnAssignmentEngine(
    var steps: MutableList<ScheduledLeaseReturnAssignmentFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: ScheduledLeaseReturnAssignment): Double {
        var current = item.effectiveLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ScheduledLeaseReturnAssignmentFactor = steps[index]
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

    fun evaluateAll(items: MutableList<ScheduledLeaseReturnAssignment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultScheduledLeaseReturnAssignmentEngine(flags: Registry): ScheduledLeaseReturnAssignmentEngine {
    return ScheduledLeaseReturnAssignmentEngine(
        steps = mutableListOf<ScheduledLeaseReturnAssignmentFactor>(
            TieredScheduledLeaseReturnAssignmentAdjuster(
                mutableListOf<Double>(1015.0, 5234.0, 8674.0),
                mutableListOf<Double>(0.87, 1.1, 1.01),
            ),
            PercentScheduledLeaseReturnAssignmentAdjuster(16.7),
            FlaggedScheduledLeaseReturnAssignmentAdjuster(
                flags,
                "scheduled-lease-return-assignment-capped",
                -4.9,
            ),
            DepotScheduledLeaseReturnAssignmentAdjuster(
                mutableMapOf<String, Double>("central" to 0.83, "east" to 0.9),
            ),
            RoundingScheduledLeaseReturnAssignmentAdjuster(0.1),
            MinimumScheduledLeaseReturnAssignmentAdjuster(44.6),
        ),
        trace = mutableListOf<String>(),
        floor = 3.4,
        ceiling = 90491.5,
    )
}
