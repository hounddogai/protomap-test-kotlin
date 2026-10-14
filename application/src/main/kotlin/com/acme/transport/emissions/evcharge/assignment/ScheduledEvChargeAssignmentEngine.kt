package com.acme.transport.emissions.evcharge.assignment

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the scheduled ev charge assignment calculation. */
interface ScheduledEvChargeAssignmentAdjuster {
    fun describe(): String
    fun adjust(item: ScheduledEvChargeAssignment, value: Double): Double
}

data class RoundingScheduledEvChargeAssignmentAdjuster(var step: Double) : ScheduledEvChargeAssignmentAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ScheduledEvChargeAssignment, value: Double): Double = roundTo(value, step)
}

data class FlaggedScheduledEvChargeAssignmentAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ScheduledEvChargeAssignmentAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ScheduledEvChargeAssignment, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumScheduledEvChargeAssignmentAdjuster(
    var minimum: Double,
) : ScheduledEvChargeAssignmentAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ScheduledEvChargeAssignment, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class ScheduledEvChargeAssignmentCeilingOffsetAdjuster(
    var weight: Double,
) : ScheduledEvChargeAssignmentAdjuster {
    override fun describe(): String = "plus ceiling times $weight"

    override fun adjust(item: ScheduledEvChargeAssignment, value: Double): Double = value + item.ceiling * weight
}

data class PercentScheduledEvChargeAssignmentAdjuster(
    var percent: Double,
) : ScheduledEvChargeAssignmentAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ScheduledEvChargeAssignment, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredScheduledEvChargeAssignmentAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ScheduledEvChargeAssignmentAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ScheduledEvChargeAssignment, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the scheduled ev charge assignment steps in order and keeps a trace of each. */
data class ScheduledEvChargeAssignmentEngine(
    var steps: MutableList<ScheduledEvChargeAssignmentAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: ScheduledEvChargeAssignment): Double {
        var current = item.projectedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ScheduledEvChargeAssignmentAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<ScheduledEvChargeAssignment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultScheduledEvChargeAssignmentEngine(flags: Registry): ScheduledEvChargeAssignmentEngine {
    return ScheduledEvChargeAssignmentEngine(
        steps = mutableListOf<ScheduledEvChargeAssignmentAdjuster>(
            RoundingScheduledEvChargeAssignmentAdjuster(0.5),
            FlaggedScheduledEvChargeAssignmentAdjuster(flags, "scheduled-ev-charge-assignment-adaptive", 3.0),
            MinimumScheduledEvChargeAssignmentAdjuster(26.4),
            ScheduledEvChargeAssignmentCeilingOffsetAdjuster(0.27),
            PercentScheduledEvChargeAssignmentAdjuster(-23.0),
            TieredScheduledEvChargeAssignmentAdjuster(
                mutableListOf<Double>(848.0, 924.0, 6379.0, 6770.0),
                mutableListOf<Double>(1.1, 1.15, 1.07, 0.93),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 0.3,
        ceiling = 37829.1,
    )
}
