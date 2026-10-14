package com.acme.transport.customs.declaration.summary

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the declaration summary calculation. */
interface DeclarationSummaryModifier {
    fun describe(): String
    fun adjust(item: DeclarationSummary, value: Double): Double
}

data class RoundingDeclarationSummaryAdjuster(var step: Double) : DeclarationSummaryModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DeclarationSummary, value: Double): Double = roundTo(value, step)
}

data class NameDeclarationSummaryAdjuster(
    var factors: MutableMap<String, Double>,
) : DeclarationSummaryModifier {
    override fun describe(): String = "by name"

    override fun adjust(item: DeclarationSummary, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedDeclarationSummaryAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DeclarationSummaryModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DeclarationSummary, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentDeclarationSummaryAdjuster(var percent: Double) : DeclarationSummaryModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DeclarationSummary, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredDeclarationSummaryAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DeclarationSummaryModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DeclarationSummary, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class DeclarationSummaryMarginOffsetAdjuster(var weight: Double) : DeclarationSummaryModifier {
    override fun describe(): String = "plus margin times $weight"

    override fun adjust(item: DeclarationSummary, value: Double): Double = value + item.margin * weight
}

/** Runs the declaration summary steps in order and keeps a trace of each. */
data class DeclarationSummaryEngine(
    var steps: MutableList<DeclarationSummaryModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: DeclarationSummary): Double {
        var current = item.effectiveTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: DeclarationSummaryModifier = steps[index]
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

    fun evaluateAll(items: MutableList<DeclarationSummary>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultDeclarationSummaryEngine(flags: Registry): DeclarationSummaryEngine {
    return DeclarationSummaryEngine(
        steps = mutableListOf<DeclarationSummaryModifier>(
            RoundingDeclarationSummaryAdjuster(0.05),
            NameDeclarationSummaryAdjuster(mutableMapOf<String, Double>("central" to 1.16, "north" to 1.01)),
            FlaggedDeclarationSummaryAdjuster(flags, "declaration-summary-manual", -9.2),
            PercentDeclarationSummaryAdjuster(6.1),
            TieredDeclarationSummaryAdjuster(
                mutableListOf<Double>(2645.0, 7045.0, 8103.0),
                mutableListOf<Double>(0.86, 1.16, 1.18),
            ),
            DeclarationSummaryMarginOffsetAdjuster(1.91),
        ),
        trace = mutableListOf<String>(),
        floor = 2.8,
        ceiling = 40026.3,
    )
}
