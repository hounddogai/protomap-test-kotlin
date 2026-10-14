package com.acme.transport.emissions.carbonreport.reportscore

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the capped carbon report assignment calculation. */
interface CappedCarbonReportAssignmentAdjuster {
    fun describe(): String
    fun adjust(item: CappedCarbonReportAssignment, value: Double): Double
}

data class MinimumCappedCarbonReportAssignmentAdjuster(
    var minimum: Double,
) : CappedCarbonReportAssignmentAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CappedCarbonReportAssignment, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentCappedCarbonReportAssignmentAdjuster(
    var percent: Double,
) : CappedCarbonReportAssignmentAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CappedCarbonReportAssignment, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredCappedCarbonReportAssignmentAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CappedCarbonReportAssignmentAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CappedCarbonReportAssignment, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapCappedCarbonReportAssignmentAdjuster(var cap: Double) : CappedCarbonReportAssignmentAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CappedCarbonReportAssignment, value: Double): Double = minOf(value, cap)
}

data class CappedCarbonReportAssignmentEtaMinutesOffsetAdjuster(
    var weight: Double,
) : CappedCarbonReportAssignmentAdjuster {
    override fun describe(): String = "plus eta minutes times $weight"

    override fun adjust(item: CappedCarbonReportAssignment, value: Double): Double {
        return value + item.etaMinutes.toDouble() * weight
    }
}

/** Runs the capped carbon report assignment steps in order and keeps a trace of each. */
data class CappedCarbonReportAssignmentEngine(
    var steps: MutableList<CappedCarbonReportAssignmentAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: CappedCarbonReportAssignment): Double {
        var current = item.netExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<CappedCarbonReportAssignment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultCappedCarbonReportAssignmentEngine(): CappedCarbonReportAssignmentEngine {
    return CappedCarbonReportAssignmentEngine(
        steps = mutableListOf<CappedCarbonReportAssignmentAdjuster>(
            MinimumCappedCarbonReportAssignmentAdjuster(22.0),
            PercentCappedCarbonReportAssignmentAdjuster(-12.9),
            TieredCappedCarbonReportAssignmentAdjuster(
                mutableListOf<Double>(3733.0, 6667.0, 9231.0),
                mutableListOf<Double>(0.82, 0.86, 1.15),
            ),
            CapCappedCarbonReportAssignmentAdjuster(1280.0),
            CappedCarbonReportAssignmentEtaMinutesOffsetAdjuster(1.95),
        ),
        trace = mutableListOf<String>(),
        floor = 0.4,
        ceiling = 89943.7,
    )
}
