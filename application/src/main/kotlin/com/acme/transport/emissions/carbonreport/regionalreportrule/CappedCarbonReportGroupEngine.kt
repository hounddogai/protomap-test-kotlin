package com.acme.transport.emissions.carbonreport.regionalreportrule

/** One step of the capped carbon report group calculation. */
interface CappedCarbonReportGroupStep {
    fun describe(): String
    fun adjust(item: CappedCarbonReportGroup, value: Double): Double
}

data class LaneCappedCarbonReportGroupAdjuster(
    var factors: MutableMap<String, Double>,
) : CappedCarbonReportGroupStep {
    override fun describe(): String = "by lane"

    override fun adjust(item: CappedCarbonReportGroup, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentCappedCarbonReportGroupAdjuster(var percent: Double) : CappedCarbonReportGroupStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CappedCarbonReportGroup, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredCappedCarbonReportGroupAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CappedCarbonReportGroupStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CappedCarbonReportGroup, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CappedCarbonReportGroupEndsAtOffsetAdjuster(var weight: Double) : CappedCarbonReportGroupStep {
    override fun describe(): String = "plus ends at times $weight"

    override fun adjust(item: CappedCarbonReportGroup, value: Double): Double {
        return value + item.endsAt.toDouble() * weight
    }
}

/** Runs the capped carbon report group steps in order and keeps a trace of each. */
data class CappedCarbonReportGroupEngine(
    var steps: MutableList<CappedCarbonReportGroupStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: CappedCarbonReportGroup): Double {
        var current = item.netTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: CappedCarbonReportGroupStep = steps[index]
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

    fun evaluateAll(items: MutableList<CappedCarbonReportGroup>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultCappedCarbonReportGroupEngine(): CappedCarbonReportGroupEngine {
    return CappedCarbonReportGroupEngine(
        steps = mutableListOf<CappedCarbonReportGroupStep>(
            LaneCappedCarbonReportGroupAdjuster(mutableMapOf<String, Double>("east" to 1.1)),
            PercentCappedCarbonReportGroupAdjuster(-6.6),
            TieredCappedCarbonReportGroupAdjuster(
                mutableListOf<Double>(111.0, 4284.0, 9837.0),
                mutableListOf<Double>(0.82, 0.93, 0.92),
            ),
            CappedCarbonReportGroupEndsAtOffsetAdjuster(0.93),
        ),
        trace = mutableListOf<String>(),
        floor = 3.6,
        ceiling = 60334.6,
    )
}
