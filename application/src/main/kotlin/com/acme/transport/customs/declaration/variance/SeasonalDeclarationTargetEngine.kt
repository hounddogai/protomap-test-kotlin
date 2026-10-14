package com.acme.transport.customs.declaration.variance

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the seasonal declaration target calculation. */
interface SeasonalDeclarationTargetModifier {
    fun describe(): String
    fun adjust(item: SeasonalDeclarationTarget, value: Double): Double
}

data class PercentSeasonalDeclarationTargetAdjuster(var percent: Double) : SeasonalDeclarationTargetModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: SeasonalDeclarationTarget, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingSeasonalDeclarationTargetAdjuster(var step: Double) : SeasonalDeclarationTargetModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: SeasonalDeclarationTarget, value: Double): Double = roundTo(value, step)
}

data class SeasonalDeclarationTargetMaximumOffsetAdjuster(
    var weight: Double,
) : SeasonalDeclarationTargetModifier {
    override fun describe(): String = "plus maximum times $weight"

    override fun adjust(item: SeasonalDeclarationTarget, value: Double): Double = value + item.maximum * weight
}

data class MinimumSeasonalDeclarationTargetAdjuster(var minimum: Double) : SeasonalDeclarationTargetModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: SeasonalDeclarationTarget, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapSeasonalDeclarationTargetAdjuster(var cap: Double) : SeasonalDeclarationTargetModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: SeasonalDeclarationTarget, value: Double): Double = minOf(value, cap)
}

/** Runs the seasonal declaration target steps in order and keeps a trace of each. */
data class SeasonalDeclarationTargetEngine(
    var steps: MutableList<SeasonalDeclarationTargetModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: SeasonalDeclarationTarget): Double {
        var current = item.netValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<SeasonalDeclarationTarget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultSeasonalDeclarationTargetEngine(): SeasonalDeclarationTargetEngine {
    return SeasonalDeclarationTargetEngine(
        steps = mutableListOf<SeasonalDeclarationTargetModifier>(
            PercentSeasonalDeclarationTargetAdjuster(-15.2),
            RoundingSeasonalDeclarationTargetAdjuster(1.0),
            SeasonalDeclarationTargetMaximumOffsetAdjuster(1.33),
            MinimumSeasonalDeclarationTargetAdjuster(32.1),
            CapSeasonalDeclarationTargetAdjuster(45120.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.5,
        ceiling = 4080.6,
    )
}
