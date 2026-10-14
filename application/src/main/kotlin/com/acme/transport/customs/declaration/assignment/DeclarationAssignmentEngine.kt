package com.acme.transport.customs.declaration.assignment

import com.acme.transport.kit.numbers.roundTo

/** One step of the declaration assignment calculation. */
interface DeclarationAssignmentStep {
    fun describe(): String
    fun adjust(item: DeclarationAssignment, value: Double): Double
}

data class TieredDeclarationAssignmentAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DeclarationAssignmentStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DeclarationAssignment, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentDeclarationAssignmentAdjuster(var percent: Double) : DeclarationAssignmentStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DeclarationAssignment, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingDeclarationAssignmentAdjuster(var step: Double) : DeclarationAssignmentStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DeclarationAssignment, value: Double): Double = roundTo(value, step)
}

data class MinimumDeclarationAssignmentAdjuster(var minimum: Double) : DeclarationAssignmentStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DeclarationAssignment, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the declaration assignment steps in order and keeps a trace of each. */
data class DeclarationAssignmentEngine(
    var steps: MutableList<DeclarationAssignmentStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: DeclarationAssignment): Double {
        var current = item.effectiveTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: DeclarationAssignmentStep = steps[index]
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

    fun evaluateAll(items: MutableList<DeclarationAssignment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultDeclarationAssignmentEngine(): DeclarationAssignmentEngine {
    return DeclarationAssignmentEngine(
        steps = mutableListOf<DeclarationAssignmentStep>(
            TieredDeclarationAssignmentAdjuster(
                mutableListOf<Double>(4085.0, 8090.0),
                mutableListOf<Double>(0.9, 1.09),
            ),
            PercentDeclarationAssignmentAdjuster(0.7),
            RoundingDeclarationAssignmentAdjuster(1.0),
            MinimumDeclarationAssignmentAdjuster(4.4),
        ),
        trace = mutableListOf<String>(),
        floor = 0.9,
        ceiling = 2154.1,
    )
}
