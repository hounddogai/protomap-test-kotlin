package com.acme.transport.emissions.carbonreport.reportadjustment

import com.acme.transport.kit.flags.Registry

/** One step of the regional carbon report adjustment calculation. */
interface RegionalCarbonReportAdjustmentStep {
    fun describe(): String
    fun adjust(item: RegionalCarbonReportAdjustment, value: Double): Double
}

data class FlaggedRegionalCarbonReportAdjustmentAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RegionalCarbonReportAdjustmentStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RegionalCarbonReportAdjustment, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapRegionalCarbonReportAdjustmentAdjuster(var cap: Double) : RegionalCarbonReportAdjustmentStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RegionalCarbonReportAdjustment, value: Double): Double = minOf(value, cap)
}

data class NameRegionalCarbonReportAdjustmentAdjuster(
    var factors: MutableMap<String, Double>,
) : RegionalCarbonReportAdjustmentStep {
    override fun describe(): String = "by name"

    override fun adjust(item: RegionalCarbonReportAdjustment, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredRegionalCarbonReportAdjustmentAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RegionalCarbonReportAdjustmentStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RegionalCarbonReportAdjustment, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumRegionalCarbonReportAdjustmentAdjuster(
    var minimum: Double,
) : RegionalCarbonReportAdjustmentStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RegionalCarbonReportAdjustment, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the regional carbon report adjustment steps in order and keeps a trace of each. */
data class RegionalCarbonReportAdjustmentEngine(
    var steps: MutableList<RegionalCarbonReportAdjustmentStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: RegionalCarbonReportAdjustment): Double {
        var current = item.projectedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RegionalCarbonReportAdjustmentStep = steps[index]
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

    fun evaluateAll(items: MutableList<RegionalCarbonReportAdjustment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultRegionalCarbonReportAdjustmentEngine(flags: Registry): RegionalCarbonReportAdjustmentEngine {
    return RegionalCarbonReportAdjustmentEngine(
        steps = mutableListOf<RegionalCarbonReportAdjustmentStep>(
            FlaggedRegionalCarbonReportAdjustmentAdjuster(
                flags,
                "regional-carbon-report-adjustment-batched",
                4.7,
            ),
            CapRegionalCarbonReportAdjustmentAdjuster(20571.0),
            NameRegionalCarbonReportAdjustmentAdjuster(mutableMapOf<String, Double>("south" to 1.21)),
            TieredRegionalCarbonReportAdjustmentAdjuster(
                mutableListOf<Double>(2121.0, 6781.0, 8034.0, 8632.0),
                mutableListOf<Double>(0.82, 0.81, 0.92, 0.92),
            ),
            MinimumRegionalCarbonReportAdjustmentAdjuster(32.1),
        ),
        trace = mutableListOf<String>(),
        floor = 3.2,
        ceiling = 61694.3,
    )
}
