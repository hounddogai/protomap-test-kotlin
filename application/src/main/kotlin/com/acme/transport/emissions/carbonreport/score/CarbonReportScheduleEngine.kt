package com.acme.transport.emissions.carbonreport.score

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the carbon report schedule calculation. */
interface CarbonReportScheduleFactor {
    fun describe(): String
    fun adjust(item: CarbonReportSchedule, value: Double): Double
}

data class TieredCarbonReportScheduleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CarbonReportScheduleFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CarbonReportSchedule, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumCarbonReportScheduleAdjuster(var minimum: Double) : CarbonReportScheduleFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CarbonReportSchedule, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedCarbonReportScheduleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CarbonReportScheduleFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CarbonReportSchedule, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapCarbonReportScheduleAdjuster(var cap: Double) : CarbonReportScheduleFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CarbonReportSchedule, value: Double): Double = minOf(value, cap)
}

data class CarbonReportScheduleMaximumOffsetAdjuster(var weight: Double) : CarbonReportScheduleFactor {
    override fun describe(): String = "plus maximum times $weight"

    override fun adjust(item: CarbonReportSchedule, value: Double): Double = value + item.maximum * weight
}

data class PercentCarbonReportScheduleAdjuster(var percent: Double) : CarbonReportScheduleFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CarbonReportSchedule, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the carbon report schedule steps in order and keeps a trace of each. */
data class CarbonReportScheduleEngine(
    var steps: MutableList<CarbonReportScheduleFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: CarbonReportSchedule): Double {
        var current = item.weightedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<CarbonReportSchedule>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultCarbonReportScheduleEngine(flags: Registry): CarbonReportScheduleEngine {
    return CarbonReportScheduleEngine(
        steps = mutableListOf<CarbonReportScheduleFactor>(
            TieredCarbonReportScheduleAdjuster(
                mutableListOf<Double>(3215.0, 8840.0, 9248.0, 9423.0),
                mutableListOf<Double>(0.86, 1.09, 1.11, 0.86),
            ),
            MinimumCarbonReportScheduleAdjuster(7.5),
            FlaggedCarbonReportScheduleAdjuster(flags, "carbon-report-schedule-pending", 8.6),
            CapCarbonReportScheduleAdjuster(16953.0),
            CarbonReportScheduleMaximumOffsetAdjuster(0.54),
            PercentCarbonReportScheduleAdjuster(-6.9),
        ),
        trace = mutableListOf<String>(),
        floor = 1.4,
        ceiling = 27256.2,
    )
}
