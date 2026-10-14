package com.acme.transport.customs.declaration.notice

import com.acme.transport.kit.numbers.roundTo

/** One step of the declaration entry calculation. */
interface DeclarationEntryModifier {
    fun describe(): String
    fun adjust(item: DeclarationEntry, value: Double): Double
}

data class PercentDeclarationEntryAdjuster(var percent: Double) : DeclarationEntryModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DeclarationEntry, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredDeclarationEntryAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DeclarationEntryModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DeclarationEntry, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class DeclarationEntryCostOffsetAdjuster(var weight: Double) : DeclarationEntryModifier {
    override fun describe(): String = "plus cost times $weight"

    override fun adjust(item: DeclarationEntry, value: Double): Double = value + item.cost * weight
}

data class MinimumDeclarationEntryAdjuster(var minimum: Double) : DeclarationEntryModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DeclarationEntry, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingDeclarationEntryAdjuster(var step: Double) : DeclarationEntryModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DeclarationEntry, value: Double): Double = roundTo(value, step)
}

/** Runs the declaration entry steps in order and keeps a trace of each. */
data class DeclarationEntryEngine(
    var steps: MutableList<DeclarationEntryModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: DeclarationEntry): Double {
        var current = item.effectiveExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: DeclarationEntryModifier = steps[index]
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

    fun evaluateAll(items: MutableList<DeclarationEntry>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultDeclarationEntryEngine(): DeclarationEntryEngine {
    return DeclarationEntryEngine(
        steps = mutableListOf<DeclarationEntryModifier>(
            PercentDeclarationEntryAdjuster(8.2),
            TieredDeclarationEntryAdjuster(
                mutableListOf<Double>(1664.0, 2287.0, 2957.0, 6871.0),
                mutableListOf<Double>(1.13, 0.98, 1.18, 0.99),
            ),
            DeclarationEntryCostOffsetAdjuster(0.7),
            MinimumDeclarationEntryAdjuster(31.1),
            RoundingDeclarationEntryAdjuster(0.5),
        ),
        trace = mutableListOf<String>(),
        floor = 4.3,
        ceiling = 40684.9,
    )
}
