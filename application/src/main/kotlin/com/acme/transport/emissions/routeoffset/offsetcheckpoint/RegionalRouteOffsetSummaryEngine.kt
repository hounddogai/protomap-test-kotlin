package com.acme.transport.emissions.routeoffset.offsetcheckpoint

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the regional route offset summary calculation. */
interface RegionalRouteOffsetSummaryAdjuster {
    fun describe(): String
    fun adjust(item: RegionalRouteOffsetSummary, value: Double): Double
}

data class SourceRegionalRouteOffsetSummaryAdjuster(
    var factors: MutableMap<String, Double>,
) : RegionalRouteOffsetSummaryAdjuster {
    override fun describe(): String = "by source"

    override fun adjust(item: RegionalRouteOffsetSummary, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RegionalRouteOffsetSummaryCapacityOffsetAdjuster(
    var weight: Double,
) : RegionalRouteOffsetSummaryAdjuster {
    override fun describe(): String = "plus capacity times $weight"

    override fun adjust(item: RegionalRouteOffsetSummary, value: Double): Double {
        return value + item.capacity.toDouble() * weight
    }
}

data class PercentRegionalRouteOffsetSummaryAdjuster(var percent: Double) : RegionalRouteOffsetSummaryAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RegionalRouteOffsetSummary, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the regional route offset summary steps in order and keeps a trace of each. */
data class RegionalRouteOffsetSummaryEngine(
    var steps: MutableList<RegionalRouteOffsetSummaryAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: RegionalRouteOffsetSummary): Double {
        var current = item.netTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RegionalRouteOffsetSummary>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultRegionalRouteOffsetSummaryEngine(): RegionalRouteOffsetSummaryEngine {
    return RegionalRouteOffsetSummaryEngine(
        steps = mutableListOf<RegionalRouteOffsetSummaryAdjuster>(
            SourceRegionalRouteOffsetSummaryAdjuster(
                mutableMapOf<String, Double>("west" to 1.23, "online" to 1.35),
            ),
            RegionalRouteOffsetSummaryCapacityOffsetAdjuster(0.12),
            PercentRegionalRouteOffsetSummaryAdjuster(24.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.4,
        ceiling = 9670.9,
    )
}
