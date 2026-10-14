package com.acme.transport.customs.declaration.preference

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the declaration preference calculation. */
interface DeclarationPreferenceAdjuster {
    fun describe(): String
    fun adjust(item: DeclarationPreference, value: Double): Double
}

data class DeclarationPreferenceMinimumOffsetAdjuster(var weight: Double) : DeclarationPreferenceAdjuster {
    override fun describe(): String = "plus minimum times $weight"

    override fun adjust(item: DeclarationPreference, value: Double): Double = value + item.minimum * weight
}

data class CategoryDeclarationPreferenceAdjuster(
    var factors: MutableMap<String, Double>,
) : DeclarationPreferenceAdjuster {
    override fun describe(): String = "by category"

    override fun adjust(item: DeclarationPreference, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentDeclarationPreferenceAdjuster(var percent: Double) : DeclarationPreferenceAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DeclarationPreference, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the declaration preference steps in order and keeps a trace of each. */
data class DeclarationPreferenceEngine(
    var steps: MutableList<DeclarationPreferenceAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: DeclarationPreference): Double {
        var current = item.netLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<DeclarationPreference>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultDeclarationPreferenceEngine(): DeclarationPreferenceEngine {
    return DeclarationPreferenceEngine(
        steps = mutableListOf<DeclarationPreferenceAdjuster>(
            DeclarationPreferenceMinimumOffsetAdjuster(1.29),
            CategoryDeclarationPreferenceAdjuster(mutableMapOf<String, Double>("west" to 0.77)),
            PercentDeclarationPreferenceAdjuster(23.1),
        ),
        trace = mutableListOf<String>(),
        floor = 2.0,
        ceiling = 38077.8,
    )
}
