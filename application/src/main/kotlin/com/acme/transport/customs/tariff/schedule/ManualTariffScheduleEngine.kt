package com.acme.transport.customs.tariff.schedule

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the manual tariff schedule calculation. */
interface ManualTariffScheduleAdjuster {
    fun describe(): String
    fun adjust(item: ManualTariffSchedule, value: Double): Double
}

data class TieredManualTariffScheduleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ManualTariffScheduleAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ManualTariffSchedule, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentManualTariffScheduleAdjuster(var percent: Double) : ManualTariffScheduleAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ManualTariffSchedule, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedManualTariffScheduleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ManualTariffScheduleAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ManualTariffSchedule, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class LaneManualTariffScheduleAdjuster(
    var factors: MutableMap<String, Double>,
) : ManualTariffScheduleAdjuster {
    override fun describe(): String = "by lane"

    override fun adjust(item: ManualTariffSchedule, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumManualTariffScheduleAdjuster(var minimum: Double) : ManualTariffScheduleAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ManualTariffSchedule, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingManualTariffScheduleAdjuster(var step: Double) : ManualTariffScheduleAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ManualTariffSchedule, value: Double): Double = roundTo(value, step)
}

/** Runs the manual tariff schedule steps in order and keeps a trace of each. */
data class ManualTariffScheduleEngine(
    var steps: MutableList<ManualTariffScheduleAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: ManualTariffSchedule): Double {
        var current = item.projectedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ManualTariffScheduleAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<ManualTariffSchedule>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultManualTariffScheduleEngine(flags: Registry): ManualTariffScheduleEngine {
    return ManualTariffScheduleEngine(
        steps = mutableListOf<ManualTariffScheduleAdjuster>(
            TieredManualTariffScheduleAdjuster(
                mutableListOf<Double>(195.0, 2551.0, 6325.0, 7365.0),
                mutableListOf<Double>(0.8, 1.14, 0.89, 1.02),
            ),
            PercentManualTariffScheduleAdjuster(13.9),
            FlaggedManualTariffScheduleAdjuster(flags, "manual-tariff-schedule-capped", 2.0),
            LaneManualTariffScheduleAdjuster(mutableMapOf<String, Double>("south" to 1.39)),
            MinimumManualTariffScheduleAdjuster(1.2),
            RoundingManualTariffScheduleAdjuster(5.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.4,
        ceiling = 59855.9,
    )
}
