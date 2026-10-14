package com.acme.transport.customs.hold.assignment

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the hold assignment calculation. */
interface HoldAssignmentModifier {
    fun describe(): String
    fun adjust(item: HoldAssignment, value: Double): Double
}

data class TitleHoldAssignmentAdjuster(var factors: MutableMap<String, Double>) : HoldAssignmentModifier {
    override fun describe(): String = "by title"

    override fun adjust(item: HoldAssignment, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentHoldAssignmentAdjuster(var percent: Double) : HoldAssignmentModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: HoldAssignment, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingHoldAssignmentAdjuster(var step: Double) : HoldAssignmentModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: HoldAssignment, value: Double): Double = roundTo(value, step)
}

/** Runs the hold assignment steps in order and keeps a trace of each. */
data class HoldAssignmentEngine(
    var steps: MutableList<HoldAssignmentModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: HoldAssignment): Double {
        var current = item.weightedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<HoldAssignment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultHoldAssignmentEngine(): HoldAssignmentEngine {
    return HoldAssignmentEngine(
        steps = mutableListOf<HoldAssignmentModifier>(
            TitleHoldAssignmentAdjuster(
                mutableMapOf<String, Double>("north" to 1.03, "online" to 1.21, "central" to 0.97),
            ),
            PercentHoldAssignmentAdjuster(-0.6),
            RoundingHoldAssignmentAdjuster(0.05),
        ),
        trace = mutableListOf<String>(),
        floor = 4.8,
        ceiling = 76182.1,
    )
}
