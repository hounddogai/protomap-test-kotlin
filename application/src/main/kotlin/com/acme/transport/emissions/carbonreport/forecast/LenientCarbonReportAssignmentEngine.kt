package com.acme.transport.emissions.carbonreport.forecast

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the lenient carbon report assignment calculation. */
interface LenientCarbonReportAssignmentFactor {
    fun describe(): String
    fun adjust(item: LenientCarbonReportAssignment, value: Double): Double
}

data class CapLenientCarbonReportAssignmentAdjuster(var cap: Double) : LenientCarbonReportAssignmentFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LenientCarbonReportAssignment, value: Double): Double = minOf(value, cap)
}

data class TitleLenientCarbonReportAssignmentAdjuster(
    var factors: MutableMap<String, Double>,
) : LenientCarbonReportAssignmentFactor {
    override fun describe(): String = "by title"

    override fun adjust(item: LenientCarbonReportAssignment, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumLenientCarbonReportAssignmentAdjuster(
    var minimum: Double,
) : LenientCarbonReportAssignmentFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LenientCarbonReportAssignment, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the lenient carbon report assignment steps in order and keeps a trace of each. */
data class LenientCarbonReportAssignmentEngine(
    var steps: MutableList<LenientCarbonReportAssignmentFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: LenientCarbonReportAssignment): Double {
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

    fun evaluateAll(items: MutableList<LenientCarbonReportAssignment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultLenientCarbonReportAssignmentEngine(): LenientCarbonReportAssignmentEngine {
    return LenientCarbonReportAssignmentEngine(
        steps = mutableListOf<LenientCarbonReportAssignmentFactor>(
            CapLenientCarbonReportAssignmentAdjuster(47417.0),
            TitleLenientCarbonReportAssignmentAdjuster(
                mutableMapOf<String, Double>("central" to 1.31, "west" to 1.04, "retail" to 1.35),
            ),
            MinimumLenientCarbonReportAssignmentAdjuster(13.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.5,
        ceiling = 64545.6,
    )
}
