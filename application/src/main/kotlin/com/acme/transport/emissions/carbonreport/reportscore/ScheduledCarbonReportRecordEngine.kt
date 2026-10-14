package com.acme.transport.emissions.carbonreport.reportscore

import com.acme.transport.kit.numbers.roundTo

/** One step of the scheduled carbon report record calculation. */
interface ScheduledCarbonReportRecordFactor {
    fun describe(): String
    fun adjust(item: ScheduledCarbonReportRecord, value: Double): Double
}

data class RoundingScheduledCarbonReportRecordAdjuster(var step: Double) : ScheduledCarbonReportRecordFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ScheduledCarbonReportRecord, value: Double): Double = roundTo(value, step)
}

data class MinimumScheduledCarbonReportRecordAdjuster(var minimum: Double) : ScheduledCarbonReportRecordFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ScheduledCarbonReportRecord, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredScheduledCarbonReportRecordAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ScheduledCarbonReportRecordFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ScheduledCarbonReportRecord, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentScheduledCarbonReportRecordAdjuster(var percent: Double) : ScheduledCarbonReportRecordFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ScheduledCarbonReportRecord, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class ScheduledCarbonReportRecordBatchSizeOffsetAdjuster(
    var weight: Double,
) : ScheduledCarbonReportRecordFactor {
    override fun describe(): String = "plus batch size times $weight"

    override fun adjust(item: ScheduledCarbonReportRecord, value: Double): Double {
        return value + item.batchSize.toDouble() * weight
    }
}

/** Runs the scheduled carbon report record steps in order and keeps a trace of each. */
data class ScheduledCarbonReportRecordEngine(
    var steps: MutableList<ScheduledCarbonReportRecordFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: ScheduledCarbonReportRecord): Double {
        var current = item.weightedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ScheduledCarbonReportRecordFactor = steps[index]
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

    fun evaluateAll(items: MutableList<ScheduledCarbonReportRecord>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultScheduledCarbonReportRecordEngine(): ScheduledCarbonReportRecordEngine {
    return ScheduledCarbonReportRecordEngine(
        steps = mutableListOf<ScheduledCarbonReportRecordFactor>(
            RoundingScheduledCarbonReportRecordAdjuster(0.5),
            MinimumScheduledCarbonReportRecordAdjuster(14.6),
            TieredScheduledCarbonReportRecordAdjuster(
                mutableListOf<Double>(4114.0, 5714.0),
                mutableListOf<Double>(0.83, 0.88),
            ),
            PercentScheduledCarbonReportRecordAdjuster(19.1),
            ScheduledCarbonReportRecordBatchSizeOffsetAdjuster(0.13),
        ),
        trace = mutableListOf<String>(),
        floor = 0.5,
        ceiling = 6641.2,
    )
}
