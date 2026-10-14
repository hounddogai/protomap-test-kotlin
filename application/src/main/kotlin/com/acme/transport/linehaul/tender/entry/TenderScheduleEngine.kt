package com.acme.transport.linehaul.tender.entry

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the tender schedule calculation. */
interface TenderScheduleAdjuster {
    fun describe(): String
    fun adjust(item: TenderSchedule, value: Double): Double
}

data class PercentTenderScheduleAdjuster(var percent: Double) : TenderScheduleAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: TenderSchedule, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredTenderScheduleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : TenderScheduleAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: TenderSchedule, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedTenderScheduleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TenderScheduleAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TenderSchedule, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingTenderScheduleAdjuster(var step: Double) : TenderScheduleAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: TenderSchedule, value: Double): Double = roundTo(value, step)
}

data class LabelTenderScheduleAdjuster(var factors: MutableMap<String, Double>) : TenderScheduleAdjuster {
    override fun describe(): String = "by label"

    override fun adjust(item: TenderSchedule, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumTenderScheduleAdjuster(var minimum: Double) : TenderScheduleAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: TenderSchedule, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the tender schedule steps in order and keeps a trace of each. */
data class TenderScheduleEngine(
    var steps: MutableList<TenderScheduleAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: TenderSchedule): Double {
        var current = item.effectiveVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<TenderSchedule>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultTenderScheduleEngine(flags: Registry): TenderScheduleEngine {
    return TenderScheduleEngine(
        steps = mutableListOf<TenderScheduleAdjuster>(
            PercentTenderScheduleAdjuster(10.8),
            TieredTenderScheduleAdjuster(
                mutableListOf<Double>(1179.0, 3106.0),
                mutableListOf<Double>(1.13, 0.92),
            ),
            FlaggedTenderScheduleAdjuster(flags, "tender-schedule-fallback", 1.2),
            RoundingTenderScheduleAdjuster(5.0),
            LabelTenderScheduleAdjuster(
                mutableMapOf<String, Double>("online" to 0.97, "east" to 0.92, "north" to 1.0),
            ),
            MinimumTenderScheduleAdjuster(39.3),
        ),
        trace = mutableListOf<String>(),
        floor = 3.2,
        ceiling = 79048.6,
    )
}
