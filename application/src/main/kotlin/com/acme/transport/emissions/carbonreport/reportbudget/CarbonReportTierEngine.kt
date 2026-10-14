package com.acme.transport.emissions.carbonreport.reportbudget

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the carbon report tier calculation. */
interface CarbonReportTierFactor {
    fun describe(): String
    fun adjust(item: CarbonReportTier, value: Double): Double
}

data class FlaggedCarbonReportTierAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CarbonReportTierFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CarbonReportTier, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CarbonReportTierDurationMinutesOffsetAdjuster(var weight: Double) : CarbonReportTierFactor {
    override fun describe(): String = "plus duration minutes times $weight"

    override fun adjust(item: CarbonReportTier, value: Double): Double {
        return value + item.durationMinutes.toDouble() * weight
    }
}

data class TieredCarbonReportTierAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CarbonReportTierFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CarbonReportTier, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the carbon report tier steps in order and keeps a trace of each. */
data class CarbonReportTierEngine(
    var steps: MutableList<CarbonReportTierFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: CarbonReportTier): Double {
        var current = item.weightedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<CarbonReportTier>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultCarbonReportTierEngine(flags: Registry): CarbonReportTierEngine {
    return CarbonReportTierEngine(
        steps = mutableListOf<CarbonReportTierFactor>(
            FlaggedCarbonReportTierAdjuster(flags, "carbon-report-tier-fallback", -2.0),
            CarbonReportTierDurationMinutesOffsetAdjuster(0.17),
            TieredCarbonReportTierAdjuster(
                mutableListOf<Double>(990.0, 6857.0, 8899.0, 8975.0),
                mutableListOf<Double>(0.87, 1.05, 0.97, 1.01),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 3.6,
        ceiling = 88959.2,
    )
}
