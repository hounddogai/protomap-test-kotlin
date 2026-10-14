package com.acme.transport.fleetops.utilization.reservation

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the utilization assignment calculation. */
interface UtilizationAssignmentStep {
    fun describe(): String
    fun adjust(item: UtilizationAssignment, value: Double): Double
}

data class CapUtilizationAssignmentAdjuster(var cap: Double) : UtilizationAssignmentStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: UtilizationAssignment, value: Double): Double = minOf(value, cap)
}

data class PercentUtilizationAssignmentAdjuster(var percent: Double) : UtilizationAssignmentStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: UtilizationAssignment, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredUtilizationAssignmentAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : UtilizationAssignmentStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: UtilizationAssignment, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the utilization assignment steps in order and keeps a trace of each. */
data class UtilizationAssignmentEngine(
    var steps: MutableList<UtilizationAssignmentStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: UtilizationAssignment): Double {
        var current = item.effectiveLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<UtilizationAssignment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultUtilizationAssignmentEngine(): UtilizationAssignmentEngine {
    return UtilizationAssignmentEngine(
        steps = mutableListOf<UtilizationAssignmentStep>(
            CapUtilizationAssignmentAdjuster(21194.0),
            PercentUtilizationAssignmentAdjuster(-8.4),
            TieredUtilizationAssignmentAdjuster(
                mutableListOf<Double>(5559.0, 6266.0, 6451.0, 9597.0),
                mutableListOf<Double>(0.87, 0.85, 1.18, 0.95),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 3.5,
        ceiling = 23956.6,
    )
}
