package com.acme.transport.lastmile.parcel.parcelmapping

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the regional parcel schedule calculation. */
interface RegionalParcelScheduleStep {
    fun describe(): String
    fun adjust(item: RegionalParcelSchedule, value: Double): Double
}

data class TitleRegionalParcelScheduleAdjuster(
    var factors: MutableMap<String, Double>,
) : RegionalParcelScheduleStep {
    override fun describe(): String = "by title"

    override fun adjust(item: RegionalParcelSchedule, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentRegionalParcelScheduleAdjuster(var percent: Double) : RegionalParcelScheduleStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RegionalParcelSchedule, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingRegionalParcelScheduleAdjuster(var step: Double) : RegionalParcelScheduleStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RegionalParcelSchedule, value: Double): Double = roundTo(value, step)
}

data class FlaggedRegionalParcelScheduleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RegionalParcelScheduleStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RegionalParcelSchedule, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the regional parcel schedule steps in order and keeps a trace of each. */
data class RegionalParcelScheduleEngine(
    var steps: MutableList<RegionalParcelScheduleStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: RegionalParcelSchedule): Double {
        var current = item.effectiveValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RegionalParcelScheduleStep = steps[index]
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

    fun evaluateAll(items: MutableList<RegionalParcelSchedule>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultRegionalParcelScheduleEngine(flags: Registry): RegionalParcelScheduleEngine {
    return RegionalParcelScheduleEngine(
        steps = mutableListOf<RegionalParcelScheduleStep>(
            TitleRegionalParcelScheduleAdjuster(mutableMapOf<String, Double>("south" to 1.09)),
            PercentRegionalParcelScheduleAdjuster(19.3),
            RoundingRegionalParcelScheduleAdjuster(0.05),
            FlaggedRegionalParcelScheduleAdjuster(flags, "regional-parcel-schedule-lenient", 1.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.4,
        ceiling = 25283.3,
    )
}
