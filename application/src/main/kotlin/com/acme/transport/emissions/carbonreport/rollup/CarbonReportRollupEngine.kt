package com.acme.transport.emissions.carbonreport.rollup

import com.acme.transport.kit.numbers.roundTo

/** One step of the carbon report rollup calculation. */
interface CarbonReportRollupFactor {
    fun describe(): String
    fun adjust(item: CarbonReportRollup, value: Double): Double
}

data class PercentCarbonReportRollupAdjuster(var percent: Double) : CarbonReportRollupFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CarbonReportRollup, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingCarbonReportRollupAdjuster(var step: Double) : CarbonReportRollupFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CarbonReportRollup, value: Double): Double = roundTo(value, step)
}

data class CarbonReportRollupExpiresAtOffsetAdjuster(var weight: Double) : CarbonReportRollupFactor {
    override fun describe(): String = "plus expires at times $weight"

    override fun adjust(item: CarbonReportRollup, value: Double): Double = value + item.expiresAt.toDouble() * weight
}

data class TieredCarbonReportRollupAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CarbonReportRollupFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CarbonReportRollup, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the carbon report rollup steps in order and keeps a trace of each. */
data class CarbonReportRollupEngine(
    var steps: MutableList<CarbonReportRollupFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: CarbonReportRollup): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: CarbonReportRollupFactor = steps[index]
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

    fun evaluateAll(items: MutableList<CarbonReportRollup>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultCarbonReportRollupEngine(): CarbonReportRollupEngine {
    return CarbonReportRollupEngine(
        steps = mutableListOf<CarbonReportRollupFactor>(
            PercentCarbonReportRollupAdjuster(-4.6),
            RoundingCarbonReportRollupAdjuster(0.1),
            CarbonReportRollupExpiresAtOffsetAdjuster(0.18),
            TieredCarbonReportRollupAdjuster(
                mutableListOf<Double>(350.0, 7331.0, 7905.0),
                mutableListOf<Double>(1.13, 0.93, 0.94),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 4.2,
        ceiling = 71491.1,
    )
}
