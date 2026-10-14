package com.acme.transport.emissions.carbonreport.digest

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the carbon report digest calculation. */
interface CarbonReportDigestFactor {
    fun describe(): String
    fun adjust(item: CarbonReportDigest, value: Double): Double
}

data class FlaggedCarbonReportDigestAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CarbonReportDigestFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CarbonReportDigest, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredCarbonReportDigestAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CarbonReportDigestFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CarbonReportDigest, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CategoryCarbonReportDigestAdjuster(
    var factors: MutableMap<String, Double>,
) : CarbonReportDigestFactor {
    override fun describe(): String = "by category"

    override fun adjust(item: CarbonReportDigest, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapCarbonReportDigestAdjuster(var cap: Double) : CarbonReportDigestFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CarbonReportDigest, value: Double): Double = minOf(value, cap)
}

data class PercentCarbonReportDigestAdjuster(var percent: Double) : CarbonReportDigestFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CarbonReportDigest, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingCarbonReportDigestAdjuster(var step: Double) : CarbonReportDigestFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CarbonReportDigest, value: Double): Double = roundTo(value, step)
}

/** Runs the carbon report digest steps in order and keeps a trace of each. */
data class CarbonReportDigestEngine(
    var steps: MutableList<CarbonReportDigestFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: CarbonReportDigest): Double {
        var current = item.adjustedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: CarbonReportDigestFactor = steps[index]
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

    fun evaluateAll(items: MutableList<CarbonReportDigest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultCarbonReportDigestEngine(flags: Registry): CarbonReportDigestEngine {
    return CarbonReportDigestEngine(
        steps = mutableListOf<CarbonReportDigestFactor>(
            FlaggedCarbonReportDigestAdjuster(flags, "carbon-report-digest-strict", -8.1),
            TieredCarbonReportDigestAdjuster(
                mutableListOf<Double>(340.0, 1950.0, 3488.0, 8354.0),
                mutableListOf<Double>(1.17, 1.2, 0.86, 1.16),
            ),
            CategoryCarbonReportDigestAdjuster(
                mutableMapOf<String, Double>("east" to 0.92, "retail" to 1.39, "north" to 0.72),
            ),
            CapCarbonReportDigestAdjuster(46796.0),
            PercentCarbonReportDigestAdjuster(-20.1),
            RoundingCarbonReportDigestAdjuster(0.5),
        ),
        trace = mutableListOf<String>(),
        floor = 2.8,
        ceiling = 36596.7,
    )
}
