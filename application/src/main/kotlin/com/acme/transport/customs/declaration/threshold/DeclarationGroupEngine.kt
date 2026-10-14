package com.acme.transport.customs.declaration.threshold

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the declaration group calculation. */
interface DeclarationGroupAdjuster {
    fun describe(): String
    fun adjust(item: DeclarationGroup, value: Double): Double
}

data class LabelDeclarationGroupAdjuster(var factors: MutableMap<String, Double>) : DeclarationGroupAdjuster {
    override fun describe(): String = "by label"

    override fun adjust(item: DeclarationGroup, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumDeclarationGroupAdjuster(var minimum: Double) : DeclarationGroupAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DeclarationGroup, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentDeclarationGroupAdjuster(var percent: Double) : DeclarationGroupAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DeclarationGroup, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingDeclarationGroupAdjuster(var step: Double) : DeclarationGroupAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DeclarationGroup, value: Double): Double = roundTo(value, step)
}

data class TieredDeclarationGroupAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DeclarationGroupAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DeclarationGroup, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the declaration group steps in order and keeps a trace of each. */
data class DeclarationGroupEngine(
    var steps: MutableList<DeclarationGroupAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: DeclarationGroup): Double {
        var current = item.adjustedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<DeclarationGroup>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultDeclarationGroupEngine(): DeclarationGroupEngine {
    return DeclarationGroupEngine(
        steps = mutableListOf<DeclarationGroupAdjuster>(
            LabelDeclarationGroupAdjuster(
                mutableMapOf<String, Double>("south" to 1.39, "west" to 0.86, "east" to 1.15),
            ),
            MinimumDeclarationGroupAdjuster(40.8),
            PercentDeclarationGroupAdjuster(6.9),
            RoundingDeclarationGroupAdjuster(0.5),
            TieredDeclarationGroupAdjuster(
                mutableListOf<Double>(6478.0, 7300.0),
                mutableListOf<Double>(0.84, 1.19),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.8,
        ceiling = 80079.5,
    )
}
