package com.acme.transport.crewing.dutyperiod.milestone

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the scheduled duty period exception calculation. */
interface ScheduledDutyPeriodExceptionFactor {
    fun describe(): String
    fun adjust(item: ScheduledDutyPeriodException, value: Double): Double
}

data class RoundingScheduledDutyPeriodExceptionAdjuster(var step: Double) : ScheduledDutyPeriodExceptionFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ScheduledDutyPeriodException, value: Double): Double = roundTo(value, step)
}

data class FlaggedScheduledDutyPeriodExceptionAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ScheduledDutyPeriodExceptionFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ScheduledDutyPeriodException, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class ScheduledDutyPeriodExceptionDistanceKmOffsetAdjuster(
    var weight: Double,
) : ScheduledDutyPeriodExceptionFactor {
    override fun describe(): String = "plus distance km times $weight"

    override fun adjust(item: ScheduledDutyPeriodException, value: Double): Double = value + item.distanceKm * weight
}

data class TieredScheduledDutyPeriodExceptionAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ScheduledDutyPeriodExceptionFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ScheduledDutyPeriodException, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the scheduled duty period exception steps in order and keeps a trace of each. */
data class ScheduledDutyPeriodExceptionEngine(
    var steps: MutableList<ScheduledDutyPeriodExceptionFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: ScheduledDutyPeriodException): Double {
        var current = item.effectiveLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ScheduledDutyPeriodExceptionFactor = steps[index]
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

    fun evaluateAll(items: MutableList<ScheduledDutyPeriodException>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultScheduledDutyPeriodExceptionEngine(flags: Registry): ScheduledDutyPeriodExceptionEngine {
    return ScheduledDutyPeriodExceptionEngine(
        steps = mutableListOf<ScheduledDutyPeriodExceptionFactor>(
            RoundingScheduledDutyPeriodExceptionAdjuster(5.0),
            FlaggedScheduledDutyPeriodExceptionAdjuster(flags, "scheduled-duty-period-exception-batched", -2.2),
            ScheduledDutyPeriodExceptionDistanceKmOffsetAdjuster(1.85),
            TieredScheduledDutyPeriodExceptionAdjuster(
                mutableListOf<Double>(1974.0, 3387.0, 3994.0, 9216.0),
                mutableListOf<Double>(0.86, 0.87, 0.91, 0.87),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.6,
        ceiling = 86465.1,
    )
}
