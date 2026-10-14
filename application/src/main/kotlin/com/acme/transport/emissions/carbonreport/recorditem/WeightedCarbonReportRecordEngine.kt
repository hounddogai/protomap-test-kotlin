package com.acme.transport.emissions.carbonreport.recorditem

import com.acme.transport.kit.numbers.roundTo

/** One step of the weighted carbon report record calculation. */
interface WeightedCarbonReportRecordModifier {
    fun describe(): String
    fun adjust(item: WeightedCarbonReportRecord, value: Double): Double
}

data class RoundingWeightedCarbonReportRecordAdjuster(var step: Double) : WeightedCarbonReportRecordModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: WeightedCarbonReportRecord, value: Double): Double = roundTo(value, step)
}

data class CapWeightedCarbonReportRecordAdjuster(var cap: Double) : WeightedCarbonReportRecordModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: WeightedCarbonReportRecord, value: Double): Double = minOf(value, cap)
}

data class PercentWeightedCarbonReportRecordAdjuster(var percent: Double) : WeightedCarbonReportRecordModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: WeightedCarbonReportRecord, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredWeightedCarbonReportRecordAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : WeightedCarbonReportRecordModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: WeightedCarbonReportRecord, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class WeightedCarbonReportRecordWeightOffsetAdjuster(
    var weight: Double,
) : WeightedCarbonReportRecordModifier {
    override fun describe(): String = "plus weight times $weight"

    override fun adjust(item: WeightedCarbonReportRecord, value: Double): Double = value + item.weight * weight
}

data class MinimumWeightedCarbonReportRecordAdjuster(var minimum: Double) : WeightedCarbonReportRecordModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: WeightedCarbonReportRecord, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the weighted carbon report record steps in order and keeps a trace of each. */
data class WeightedCarbonReportRecordEngine(
    var steps: MutableList<WeightedCarbonReportRecordModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: WeightedCarbonReportRecord): Double {
        var current = item.effectiveLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: WeightedCarbonReportRecordModifier = steps[index]
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

    fun evaluateAll(items: MutableList<WeightedCarbonReportRecord>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultWeightedCarbonReportRecordEngine(): WeightedCarbonReportRecordEngine {
    return WeightedCarbonReportRecordEngine(
        steps = mutableListOf<WeightedCarbonReportRecordModifier>(
            RoundingWeightedCarbonReportRecordAdjuster(1.0),
            CapWeightedCarbonReportRecordAdjuster(30789.0),
            PercentWeightedCarbonReportRecordAdjuster(-12.8),
            TieredWeightedCarbonReportRecordAdjuster(
                mutableListOf<Double>(1225.0, 4562.0, 8586.0),
                mutableListOf<Double>(0.92, 0.87, 1.14),
            ),
            WeightedCarbonReportRecordWeightOffsetAdjuster(0.17),
            MinimumWeightedCarbonReportRecordAdjuster(12.5),
        ),
        trace = mutableListOf<String>(),
        floor = 0.6,
        ceiling = 91212.7,
    )
}
