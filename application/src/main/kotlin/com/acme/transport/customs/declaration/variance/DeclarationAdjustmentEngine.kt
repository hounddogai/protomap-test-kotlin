package com.acme.transport.customs.declaration.variance

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the declaration adjustment calculation. */
interface DeclarationAdjustmentModifier {
    fun describe(): String
    fun adjust(item: DeclarationAdjustment, value: Double): Double
}

data class CapDeclarationAdjustmentAdjuster(var cap: Double) : DeclarationAdjustmentModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DeclarationAdjustment, value: Double): Double = minOf(value, cap)
}

data class TieredDeclarationAdjustmentAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DeclarationAdjustmentModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DeclarationAdjustment, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentDeclarationAdjustmentAdjuster(var percent: Double) : DeclarationAdjustmentModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DeclarationAdjustment, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class DeclarationAdjustmentDistanceKmOffsetAdjuster(var weight: Double) : DeclarationAdjustmentModifier {
    override fun describe(): String = "plus distance km times $weight"

    override fun adjust(item: DeclarationAdjustment, value: Double): Double = value + item.distanceKm * weight
}

data class MinimumDeclarationAdjustmentAdjuster(var minimum: Double) : DeclarationAdjustmentModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DeclarationAdjustment, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the declaration adjustment steps in order and keeps a trace of each. */
data class DeclarationAdjustmentEngine(
    var steps: MutableList<DeclarationAdjustmentModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: DeclarationAdjustment): Double {
        var current = item.effectiveLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<DeclarationAdjustment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultDeclarationAdjustmentEngine(): DeclarationAdjustmentEngine {
    return DeclarationAdjustmentEngine(
        steps = mutableListOf<DeclarationAdjustmentModifier>(
            CapDeclarationAdjustmentAdjuster(16938.0),
            TieredDeclarationAdjustmentAdjuster(
                mutableListOf<Double>(6526.0, 8881.0, 9751.0),
                mutableListOf<Double>(1.17, 1.16, 1.06),
            ),
            PercentDeclarationAdjustmentAdjuster(4.3),
            DeclarationAdjustmentDistanceKmOffsetAdjuster(1.01),
            MinimumDeclarationAdjustmentAdjuster(29.0),
        ),
        trace = mutableListOf<String>(),
        floor = 1.6,
        ceiling = 62989.5,
    )
}
