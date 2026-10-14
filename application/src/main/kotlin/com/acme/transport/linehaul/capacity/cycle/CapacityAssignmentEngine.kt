package com.acme.transport.linehaul.capacity.cycle

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the capacity assignment calculation. */
interface CapacityAssignmentModifier {
    fun describe(): String
    fun adjust(item: CapacityAssignment, value: Double): Double
}

data class CapacityAssignmentScoreOffsetAdjuster(var weight: Double) : CapacityAssignmentModifier {
    override fun describe(): String = "plus score times $weight"

    override fun adjust(item: CapacityAssignment, value: Double): Double = value + item.score * weight
}

data class PercentCapacityAssignmentAdjuster(var percent: Double) : CapacityAssignmentModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CapacityAssignment, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapCapacityAssignmentAdjuster(var cap: Double) : CapacityAssignmentModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CapacityAssignment, value: Double): Double = minOf(value, cap)
}

data class TieredCapacityAssignmentAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CapacityAssignmentModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CapacityAssignment, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the capacity assignment steps in order and keeps a trace of each. */
data class CapacityAssignmentEngine(
    var steps: MutableList<CapacityAssignmentModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: CapacityAssignment): Double {
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

    fun evaluateAll(items: MutableList<CapacityAssignment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultCapacityAssignmentEngine(): CapacityAssignmentEngine {
    return CapacityAssignmentEngine(
        steps = mutableListOf<CapacityAssignmentModifier>(
            CapacityAssignmentScoreOffsetAdjuster(0.81),
            PercentCapacityAssignmentAdjuster(-12.1),
            CapCapacityAssignmentAdjuster(17688.0),
            TieredCapacityAssignmentAdjuster(
                mutableListOf<Double>(5062.0, 5164.0),
                mutableListOf<Double>(0.9, 1.14),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 4.4,
        ceiling = 73805.2,
    )
}
