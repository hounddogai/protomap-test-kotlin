package com.acme.transport.fleetops.leasereturn.schedule

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the lease return schedule calculation. */
interface LeaseReturnScheduleFactor {
    fun describe(): String
    fun adjust(item: LeaseReturnSchedule, value: Double): Double
}

data class FlaggedLeaseReturnScheduleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LeaseReturnScheduleFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LeaseReturnSchedule, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class LocaleLeaseReturnScheduleAdjuster(
    var factors: MutableMap<String, Double>,
) : LeaseReturnScheduleFactor {
    override fun describe(): String = "by locale"

    override fun adjust(item: LeaseReturnSchedule, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingLeaseReturnScheduleAdjuster(var step: Double) : LeaseReturnScheduleFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LeaseReturnSchedule, value: Double): Double = roundTo(value, step)
}

data class TieredLeaseReturnScheduleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LeaseReturnScheduleFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LeaseReturnSchedule, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the lease return schedule steps in order and keeps a trace of each. */
data class LeaseReturnScheduleEngine(
    var steps: MutableList<LeaseReturnScheduleFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: LeaseReturnSchedule): Double {
        var current = item.effectiveValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LeaseReturnScheduleFactor = steps[index]
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

    fun evaluateAll(items: MutableList<LeaseReturnSchedule>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultLeaseReturnScheduleEngine(flags: Registry): LeaseReturnScheduleEngine {
    return LeaseReturnScheduleEngine(
        steps = mutableListOf<LeaseReturnScheduleFactor>(
            FlaggedLeaseReturnScheduleAdjuster(flags, "lease-return-schedule-regional", 3.5),
            LocaleLeaseReturnScheduleAdjuster(
                mutableMapOf<String, Double>("east" to 1.38, "west" to 1.11, "central" to 1.06),
            ),
            RoundingLeaseReturnScheduleAdjuster(5.0),
            TieredLeaseReturnScheduleAdjuster(
                mutableListOf<Double>(2913.0, 5406.0),
                mutableListOf<Double>(0.93, 1.18),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 0.5,
        ceiling = 55708.6,
    )
}
