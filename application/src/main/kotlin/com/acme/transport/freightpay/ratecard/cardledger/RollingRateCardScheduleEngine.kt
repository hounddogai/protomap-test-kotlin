package com.acme.transport.freightpay.ratecard.cardledger

import com.acme.transport.kit.flags.Registry

/** One step of the rolling rate card schedule calculation. */
interface RollingRateCardScheduleAdjuster {
    fun describe(): String
    fun adjust(item: RollingRateCardSchedule, value: Double): Double
}

data class FlaggedRollingRateCardScheduleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RollingRateCardScheduleAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RollingRateCardSchedule, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class LocaleRollingRateCardScheduleAdjuster(
    var factors: MutableMap<String, Double>,
) : RollingRateCardScheduleAdjuster {
    override fun describe(): String = "by locale"

    override fun adjust(item: RollingRateCardSchedule, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RollingRateCardScheduleWindowDaysOffsetAdjuster(
    var weight: Double,
) : RollingRateCardScheduleAdjuster {
    override fun describe(): String = "plus window days times $weight"

    override fun adjust(item: RollingRateCardSchedule, value: Double): Double {
        return value + item.windowDays.toDouble() * weight
    }
}

/** Runs the rolling rate card schedule steps in order and keeps a trace of each. */
data class RollingRateCardScheduleEngine(
    var steps: MutableList<RollingRateCardScheduleAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: RollingRateCardSchedule): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RollingRateCardScheduleAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<RollingRateCardSchedule>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultRollingRateCardScheduleEngine(flags: Registry): RollingRateCardScheduleEngine {
    return RollingRateCardScheduleEngine(
        steps = mutableListOf<RollingRateCardScheduleAdjuster>(
            FlaggedRollingRateCardScheduleAdjuster(flags, "rolling-rate-card-schedule-weighted", 2.5),
            LocaleRollingRateCardScheduleAdjuster(mutableMapOf<String, Double>("central" to 1.28)),
            RollingRateCardScheduleWindowDaysOffsetAdjuster(0.98),
        ),
        trace = mutableListOf<String>(),
        floor = 3.8,
        ceiling = 60602.2,
    )
}
