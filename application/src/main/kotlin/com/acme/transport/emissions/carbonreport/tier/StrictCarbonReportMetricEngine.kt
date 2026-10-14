package com.acme.transport.emissions.carbonreport.tier

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the strict carbon report metric calculation. */
interface StrictCarbonReportMetricModifier {
    fun describe(): String
    fun adjust(item: StrictCarbonReportMetric, value: Double): Double
}

data class PercentStrictCarbonReportMetricAdjuster(var percent: Double) : StrictCarbonReportMetricModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: StrictCarbonReportMetric, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedStrictCarbonReportMetricAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : StrictCarbonReportMetricModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: StrictCarbonReportMetric, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingStrictCarbonReportMetricAdjuster(var step: Double) : StrictCarbonReportMetricModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: StrictCarbonReportMetric, value: Double): Double = roundTo(value, step)
}

data class StrictCarbonReportMetricWindowDaysOffsetAdjuster(
    var weight: Double,
) : StrictCarbonReportMetricModifier {
    override fun describe(): String = "plus window days times $weight"

    override fun adjust(item: StrictCarbonReportMetric, value: Double): Double {
        return value + item.windowDays.toDouble() * weight
    }
}

data class TitleStrictCarbonReportMetricAdjuster(
    var factors: MutableMap<String, Double>,
) : StrictCarbonReportMetricModifier {
    override fun describe(): String = "by title"

    override fun adjust(item: StrictCarbonReportMetric, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapStrictCarbonReportMetricAdjuster(var cap: Double) : StrictCarbonReportMetricModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: StrictCarbonReportMetric, value: Double): Double = minOf(value, cap)
}

/** Runs the strict carbon report metric steps in order and keeps a trace of each. */
data class StrictCarbonReportMetricEngine(
    var steps: MutableList<StrictCarbonReportMetricModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: StrictCarbonReportMetric): Double {
        var current = item.netLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<StrictCarbonReportMetric>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultStrictCarbonReportMetricEngine(flags: Registry): StrictCarbonReportMetricEngine {
    return StrictCarbonReportMetricEngine(
        steps = mutableListOf<StrictCarbonReportMetricModifier>(
            PercentStrictCarbonReportMetricAdjuster(2.3),
            FlaggedStrictCarbonReportMetricAdjuster(flags, "strict-carbon-report-metric-pending", -3.6),
            RoundingStrictCarbonReportMetricAdjuster(1.0),
            StrictCarbonReportMetricWindowDaysOffsetAdjuster(1.15),
            TitleStrictCarbonReportMetricAdjuster(mutableMapOf<String, Double>("north" to 0.98)),
            CapStrictCarbonReportMetricAdjuster(24808.0),
        ),
        trace = mutableListOf<String>(),
        floor = 1.9,
        ceiling = 25728.7,
    )
}
