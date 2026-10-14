package com.acme.transport.freightpay.accessorial.accessorialplan

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the seasonal accessorial tier calculation. */
interface SeasonalAccessorialTierModifier {
    fun describe(): String
    fun adjust(item: SeasonalAccessorialTier, value: Double): Double
}

data class RoundingSeasonalAccessorialTierAdjuster(var step: Double) : SeasonalAccessorialTierModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: SeasonalAccessorialTier, value: Double): Double = roundTo(value, step)
}

data class CapSeasonalAccessorialTierAdjuster(var cap: Double) : SeasonalAccessorialTierModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: SeasonalAccessorialTier, value: Double): Double = minOf(value, cap)
}

data class SeasonalAccessorialTierLoadKgOffsetAdjuster(var weight: Double) : SeasonalAccessorialTierModifier {
    override fun describe(): String = "plus load kg times $weight"

    override fun adjust(item: SeasonalAccessorialTier, value: Double): Double = value + item.loadKg * weight
}

/** Runs the seasonal accessorial tier steps in order and keeps a trace of each. */
data class SeasonalAccessorialTierEngine(
    var steps: MutableList<SeasonalAccessorialTierModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: SeasonalAccessorialTier): Double {
        var current = item.projectedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<SeasonalAccessorialTier>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultSeasonalAccessorialTierEngine(): SeasonalAccessorialTierEngine {
    return SeasonalAccessorialTierEngine(
        steps = mutableListOf<SeasonalAccessorialTierModifier>(
            RoundingSeasonalAccessorialTierAdjuster(5.0),
            CapSeasonalAccessorialTierAdjuster(47797.0),
            SeasonalAccessorialTierLoadKgOffsetAdjuster(0.89),
        ),
        trace = mutableListOf<String>(),
        floor = 5.0,
        ceiling = 5352.4,
    )
}
