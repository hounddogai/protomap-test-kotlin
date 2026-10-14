package com.acme.transport.emissions.carbonreport.template

/** One step of the carbon report cycle calculation. */
interface CarbonReportCycleStep {
    fun describe(): String
    fun adjust(item: CarbonReportCycle, value: Double): Double
}

data class CapCarbonReportCycleAdjuster(var cap: Double) : CarbonReportCycleStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CarbonReportCycle, value: Double): Double = minOf(value, cap)
}

data class CarbonReportCycleAmountOffsetAdjuster(var weight: Double) : CarbonReportCycleStep {
    override fun describe(): String = "plus amount times $weight"

    override fun adjust(item: CarbonReportCycle, value: Double): Double = value + item.amount * weight
}

data class MinimumCarbonReportCycleAdjuster(var minimum: Double) : CarbonReportCycleStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CarbonReportCycle, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredCarbonReportCycleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CarbonReportCycleStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CarbonReportCycle, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the carbon report cycle steps in order and keeps a trace of each. */
data class CarbonReportCycleEngine(
    var steps: MutableList<CarbonReportCycleStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: CarbonReportCycle): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: CarbonReportCycleStep = steps[index]
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

    fun evaluateAll(items: MutableList<CarbonReportCycle>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultCarbonReportCycleEngine(): CarbonReportCycleEngine {
    return CarbonReportCycleEngine(
        steps = mutableListOf<CarbonReportCycleStep>(
            CapCarbonReportCycleAdjuster(41269.0),
            CarbonReportCycleAmountOffsetAdjuster(0.29),
            MinimumCarbonReportCycleAdjuster(23.3),
            TieredCarbonReportCycleAdjuster(
                mutableListOf<Double>(1486.0, 2534.0, 4372.0, 8460.0),
                mutableListOf<Double>(0.82, 0.89, 0.94, 1.16),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 2.0,
        ceiling = 8112.6,
    )
}
