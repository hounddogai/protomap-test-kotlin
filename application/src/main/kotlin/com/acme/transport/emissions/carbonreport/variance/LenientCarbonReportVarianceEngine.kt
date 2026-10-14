package com.acme.transport.emissions.carbonreport.variance

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the lenient carbon report variance calculation. */
interface LenientCarbonReportVarianceAdjuster {
    fun describe(): String
    fun adjust(item: LenientCarbonReportVariance, value: Double): Double
}

data class CapLenientCarbonReportVarianceAdjuster(var cap: Double) : LenientCarbonReportVarianceAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LenientCarbonReportVariance, value: Double): Double = minOf(value, cap)
}

data class FlaggedLenientCarbonReportVarianceAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LenientCarbonReportVarianceAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LenientCarbonReportVariance, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentLenientCarbonReportVarianceAdjuster(
    var percent: Double,
) : LenientCarbonReportVarianceAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LenientCarbonReportVariance, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumLenientCarbonReportVarianceAdjuster(
    var minimum: Double,
) : LenientCarbonReportVarianceAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LenientCarbonReportVariance, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class LenientCarbonReportVarianceEtaMinutesOffsetAdjuster(
    var weight: Double,
) : LenientCarbonReportVarianceAdjuster {
    override fun describe(): String = "plus eta minutes times $weight"

    override fun adjust(item: LenientCarbonReportVariance, value: Double): Double {
        return value + item.etaMinutes.toDouble() * weight
    }
}

/** Runs the lenient carbon report variance steps in order and keeps a trace of each. */
data class LenientCarbonReportVarianceEngine(
    var steps: MutableList<LenientCarbonReportVarianceAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: LenientCarbonReportVariance): Double {
        var current = item.projectedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<LenientCarbonReportVariance>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultLenientCarbonReportVarianceEngine(flags: Registry): LenientCarbonReportVarianceEngine {
    return LenientCarbonReportVarianceEngine(
        steps = mutableListOf<LenientCarbonReportVarianceAdjuster>(
            CapLenientCarbonReportVarianceAdjuster(39286.0),
            FlaggedLenientCarbonReportVarianceAdjuster(flags, "lenient-carbon-report-variance-default", 3.0),
            PercentLenientCarbonReportVarianceAdjuster(-8.0),
            MinimumLenientCarbonReportVarianceAdjuster(39.4),
            LenientCarbonReportVarianceEtaMinutesOffsetAdjuster(1.62),
        ),
        trace = mutableListOf<String>(),
        floor = 0.0,
        ceiling = 4661.5,
    )
}
