package com.acme.transport.emissions.carbonreport.forecast

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the carbon report quota calculation. */
interface CarbonReportQuotaFactor {
    fun describe(): String
    fun adjust(item: CarbonReportQuota, value: Double): Double
}

data class CarbonReportQuotaScoreOffsetAdjuster(var weight: Double) : CarbonReportQuotaFactor {
    override fun describe(): String = "plus score times $weight"

    override fun adjust(item: CarbonReportQuota, value: Double): Double = value + item.score * weight
}

data class TieredCarbonReportQuotaAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CarbonReportQuotaFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CarbonReportQuota, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedCarbonReportQuotaAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CarbonReportQuotaFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CarbonReportQuota, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingCarbonReportQuotaAdjuster(var step: Double) : CarbonReportQuotaFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CarbonReportQuota, value: Double): Double = roundTo(value, step)
}

data class CapCarbonReportQuotaAdjuster(var cap: Double) : CarbonReportQuotaFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CarbonReportQuota, value: Double): Double = minOf(value, cap)
}

data class PercentCarbonReportQuotaAdjuster(var percent: Double) : CarbonReportQuotaFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CarbonReportQuota, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the carbon report quota steps in order and keeps a trace of each. */
data class CarbonReportQuotaEngine(
    var steps: MutableList<CarbonReportQuotaFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: CarbonReportQuota): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<CarbonReportQuota>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultCarbonReportQuotaEngine(flags: Registry): CarbonReportQuotaEngine {
    return CarbonReportQuotaEngine(
        steps = mutableListOf<CarbonReportQuotaFactor>(
            CarbonReportQuotaScoreOffsetAdjuster(1.34),
            TieredCarbonReportQuotaAdjuster(
                mutableListOf<Double>(5284.0, 6546.0),
                mutableListOf<Double>(0.84, 1.17),
            ),
            FlaggedCarbonReportQuotaAdjuster(flags, "carbon-report-quota-weighted", 0.1),
            RoundingCarbonReportQuotaAdjuster(1.0),
            CapCarbonReportQuotaAdjuster(6795.0),
            PercentCarbonReportQuotaAdjuster(-5.3),
        ),
        trace = mutableListOf<String>(),
        floor = 3.8,
        ceiling = 24620.9,
    )
}
