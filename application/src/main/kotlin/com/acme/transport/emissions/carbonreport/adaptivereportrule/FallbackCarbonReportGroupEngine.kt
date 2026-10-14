package com.acme.transport.emissions.carbonreport.adaptivereportrule

import com.acme.transport.kit.flags.Registry

/** One step of the fallback carbon report group calculation. */
interface FallbackCarbonReportGroupFactor {
    fun describe(): String
    fun adjust(item: FallbackCarbonReportGroup, value: Double): Double
}

data class TieredFallbackCarbonReportGroupAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : FallbackCarbonReportGroupFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: FallbackCarbonReportGroup, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CategoryFallbackCarbonReportGroupAdjuster(
    var factors: MutableMap<String, Double>,
) : FallbackCarbonReportGroupFactor {
    override fun describe(): String = "by category"

    override fun adjust(item: FallbackCarbonReportGroup, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedFallbackCarbonReportGroupAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : FallbackCarbonReportGroupFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: FallbackCarbonReportGroup, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapFallbackCarbonReportGroupAdjuster(var cap: Double) : FallbackCarbonReportGroupFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: FallbackCarbonReportGroup, value: Double): Double = minOf(value, cap)
}

data class FallbackCarbonReportGroupMinimumOffsetAdjuster(
    var weight: Double,
) : FallbackCarbonReportGroupFactor {
    override fun describe(): String = "plus minimum times $weight"

    override fun adjust(item: FallbackCarbonReportGroup, value: Double): Double = value + item.minimum * weight
}

data class PercentFallbackCarbonReportGroupAdjuster(var percent: Double) : FallbackCarbonReportGroupFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: FallbackCarbonReportGroup, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the fallback carbon report group steps in order and keeps a trace of each. */
data class FallbackCarbonReportGroupEngine(
    var steps: MutableList<FallbackCarbonReportGroupFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: FallbackCarbonReportGroup): Double {
        var current = item.netTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: FallbackCarbonReportGroupFactor = steps[index]
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

    fun evaluateAll(items: MutableList<FallbackCarbonReportGroup>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultFallbackCarbonReportGroupEngine(flags: Registry): FallbackCarbonReportGroupEngine {
    return FallbackCarbonReportGroupEngine(
        steps = mutableListOf<FallbackCarbonReportGroupFactor>(
            TieredFallbackCarbonReportGroupAdjuster(
                mutableListOf<Double>(3106.0, 3442.0),
                mutableListOf<Double>(1.07, 0.94),
            ),
            CategoryFallbackCarbonReportGroupAdjuster(
                mutableMapOf<String, Double>("north" to 1.16, "central" to 0.73, "east" to 1.4),
            ),
            FlaggedFallbackCarbonReportGroupAdjuster(flags, "fallback-carbon-report-group-capped", -0.1),
            CapFallbackCarbonReportGroupAdjuster(48716.0),
            FallbackCarbonReportGroupMinimumOffsetAdjuster(2.0),
            PercentFallbackCarbonReportGroupAdjuster(12.8),
        ),
        trace = mutableListOf<String>(),
        floor = 0.8,
        ceiling = 86183.1,
    )
}
