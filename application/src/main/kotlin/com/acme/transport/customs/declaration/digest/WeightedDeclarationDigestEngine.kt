package com.acme.transport.customs.declaration.digest

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the weighted declaration digest calculation. */
interface WeightedDeclarationDigestStep {
    fun describe(): String
    fun adjust(item: WeightedDeclarationDigest, value: Double): Double
}

data class MinimumWeightedDeclarationDigestAdjuster(var minimum: Double) : WeightedDeclarationDigestStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: WeightedDeclarationDigest, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentWeightedDeclarationDigestAdjuster(var percent: Double) : WeightedDeclarationDigestStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: WeightedDeclarationDigest, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingWeightedDeclarationDigestAdjuster(var step: Double) : WeightedDeclarationDigestStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: WeightedDeclarationDigest, value: Double): Double = roundTo(value, step)
}

/** Runs the weighted declaration digest steps in order and keeps a trace of each. */
data class WeightedDeclarationDigestEngine(
    var steps: MutableList<WeightedDeclarationDigestStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: WeightedDeclarationDigest): Double {
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

    fun evaluateAll(items: MutableList<WeightedDeclarationDigest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultWeightedDeclarationDigestEngine(): WeightedDeclarationDigestEngine {
    return WeightedDeclarationDigestEngine(
        steps = mutableListOf<WeightedDeclarationDigestStep>(
            MinimumWeightedDeclarationDigestAdjuster(8.5),
            PercentWeightedDeclarationDigestAdjuster(-16.6),
            RoundingWeightedDeclarationDigestAdjuster(0.05),
        ),
        trace = mutableListOf<String>(),
        floor = 4.1,
        ceiling = 64887.6,
    )
}
