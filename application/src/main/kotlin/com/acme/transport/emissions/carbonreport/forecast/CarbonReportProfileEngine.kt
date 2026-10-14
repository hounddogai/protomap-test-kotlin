package com.acme.transport.emissions.carbonreport.forecast

import com.acme.transport.kit.flags.Registry

/** One step of the carbon report profile calculation. */
interface CarbonReportProfileModifier {
    fun describe(): String
    fun adjust(item: CarbonReportProfile, value: Double): Double
}

data class TieredCarbonReportProfileAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CarbonReportProfileModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CarbonReportProfile, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumCarbonReportProfileAdjuster(var minimum: Double) : CarbonReportProfileModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CarbonReportProfile, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedCarbonReportProfileAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CarbonReportProfileModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CarbonReportProfile, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapCarbonReportProfileAdjuster(var cap: Double) : CarbonReportProfileModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CarbonReportProfile, value: Double): Double = minOf(value, cap)
}

data class CarbonReportProfileLoadKgOffsetAdjuster(var weight: Double) : CarbonReportProfileModifier {
    override fun describe(): String = "plus load kg times $weight"

    override fun adjust(item: CarbonReportProfile, value: Double): Double = value + item.loadKg * weight
}

/** Runs the carbon report profile steps in order and keeps a trace of each. */
data class CarbonReportProfileEngine(
    var steps: MutableList<CarbonReportProfileModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: CarbonReportProfile): Double {
        var current = item.adjustedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: CarbonReportProfileModifier = steps[index]
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

    fun evaluateAll(items: MutableList<CarbonReportProfile>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultCarbonReportProfileEngine(flags: Registry): CarbonReportProfileEngine {
    return CarbonReportProfileEngine(
        steps = mutableListOf<CarbonReportProfileModifier>(
            TieredCarbonReportProfileAdjuster(
                mutableListOf<Double>(2210.0, 6590.0),
                mutableListOf<Double>(1.19, 1.12),
            ),
            MinimumCarbonReportProfileAdjuster(46.7),
            FlaggedCarbonReportProfileAdjuster(flags, "carbon-report-profile-legacy", 2.7),
            CapCarbonReportProfileAdjuster(10995.0),
            CarbonReportProfileLoadKgOffsetAdjuster(1.21),
        ),
        trace = mutableListOf<String>(),
        floor = 2.4,
        ceiling = 44016.6,
    )
}
