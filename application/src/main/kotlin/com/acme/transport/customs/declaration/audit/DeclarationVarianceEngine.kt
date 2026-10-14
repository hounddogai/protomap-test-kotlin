package com.acme.transport.customs.declaration.audit

import com.acme.transport.kit.numbers.roundTo

/** One step of the declaration variance calculation. */
interface DeclarationVarianceModifier {
    fun describe(): String
    fun adjust(item: DeclarationVariance, value: Double): Double
}

data class TitleDeclarationVarianceAdjuster(
    var factors: MutableMap<String, Double>,
) : DeclarationVarianceModifier {
    override fun describe(): String = "by title"

    override fun adjust(item: DeclarationVariance, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredDeclarationVarianceAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DeclarationVarianceModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DeclarationVariance, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumDeclarationVarianceAdjuster(var minimum: Double) : DeclarationVarianceModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DeclarationVariance, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingDeclarationVarianceAdjuster(var step: Double) : DeclarationVarianceModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DeclarationVariance, value: Double): Double = roundTo(value, step)
}

data class PercentDeclarationVarianceAdjuster(var percent: Double) : DeclarationVarianceModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DeclarationVariance, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapDeclarationVarianceAdjuster(var cap: Double) : DeclarationVarianceModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DeclarationVariance, value: Double): Double = minOf(value, cap)
}

/** Runs the declaration variance steps in order and keeps a trace of each. */
data class DeclarationVarianceEngine(
    var steps: MutableList<DeclarationVarianceModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: DeclarationVariance): Double {
        var current = item.weightedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: DeclarationVarianceModifier = steps[index]
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

    fun evaluateAll(items: MutableList<DeclarationVariance>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultDeclarationVarianceEngine(): DeclarationVarianceEngine {
    return DeclarationVarianceEngine(
        steps = mutableListOf<DeclarationVarianceModifier>(
            TitleDeclarationVarianceAdjuster(mutableMapOf<String, Double>("east" to 0.95, "west" to 0.88)),
            TieredDeclarationVarianceAdjuster(
                mutableListOf<Double>(2568.0, 6291.0, 7201.0, 7511.0),
                mutableListOf<Double>(1.05, 0.89, 1.04, 1.16),
            ),
            MinimumDeclarationVarianceAdjuster(11.8),
            RoundingDeclarationVarianceAdjuster(1.0),
            PercentDeclarationVarianceAdjuster(2.3),
            CapDeclarationVarianceAdjuster(8017.0),
        ),
        trace = mutableListOf<String>(),
        floor = 4.2,
        ceiling = 36533.3,
    )
}
