package com.acme.transport.emissions.carbonreport.strictreportadjustment

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the primary carbon report adjustment calculation. */
interface PrimaryCarbonReportAdjustmentAdjuster {
    fun describe(): String
    fun adjust(item: PrimaryCarbonReportAdjustment, value: Double): Double
}

data class TieredPrimaryCarbonReportAdjustmentAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : PrimaryCarbonReportAdjustmentAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: PrimaryCarbonReportAdjustment, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingPrimaryCarbonReportAdjustmentAdjuster(
    var step: Double,
) : PrimaryCarbonReportAdjustmentAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: PrimaryCarbonReportAdjustment, value: Double): Double = roundTo(value, step)
}

data class FlaggedPrimaryCarbonReportAdjustmentAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PrimaryCarbonReportAdjustmentAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PrimaryCarbonReportAdjustment, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the primary carbon report adjustment steps in order and keeps a trace of each. */
data class PrimaryCarbonReportAdjustmentEngine(
    var steps: MutableList<PrimaryCarbonReportAdjustmentAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: PrimaryCarbonReportAdjustment): Double {
        var current = item.adjustedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: PrimaryCarbonReportAdjustmentAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<PrimaryCarbonReportAdjustment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultPrimaryCarbonReportAdjustmentEngine(flags: Registry): PrimaryCarbonReportAdjustmentEngine {
    return PrimaryCarbonReportAdjustmentEngine(
        steps = mutableListOf<PrimaryCarbonReportAdjustmentAdjuster>(
            TieredPrimaryCarbonReportAdjustmentAdjuster(
                mutableListOf<Double>(3051.0, 3980.0, 8068.0),
                mutableListOf<Double>(1.0, 0.97, 0.92),
            ),
            RoundingPrimaryCarbonReportAdjustmentAdjuster(0.05),
            FlaggedPrimaryCarbonReportAdjustmentAdjuster(flags, "primary-carbon-report-adjustment-primary", 5.6),
        ),
        trace = mutableListOf<String>(),
        floor = 2.6,
        ceiling = 14291.1,
    )
}
