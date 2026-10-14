package com.acme.transport.crewing.restbreak.schedule

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the pending rest break schedule calculation. */
interface PendingRestBreakScheduleFactor {
    fun describe(): String
    fun adjust(item: PendingRestBreakSchedule, value: Double): Double
}

data class FlaggedPendingRestBreakScheduleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PendingRestBreakScheduleFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PendingRestBreakSchedule, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingPendingRestBreakScheduleAdjuster(var step: Double) : PendingRestBreakScheduleFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: PendingRestBreakSchedule, value: Double): Double = roundTo(value, step)
}

data class ChannelPendingRestBreakScheduleAdjuster(
    var factors: MutableMap<String, Double>,
) : PendingRestBreakScheduleFactor {
    override fun describe(): String = "by channel"

    override fun adjust(item: PendingRestBreakSchedule, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PendingRestBreakScheduleFuelLitersOffsetAdjuster(
    var weight: Double,
) : PendingRestBreakScheduleFactor {
    override fun describe(): String = "plus fuel liters times $weight"

    override fun adjust(item: PendingRestBreakSchedule, value: Double): Double = value + item.fuelLiters * weight
}

data class MinimumPendingRestBreakScheduleAdjuster(var minimum: Double) : PendingRestBreakScheduleFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PendingRestBreakSchedule, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapPendingRestBreakScheduleAdjuster(var cap: Double) : PendingRestBreakScheduleFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: PendingRestBreakSchedule, value: Double): Double = minOf(value, cap)
}

/** Runs the pending rest break schedule steps in order and keeps a trace of each. */
data class PendingRestBreakScheduleEngine(
    var steps: MutableList<PendingRestBreakScheduleFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: PendingRestBreakSchedule): Double {
        var current = item.adjustedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<PendingRestBreakSchedule>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultPendingRestBreakScheduleEngine(flags: Registry): PendingRestBreakScheduleEngine {
    return PendingRestBreakScheduleEngine(
        steps = mutableListOf<PendingRestBreakScheduleFactor>(
            FlaggedPendingRestBreakScheduleAdjuster(flags, "pending-rest-break-schedule-primary", -8.8),
            RoundingPendingRestBreakScheduleAdjuster(5.0),
            ChannelPendingRestBreakScheduleAdjuster(mutableMapOf<String, Double>("south" to 0.77)),
            PendingRestBreakScheduleFuelLitersOffsetAdjuster(0.05),
            MinimumPendingRestBreakScheduleAdjuster(46.0),
            CapPendingRestBreakScheduleAdjuster(27129.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.2,
        ceiling = 61356.6,
    )
}
