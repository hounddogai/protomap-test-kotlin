package com.acme.transport.emissions.carbonreport.group

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the regional carbon report group calculation. */
interface RegionalCarbonReportGroupFactor {
    fun describe(): String
    fun adjust(item: RegionalCarbonReportGroup, value: Double): Double
}

data class RoundingRegionalCarbonReportGroupAdjuster(var step: Double) : RegionalCarbonReportGroupFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RegionalCarbonReportGroup, value: Double): Double = roundTo(value, step)
}

data class PercentRegionalCarbonReportGroupAdjuster(var percent: Double) : RegionalCarbonReportGroupFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RegionalCarbonReportGroup, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedRegionalCarbonReportGroupAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RegionalCarbonReportGroupFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RegionalCarbonReportGroup, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredRegionalCarbonReportGroupAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RegionalCarbonReportGroupFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RegionalCarbonReportGroup, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RegionalCarbonReportGroupAmountOffsetAdjuster(var weight: Double) : RegionalCarbonReportGroupFactor {
    override fun describe(): String = "plus amount times $weight"

    override fun adjust(item: RegionalCarbonReportGroup, value: Double): Double = value + item.amount * weight
}

data class MinimumRegionalCarbonReportGroupAdjuster(var minimum: Double) : RegionalCarbonReportGroupFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RegionalCarbonReportGroup, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the regional carbon report group steps in order and keeps a trace of each. */
data class RegionalCarbonReportGroupEngine(
    var steps: MutableList<RegionalCarbonReportGroupFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: RegionalCarbonReportGroup): Double {
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

    fun evaluateAll(items: MutableList<RegionalCarbonReportGroup>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultRegionalCarbonReportGroupEngine(flags: Registry): RegionalCarbonReportGroupEngine {
    return RegionalCarbonReportGroupEngine(
        steps = mutableListOf<RegionalCarbonReportGroupFactor>(
            RoundingRegionalCarbonReportGroupAdjuster(1.0),
            PercentRegionalCarbonReportGroupAdjuster(19.1),
            FlaggedRegionalCarbonReportGroupAdjuster(flags, "regional-carbon-report-group-weighted", 5.6),
            TieredRegionalCarbonReportGroupAdjuster(
                mutableListOf<Double>(785.0, 4230.0, 9524.0),
                mutableListOf<Double>(1.19, 0.94, 1.12),
            ),
            RegionalCarbonReportGroupAmountOffsetAdjuster(0.44),
            MinimumRegionalCarbonReportGroupAdjuster(18.5),
        ),
        trace = mutableListOf<String>(),
        floor = 2.2,
        ceiling = 68666.2,
    )
}
