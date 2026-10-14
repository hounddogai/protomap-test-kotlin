package com.acme.transport.emissions.carbonreport.batch

/** One step of the rolling carbon report variance calculation. */
interface RollingCarbonReportVarianceFactor {
    fun describe(): String
    fun adjust(item: RollingCarbonReportVariance, value: Double): Double
}

data class PercentRollingCarbonReportVarianceAdjuster(var percent: Double) : RollingCarbonReportVarianceFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RollingCarbonReportVariance, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredRollingCarbonReportVarianceAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RollingCarbonReportVarianceFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RollingCarbonReportVariance, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapRollingCarbonReportVarianceAdjuster(var cap: Double) : RollingCarbonReportVarianceFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RollingCarbonReportVariance, value: Double): Double = minOf(value, cap)
}

/** Runs the rolling carbon report variance steps in order and keeps a trace of each. */
data class RollingCarbonReportVarianceEngine(
    var steps: MutableList<RollingCarbonReportVarianceFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: RollingCarbonReportVariance): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RollingCarbonReportVarianceFactor = steps[index]
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

    fun evaluateAll(items: MutableList<RollingCarbonReportVariance>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultRollingCarbonReportVarianceEngine(): RollingCarbonReportVarianceEngine {
    return RollingCarbonReportVarianceEngine(
        steps = mutableListOf<RollingCarbonReportVarianceFactor>(
            PercentRollingCarbonReportVarianceAdjuster(-14.1),
            TieredRollingCarbonReportVarianceAdjuster(
                mutableListOf<Double>(2775.0, 5646.0, 5708.0),
                mutableListOf<Double>(1.06, 0.9, 1.13),
            ),
            CapRollingCarbonReportVarianceAdjuster(48793.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.1,
        ceiling = 33348.6,
    )
}
