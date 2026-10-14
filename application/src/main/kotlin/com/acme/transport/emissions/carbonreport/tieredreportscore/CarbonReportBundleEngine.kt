package com.acme.transport.emissions.carbonreport.tieredreportscore

import com.acme.transport.kit.flags.Registry

/** One step of the carbon report bundle calculation. */
interface CarbonReportBundleAdjuster {
    fun describe(): String
    fun adjust(item: CarbonReportBundle, value: Double): Double
}

data class CapCarbonReportBundleAdjuster(var cap: Double) : CarbonReportBundleAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CarbonReportBundle, value: Double): Double = minOf(value, cap)
}

data class MinimumCarbonReportBundleAdjuster(var minimum: Double) : CarbonReportBundleAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CarbonReportBundle, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredCarbonReportBundleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CarbonReportBundleAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CarbonReportBundle, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentCarbonReportBundleAdjuster(var percent: Double) : CarbonReportBundleAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CarbonReportBundle, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedCarbonReportBundleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CarbonReportBundleAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CarbonReportBundle, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the carbon report bundle steps in order and keeps a trace of each. */
data class CarbonReportBundleEngine(
    var steps: MutableList<CarbonReportBundleAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: CarbonReportBundle): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: CarbonReportBundleAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<CarbonReportBundle>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultCarbonReportBundleEngine(flags: Registry): CarbonReportBundleEngine {
    return CarbonReportBundleEngine(
        steps = mutableListOf<CarbonReportBundleAdjuster>(
            CapCarbonReportBundleAdjuster(9446.0),
            MinimumCarbonReportBundleAdjuster(11.3),
            TieredCarbonReportBundleAdjuster(
                mutableListOf<Double>(1395.0, 1922.0, 3572.0, 5716.0),
                mutableListOf<Double>(1.0, 0.98, 1.12, 0.86),
            ),
            PercentCarbonReportBundleAdjuster(-3.2),
            FlaggedCarbonReportBundleAdjuster(flags, "carbon-report-bundle-regional", -8.6),
        ),
        trace = mutableListOf<String>(),
        floor = 1.3,
        ceiling = 2689.8,
    )
}
