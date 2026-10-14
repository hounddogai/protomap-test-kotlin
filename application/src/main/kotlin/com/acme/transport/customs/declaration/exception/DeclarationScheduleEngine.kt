package com.acme.transport.customs.declaration.exception

import com.acme.transport.kit.numbers.roundTo

/** One step of the declaration schedule calculation. */
interface DeclarationScheduleStep {
    fun describe(): String
    fun adjust(item: DeclarationSchedule, value: Double): Double
}

data class PercentDeclarationScheduleAdjuster(var percent: Double) : DeclarationScheduleStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DeclarationSchedule, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredDeclarationScheduleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DeclarationScheduleStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DeclarationSchedule, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingDeclarationScheduleAdjuster(var step: Double) : DeclarationScheduleStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DeclarationSchedule, value: Double): Double = roundTo(value, step)
}

/** Runs the declaration schedule steps in order and keeps a trace of each. */
data class DeclarationScheduleEngine(
    var steps: MutableList<DeclarationScheduleStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: DeclarationSchedule): Double {
        var current = item.projectedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: DeclarationScheduleStep = steps[index]
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

    fun evaluateAll(items: MutableList<DeclarationSchedule>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultDeclarationScheduleEngine(): DeclarationScheduleEngine {
    return DeclarationScheduleEngine(
        steps = mutableListOf<DeclarationScheduleStep>(
            PercentDeclarationScheduleAdjuster(8.8),
            TieredDeclarationScheduleAdjuster(
                mutableListOf<Double>(6612.0, 8512.0),
                mutableListOf<Double>(0.93, 1.14),
            ),
            RoundingDeclarationScheduleAdjuster(0.25),
        ),
        trace = mutableListOf<String>(),
        floor = 4.6,
        ceiling = 32634.3,
    )
}
