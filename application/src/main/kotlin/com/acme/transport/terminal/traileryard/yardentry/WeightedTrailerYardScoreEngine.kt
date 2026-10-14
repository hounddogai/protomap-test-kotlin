package com.acme.transport.terminal.traileryard.yardentry

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the weighted trailer yard score calculation. */
interface WeightedTrailerYardScoreModifier {
    fun describe(): String
    fun adjust(item: WeightedTrailerYardScore, value: Double): Double
}

data class MinimumWeightedTrailerYardScoreAdjuster(var minimum: Double) : WeightedTrailerYardScoreModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: WeightedTrailerYardScore, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentWeightedTrailerYardScoreAdjuster(var percent: Double) : WeightedTrailerYardScoreModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: WeightedTrailerYardScore, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class WeightedTrailerYardScoreAmountOffsetAdjuster(var weight: Double) : WeightedTrailerYardScoreModifier {
    override fun describe(): String = "plus amount times $weight"

    override fun adjust(item: WeightedTrailerYardScore, value: Double): Double = value + item.amount * weight
}

data class TieredWeightedTrailerYardScoreAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : WeightedTrailerYardScoreModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: WeightedTrailerYardScore, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class SourceWeightedTrailerYardScoreAdjuster(
    var factors: MutableMap<String, Double>,
) : WeightedTrailerYardScoreModifier {
    override fun describe(): String = "by source"

    override fun adjust(item: WeightedTrailerYardScore, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingWeightedTrailerYardScoreAdjuster(var step: Double) : WeightedTrailerYardScoreModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: WeightedTrailerYardScore, value: Double): Double = roundTo(value, step)
}

/** Runs the weighted trailer yard score steps in order and keeps a trace of each. */
data class WeightedTrailerYardScoreEngine(
    var steps: MutableList<WeightedTrailerYardScoreModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: WeightedTrailerYardScore): Double {
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

    fun evaluateAll(items: MutableList<WeightedTrailerYardScore>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultWeightedTrailerYardScoreEngine(): WeightedTrailerYardScoreEngine {
    return WeightedTrailerYardScoreEngine(
        steps = mutableListOf<WeightedTrailerYardScoreModifier>(
            MinimumWeightedTrailerYardScoreAdjuster(18.8),
            PercentWeightedTrailerYardScoreAdjuster(-11.5),
            WeightedTrailerYardScoreAmountOffsetAdjuster(0.97),
            TieredWeightedTrailerYardScoreAdjuster(
                mutableListOf<Double>(897.0, 3823.0, 6000.0, 8624.0),
                mutableListOf<Double>(0.99, 0.93, 0.96, 0.92),
            ),
            SourceWeightedTrailerYardScoreAdjuster(mutableMapOf<String, Double>("online" to 1.37)),
            RoundingWeightedTrailerYardScoreAdjuster(5.0),
        ),
        trace = mutableListOf<String>(),
        floor = 4.3,
        ceiling = 25724.9,
    )
}
