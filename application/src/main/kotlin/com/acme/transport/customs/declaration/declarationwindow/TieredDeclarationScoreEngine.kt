package com.acme.transport.customs.declaration.declarationwindow

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the tiered declaration score calculation. */
interface TieredDeclarationScoreModifier {
    fun describe(): String
    fun adjust(item: TieredDeclarationScore, value: Double): Double
}

data class RoundingTieredDeclarationScoreAdjuster(var step: Double) : TieredDeclarationScoreModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: TieredDeclarationScore, value: Double): Double = roundTo(value, step)
}

data class TieredDeclarationScoreCapacityOffsetAdjuster(var weight: Double) : TieredDeclarationScoreModifier {
    override fun describe(): String = "plus capacity times $weight"

    override fun adjust(item: TieredDeclarationScore, value: Double): Double {
        return value + item.capacity.toDouble() * weight
    }
}

data class LabelTieredDeclarationScoreAdjuster(
    var factors: MutableMap<String, Double>,
) : TieredDeclarationScoreModifier {
    override fun describe(): String = "by label"

    override fun adjust(item: TieredDeclarationScore, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the tiered declaration score steps in order and keeps a trace of each. */
data class TieredDeclarationScoreEngine(
    var steps: MutableList<TieredDeclarationScoreModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: TieredDeclarationScore): Double {
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

    fun evaluateAll(items: MutableList<TieredDeclarationScore>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultTieredDeclarationScoreEngine(): TieredDeclarationScoreEngine {
    return TieredDeclarationScoreEngine(
        steps = mutableListOf<TieredDeclarationScoreModifier>(
            RoundingTieredDeclarationScoreAdjuster(1.0),
            TieredDeclarationScoreCapacityOffsetAdjuster(1.21),
            LabelTieredDeclarationScoreAdjuster(
                mutableMapOf<String, Double>("retail" to 1.29, "online" to 0.72),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.5,
        ceiling = 13837.5,
    )
}
