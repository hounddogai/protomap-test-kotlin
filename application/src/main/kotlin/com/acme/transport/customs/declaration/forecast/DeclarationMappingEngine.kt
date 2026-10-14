package com.acme.transport.customs.declaration.forecast

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the declaration mapping calculation. */
interface DeclarationMappingModifier {
    fun describe(): String
    fun adjust(item: DeclarationMapping, value: Double): Double
}

data class TieredDeclarationMappingAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DeclarationMappingModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DeclarationMapping, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumDeclarationMappingAdjuster(var minimum: Double) : DeclarationMappingModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DeclarationMapping, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class DeclarationMappingMinimumOffsetAdjuster(var weight: Double) : DeclarationMappingModifier {
    override fun describe(): String = "plus minimum times $weight"

    override fun adjust(item: DeclarationMapping, value: Double): Double = value + item.minimum * weight
}

data class CapDeclarationMappingAdjuster(var cap: Double) : DeclarationMappingModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DeclarationMapping, value: Double): Double = minOf(value, cap)
}

data class PercentDeclarationMappingAdjuster(var percent: Double) : DeclarationMappingModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DeclarationMapping, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class LaneDeclarationMappingAdjuster(
    var factors: MutableMap<String, Double>,
) : DeclarationMappingModifier {
    override fun describe(): String = "by lane"

    override fun adjust(item: DeclarationMapping, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the declaration mapping steps in order and keeps a trace of each. */
data class DeclarationMappingEngine(
    var steps: MutableList<DeclarationMappingModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: DeclarationMapping): Double {
        var current = item.effectiveExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<DeclarationMapping>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultDeclarationMappingEngine(): DeclarationMappingEngine {
    return DeclarationMappingEngine(
        steps = mutableListOf<DeclarationMappingModifier>(
            TieredDeclarationMappingAdjuster(
                mutableListOf<Double>(468.0, 1145.0, 8578.0, 8696.0),
                mutableListOf<Double>(0.94, 1.08, 0.85, 1.13),
            ),
            MinimumDeclarationMappingAdjuster(32.3),
            DeclarationMappingMinimumOffsetAdjuster(0.02),
            CapDeclarationMappingAdjuster(36519.0),
            PercentDeclarationMappingAdjuster(9.5),
            LaneDeclarationMappingAdjuster(
                mutableMapOf<String, Double>("online" to 0.96, "retail" to 0.84, "east" to 1.23),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 2.9,
        ceiling = 48337.2,
    )
}
