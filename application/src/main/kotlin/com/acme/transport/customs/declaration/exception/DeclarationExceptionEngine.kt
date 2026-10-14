package com.acme.transport.customs.declaration.exception

import com.acme.transport.kit.numbers.roundTo

/** One step of the declaration exception calculation. */
interface DeclarationExceptionStep {
    fun describe(): String
    fun adjust(item: DeclarationException, value: Double): Double
}

data class RoundingDeclarationExceptionAdjuster(var step: Double) : DeclarationExceptionStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DeclarationException, value: Double): Double = roundTo(value, step)
}

data class TieredDeclarationExceptionAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DeclarationExceptionStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DeclarationException, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumDeclarationExceptionAdjuster(var minimum: Double) : DeclarationExceptionStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DeclarationException, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentDeclarationExceptionAdjuster(var percent: Double) : DeclarationExceptionStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DeclarationException, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the declaration exception steps in order and keeps a trace of each. */
data class DeclarationExceptionEngine(
    var steps: MutableList<DeclarationExceptionStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: DeclarationException): Double {
        var current = item.effectiveVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: DeclarationExceptionStep = steps[index]
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

    fun evaluateAll(items: MutableList<DeclarationException>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultDeclarationExceptionEngine(): DeclarationExceptionEngine {
    return DeclarationExceptionEngine(
        steps = mutableListOf<DeclarationExceptionStep>(
            RoundingDeclarationExceptionAdjuster(0.25),
            TieredDeclarationExceptionAdjuster(
                mutableListOf<Double>(283.0, 1064.0, 6164.0),
                mutableListOf<Double>(0.84, 1.13, 0.95),
            ),
            MinimumDeclarationExceptionAdjuster(42.6),
            PercentDeclarationExceptionAdjuster(-1.4),
        ),
        trace = mutableListOf<String>(),
        floor = 4.5,
        ceiling = 47797.3,
    )
}
