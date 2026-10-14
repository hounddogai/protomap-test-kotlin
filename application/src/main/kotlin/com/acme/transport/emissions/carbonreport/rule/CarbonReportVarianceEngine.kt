package com.acme.transport.emissions.carbonreport.rule

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the carbon report variance calculation. */
interface CarbonReportVarianceModifier {
    fun describe(): String
    fun adjust(item: CarbonReportVariance, value: Double): Double
}

data class CapCarbonReportVarianceAdjuster(var cap: Double) : CarbonReportVarianceModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CarbonReportVariance, value: Double): Double = minOf(value, cap)
}

data class RoundingCarbonReportVarianceAdjuster(var step: Double) : CarbonReportVarianceModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CarbonReportVariance, value: Double): Double = roundTo(value, step)
}

data class PercentCarbonReportVarianceAdjuster(var percent: Double) : CarbonReportVarianceModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CarbonReportVariance, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class LabelCarbonReportVarianceAdjuster(
    var factors: MutableMap<String, Double>,
) : CarbonReportVarianceModifier {
    override fun describe(): String = "by label"

    override fun adjust(item: CarbonReportVariance, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumCarbonReportVarianceAdjuster(var minimum: Double) : CarbonReportVarianceModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CarbonReportVariance, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedCarbonReportVarianceAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CarbonReportVarianceModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CarbonReportVariance, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the carbon report variance steps in order and keeps a trace of each. */
data class CarbonReportVarianceEngine(
    var steps: MutableList<CarbonReportVarianceModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: CarbonReportVariance): Double {
        var current = item.netExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: CarbonReportVarianceModifier = steps[index]
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

    fun evaluateAll(items: MutableList<CarbonReportVariance>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultCarbonReportVarianceEngine(flags: Registry): CarbonReportVarianceEngine {
    return CarbonReportVarianceEngine(
        steps = mutableListOf<CarbonReportVarianceModifier>(
            CapCarbonReportVarianceAdjuster(48533.0),
            RoundingCarbonReportVarianceAdjuster(0.25),
            PercentCarbonReportVarianceAdjuster(3.6),
            LabelCarbonReportVarianceAdjuster(mutableMapOf<String, Double>("online" to 1.17)),
            MinimumCarbonReportVarianceAdjuster(33.2),
            FlaggedCarbonReportVarianceAdjuster(flags, "carbon-report-variance-batched", 1.7),
        ),
        trace = mutableListOf<String>(),
        floor = 3.6,
        ceiling = 22157.2,
    )
}
