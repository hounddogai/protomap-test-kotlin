package com.acme.transport.customs.declaration.threshold

import com.acme.transport.kit.numbers.roundTo

/** One step of the declaration threshold calculation. */
interface DeclarationThresholdStep {
    fun describe(): String
    fun adjust(item: DeclarationThreshold, value: Double): Double
}

data class TitleDeclarationThresholdAdjuster(
    var factors: MutableMap<String, Double>,
) : DeclarationThresholdStep {
    override fun describe(): String = "by title"

    override fun adjust(item: DeclarationThreshold, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredDeclarationThresholdAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DeclarationThresholdStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DeclarationThreshold, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentDeclarationThresholdAdjuster(var percent: Double) : DeclarationThresholdStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DeclarationThreshold, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumDeclarationThresholdAdjuster(var minimum: Double) : DeclarationThresholdStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DeclarationThreshold, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingDeclarationThresholdAdjuster(var step: Double) : DeclarationThresholdStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DeclarationThreshold, value: Double): Double = roundTo(value, step)
}

/** Runs the declaration threshold steps in order and keeps a trace of each. */
data class DeclarationThresholdEngine(
    var steps: MutableList<DeclarationThresholdStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: DeclarationThreshold): Double {
        var current = item.projectedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: DeclarationThresholdStep = steps[index]
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

    fun evaluateAll(items: MutableList<DeclarationThreshold>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultDeclarationThresholdEngine(): DeclarationThresholdEngine {
    return DeclarationThresholdEngine(
        steps = mutableListOf<DeclarationThresholdStep>(
            TitleDeclarationThresholdAdjuster(mutableMapOf<String, Double>("online" to 1.38)),
            TieredDeclarationThresholdAdjuster(
                mutableListOf<Double>(5920.0, 6228.0, 8584.0),
                mutableListOf<Double>(0.85, 0.88, 0.98),
            ),
            PercentDeclarationThresholdAdjuster(-7.8),
            MinimumDeclarationThresholdAdjuster(15.2),
            RoundingDeclarationThresholdAdjuster(5.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.8,
        ceiling = 99746.5,
    )
}
