package com.acme.transport.emissions.carbonreport.variance

import com.acme.transport.kit.flags.Registry

/** One step of the carbon report segment calculation. */
interface CarbonReportSegmentModifier {
    fun describe(): String
    fun adjust(item: CarbonReportSegment, value: Double): Double
}

data class FlaggedCarbonReportSegmentAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CarbonReportSegmentModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CarbonReportSegment, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredCarbonReportSegmentAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CarbonReportSegmentModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CarbonReportSegment, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumCarbonReportSegmentAdjuster(var minimum: Double) : CarbonReportSegmentModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CarbonReportSegment, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapCarbonReportSegmentAdjuster(var cap: Double) : CarbonReportSegmentModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CarbonReportSegment, value: Double): Double = minOf(value, cap)
}

data class SourceCarbonReportSegmentAdjuster(
    var factors: MutableMap<String, Double>,
) : CarbonReportSegmentModifier {
    override fun describe(): String = "by source"

    override fun adjust(item: CarbonReportSegment, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CarbonReportSegmentQuantityOffsetAdjuster(var weight: Double) : CarbonReportSegmentModifier {
    override fun describe(): String = "plus quantity times $weight"

    override fun adjust(item: CarbonReportSegment, value: Double): Double = value + item.quantity.toDouble() * weight
}

/** Runs the carbon report segment steps in order and keeps a trace of each. */
data class CarbonReportSegmentEngine(
    var steps: MutableList<CarbonReportSegmentModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: CarbonReportSegment): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: CarbonReportSegmentModifier = steps[index]
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

    fun evaluateAll(items: MutableList<CarbonReportSegment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultCarbonReportSegmentEngine(flags: Registry): CarbonReportSegmentEngine {
    return CarbonReportSegmentEngine(
        steps = mutableListOf<CarbonReportSegmentModifier>(
            FlaggedCarbonReportSegmentAdjuster(flags, "carbon-report-segment-adaptive", -7.2),
            TieredCarbonReportSegmentAdjuster(
                mutableListOf<Double>(3607.0, 4952.0, 6498.0),
                mutableListOf<Double>(1.09, 0.82, 0.91),
            ),
            MinimumCarbonReportSegmentAdjuster(14.0),
            CapCarbonReportSegmentAdjuster(40006.0),
            SourceCarbonReportSegmentAdjuster(mutableMapOf<String, Double>("south" to 0.7)),
            CarbonReportSegmentQuantityOffsetAdjuster(1.29),
        ),
        trace = mutableListOf<String>(),
        floor = 4.7,
        ceiling = 84677.4,
    )
}
