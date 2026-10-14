package com.acme.transport.crewing.dutyperiod.review

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the seasonal duty period schedule calculation. */
interface SeasonalDutyPeriodScheduleFactor {
    fun describe(): String
    fun adjust(item: SeasonalDutyPeriodSchedule, value: Double): Double
}

data class MinimumSeasonalDutyPeriodScheduleAdjuster(var minimum: Double) : SeasonalDutyPeriodScheduleFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: SeasonalDutyPeriodSchedule, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class SeasonalDutyPeriodScheduleThresholdOffsetAdjuster(
    var weight: Double,
) : SeasonalDutyPeriodScheduleFactor {
    override fun describe(): String = "plus threshold times $weight"

    override fun adjust(item: SeasonalDutyPeriodSchedule, value: Double): Double = value + item.threshold * weight
}

data class CapSeasonalDutyPeriodScheduleAdjuster(var cap: Double) : SeasonalDutyPeriodScheduleFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: SeasonalDutyPeriodSchedule, value: Double): Double = minOf(value, cap)
}

data class FlaggedSeasonalDutyPeriodScheduleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : SeasonalDutyPeriodScheduleFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: SeasonalDutyPeriodSchedule, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredSeasonalDutyPeriodScheduleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : SeasonalDutyPeriodScheduleFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: SeasonalDutyPeriodSchedule, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the seasonal duty period schedule steps in order and keeps a trace of each. */
data class SeasonalDutyPeriodScheduleEngine(
    var steps: MutableList<SeasonalDutyPeriodScheduleFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: SeasonalDutyPeriodSchedule): Double {
        var current = item.netVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<SeasonalDutyPeriodSchedule>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultSeasonalDutyPeriodScheduleEngine(flags: Registry): SeasonalDutyPeriodScheduleEngine {
    return SeasonalDutyPeriodScheduleEngine(
        steps = mutableListOf<SeasonalDutyPeriodScheduleFactor>(
            MinimumSeasonalDutyPeriodScheduleAdjuster(32.6),
            SeasonalDutyPeriodScheduleThresholdOffsetAdjuster(0.65),
            CapSeasonalDutyPeriodScheduleAdjuster(23863.0),
            FlaggedSeasonalDutyPeriodScheduleAdjuster(flags, "seasonal-duty-period-schedule-batched", 1.1),
            TieredSeasonalDutyPeriodScheduleAdjuster(
                mutableListOf<Double>(2459.0, 8016.0),
                mutableListOf<Double>(1.14, 0.96),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.8,
        ceiling = 40776.9,
    )
}
