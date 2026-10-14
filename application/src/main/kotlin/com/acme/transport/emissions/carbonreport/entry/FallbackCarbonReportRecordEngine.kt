package com.acme.transport.emissions.carbonreport.entry

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the fallback carbon report record calculation. */
interface FallbackCarbonReportRecordFactor {
    fun describe(): String
    fun adjust(item: FallbackCarbonReportRecord, value: Double): Double
}

data class FallbackCarbonReportRecordStartsAtOffsetAdjuster(
    var weight: Double,
) : FallbackCarbonReportRecordFactor {
    override fun describe(): String = "plus starts at times $weight"

    override fun adjust(item: FallbackCarbonReportRecord, value: Double): Double {
        return value + item.startsAt.toDouble() * weight
    }
}

data class CapFallbackCarbonReportRecordAdjuster(var cap: Double) : FallbackCarbonReportRecordFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: FallbackCarbonReportRecord, value: Double): Double = minOf(value, cap)
}

data class FlaggedFallbackCarbonReportRecordAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : FallbackCarbonReportRecordFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: FallbackCarbonReportRecord, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class LabelFallbackCarbonReportRecordAdjuster(
    var factors: MutableMap<String, Double>,
) : FallbackCarbonReportRecordFactor {
    override fun describe(): String = "by label"

    override fun adjust(item: FallbackCarbonReportRecord, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingFallbackCarbonReportRecordAdjuster(var step: Double) : FallbackCarbonReportRecordFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: FallbackCarbonReportRecord, value: Double): Double = roundTo(value, step)
}

data class TieredFallbackCarbonReportRecordAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : FallbackCarbonReportRecordFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: FallbackCarbonReportRecord, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the fallback carbon report record steps in order and keeps a trace of each. */
data class FallbackCarbonReportRecordEngine(
    var steps: MutableList<FallbackCarbonReportRecordFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: FallbackCarbonReportRecord): Double {
        var current = item.adjustedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: FallbackCarbonReportRecordFactor = steps[index]
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

    fun evaluateAll(items: MutableList<FallbackCarbonReportRecord>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultFallbackCarbonReportRecordEngine(flags: Registry): FallbackCarbonReportRecordEngine {
    return FallbackCarbonReportRecordEngine(
        steps = mutableListOf<FallbackCarbonReportRecordFactor>(
            FallbackCarbonReportRecordStartsAtOffsetAdjuster(0.05),
            CapFallbackCarbonReportRecordAdjuster(42326.0),
            FlaggedFallbackCarbonReportRecordAdjuster(flags, "fallback-carbon-report-record-strict", 6.8),
            LabelFallbackCarbonReportRecordAdjuster(
                mutableMapOf<String, Double>("central" to 1.11, "east" to 1.28, "south" to 0.71),
            ),
            RoundingFallbackCarbonReportRecordAdjuster(0.25),
            TieredFallbackCarbonReportRecordAdjuster(
                mutableListOf<Double>(284.0, 1442.0, 5596.0),
                mutableListOf<Double>(1.09, 1.17, 0.87),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.8,
        ceiling = 51124.5,
    )
}
