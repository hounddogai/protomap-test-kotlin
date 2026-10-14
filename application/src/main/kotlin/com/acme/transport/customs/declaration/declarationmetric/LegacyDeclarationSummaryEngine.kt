package com.acme.transport.customs.declaration.declarationmetric

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the legacy declaration summary calculation. */
interface LegacyDeclarationSummaryModifier {
    fun describe(): String
    fun adjust(item: LegacyDeclarationSummary, value: Double): Double
}

data class ChannelLegacyDeclarationSummaryAdjuster(
    var factors: MutableMap<String, Double>,
) : LegacyDeclarationSummaryModifier {
    override fun describe(): String = "by channel"

    override fun adjust(item: LegacyDeclarationSummary, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapLegacyDeclarationSummaryAdjuster(var cap: Double) : LegacyDeclarationSummaryModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LegacyDeclarationSummary, value: Double): Double = minOf(value, cap)
}

data class MinimumLegacyDeclarationSummaryAdjuster(var minimum: Double) : LegacyDeclarationSummaryModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LegacyDeclarationSummary, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the legacy declaration summary steps in order and keeps a trace of each. */
data class LegacyDeclarationSummaryEngine(
    var steps: MutableList<LegacyDeclarationSummaryModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: LegacyDeclarationSummary): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<LegacyDeclarationSummary>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultLegacyDeclarationSummaryEngine(): LegacyDeclarationSummaryEngine {
    return LegacyDeclarationSummaryEngine(
        steps = mutableListOf<LegacyDeclarationSummaryModifier>(
            ChannelLegacyDeclarationSummaryAdjuster(
                mutableMapOf<String, Double>("east" to 0.7, "central" to 0.83),
            ),
            CapLegacyDeclarationSummaryAdjuster(43933.0),
            MinimumLegacyDeclarationSummaryAdjuster(5.4),
        ),
        trace = mutableListOf<String>(),
        floor = 3.2,
        ceiling = 95248.0,
    )
}
