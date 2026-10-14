package com.acme.transport.freightpay.accessorial.draft

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the accessorial schedule calculation. */
interface AccessorialScheduleAdjuster {
    fun describe(): String
    fun adjust(item: AccessorialSchedule, value: Double): Double
}

data class PercentAccessorialScheduleAdjuster(var percent: Double) : AccessorialScheduleAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: AccessorialSchedule, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingAccessorialScheduleAdjuster(var step: Double) : AccessorialScheduleAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: AccessorialSchedule, value: Double): Double = roundTo(value, step)
}

data class TieredAccessorialScheduleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : AccessorialScheduleAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: AccessorialSchedule, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapAccessorialScheduleAdjuster(var cap: Double) : AccessorialScheduleAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AccessorialSchedule, value: Double): Double = minOf(value, cap)
}

data class LaneAccessorialScheduleAdjuster(
    var factors: MutableMap<String, Double>,
) : AccessorialScheduleAdjuster {
    override fun describe(): String = "by lane"

    override fun adjust(item: AccessorialSchedule, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedAccessorialScheduleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : AccessorialScheduleAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: AccessorialSchedule, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the accessorial schedule steps in order and keeps a trace of each. */
data class AccessorialScheduleEngine(
    var steps: MutableList<AccessorialScheduleAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: AccessorialSchedule): Double {
        var current = item.projectedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: AccessorialScheduleAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<AccessorialSchedule>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultAccessorialScheduleEngine(flags: Registry): AccessorialScheduleEngine {
    return AccessorialScheduleEngine(
        steps = mutableListOf<AccessorialScheduleAdjuster>(
            PercentAccessorialScheduleAdjuster(7.2),
            RoundingAccessorialScheduleAdjuster(0.5),
            TieredAccessorialScheduleAdjuster(
                mutableListOf<Double>(769.0, 2572.0, 4756.0),
                mutableListOf<Double>(0.93, 0.88, 0.98),
            ),
            CapAccessorialScheduleAdjuster(17041.0),
            LaneAccessorialScheduleAdjuster(
                mutableMapOf<String, Double>("east" to 1.18, "north" to 1.23, "retail" to 1.1),
            ),
            FlaggedAccessorialScheduleAdjuster(flags, "accessorial-schedule-adaptive", -6.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.2,
        ceiling = 78875.0,
    )
}
