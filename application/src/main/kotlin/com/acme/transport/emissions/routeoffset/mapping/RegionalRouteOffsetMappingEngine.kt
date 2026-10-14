package com.acme.transport.emissions.routeoffset.mapping

import com.acme.transport.kit.flags.Registry

/** One step of the regional route offset mapping calculation. */
interface RegionalRouteOffsetMappingFactor {
    fun describe(): String
    fun adjust(item: RegionalRouteOffsetMapping, value: Double): Double
}

data class FlaggedRegionalRouteOffsetMappingAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RegionalRouteOffsetMappingFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RegionalRouteOffsetMapping, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentRegionalRouteOffsetMappingAdjuster(var percent: Double) : RegionalRouteOffsetMappingFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RegionalRouteOffsetMapping, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredRegionalRouteOffsetMappingAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RegionalRouteOffsetMappingFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RegionalRouteOffsetMapping, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the regional route offset mapping steps in order and keeps a trace of each. */
data class RegionalRouteOffsetMappingEngine(
    var steps: MutableList<RegionalRouteOffsetMappingFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: RegionalRouteOffsetMapping): Double {
        var current = item.weightedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RegionalRouteOffsetMappingFactor = steps[index]
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

    fun evaluateAll(items: MutableList<RegionalRouteOffsetMapping>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultRegionalRouteOffsetMappingEngine(flags: Registry): RegionalRouteOffsetMappingEngine {
    return RegionalRouteOffsetMappingEngine(
        steps = mutableListOf<RegionalRouteOffsetMappingFactor>(
            FlaggedRegionalRouteOffsetMappingAdjuster(flags, "regional-route-offset-mapping-legacy", 0.9),
            PercentRegionalRouteOffsetMappingAdjuster(3.0),
            TieredRegionalRouteOffsetMappingAdjuster(
                mutableListOf<Double>(1175.0, 3354.0, 4726.0, 7187.0),
                mutableListOf<Double>(1.15, 0.9, 0.96, 1.04),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 3.2,
        ceiling = 32983.6,
    )
}
