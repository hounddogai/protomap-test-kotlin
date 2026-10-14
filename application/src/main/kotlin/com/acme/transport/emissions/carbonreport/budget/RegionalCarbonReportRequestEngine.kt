package com.acme.transport.emissions.carbonreport.budget

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the regional carbon report request calculation. */
interface RegionalCarbonReportRequestAdjuster {
    fun describe(): String
    fun adjust(item: RegionalCarbonReportRequest, value: Double): Double
}

data class RegionalCarbonReportRequestStartsAtOffsetAdjuster(
    var weight: Double,
) : RegionalCarbonReportRequestAdjuster {
    override fun describe(): String = "plus starts at times $weight"

    override fun adjust(item: RegionalCarbonReportRequest, value: Double): Double {
        return value + item.startsAt.toDouble() * weight
    }
}

data class CapRegionalCarbonReportRequestAdjuster(var cap: Double) : RegionalCarbonReportRequestAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RegionalCarbonReportRequest, value: Double): Double = minOf(value, cap)
}

data class SourceRegionalCarbonReportRequestAdjuster(
    var factors: MutableMap<String, Double>,
) : RegionalCarbonReportRequestAdjuster {
    override fun describe(): String = "by source"

    override fun adjust(item: RegionalCarbonReportRequest, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the regional carbon report request steps in order and keeps a trace of each. */
data class RegionalCarbonReportRequestEngine(
    var steps: MutableList<RegionalCarbonReportRequestAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: RegionalCarbonReportRequest): Double {
        var current = item.effectiveTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RegionalCarbonReportRequest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultRegionalCarbonReportRequestEngine(): RegionalCarbonReportRequestEngine {
    return RegionalCarbonReportRequestEngine(
        steps = mutableListOf<RegionalCarbonReportRequestAdjuster>(
            RegionalCarbonReportRequestStartsAtOffsetAdjuster(0.81),
            CapRegionalCarbonReportRequestAdjuster(42454.0),
            SourceRegionalCarbonReportRequestAdjuster(
                mutableMapOf<String, Double>("online" to 1.35, "north" to 0.94),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.3,
        ceiling = 21303.6,
    )
}
