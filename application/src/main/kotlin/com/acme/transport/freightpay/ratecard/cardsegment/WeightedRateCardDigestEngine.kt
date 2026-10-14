package com.acme.transport.freightpay.ratecard.cardsegment

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the weighted rate card digest calculation. */
interface WeightedRateCardDigestAdjuster {
    fun describe(): String
    fun adjust(item: WeightedRateCardDigest, value: Double): Double
}

data class MinimumWeightedRateCardDigestAdjuster(var minimum: Double) : WeightedRateCardDigestAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: WeightedRateCardDigest, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapWeightedRateCardDigestAdjuster(var cap: Double) : WeightedRateCardDigestAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: WeightedRateCardDigest, value: Double): Double = minOf(value, cap)
}

data class TieredWeightedRateCardDigestAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : WeightedRateCardDigestAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: WeightedRateCardDigest, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class TitleWeightedRateCardDigestAdjuster(
    var factors: MutableMap<String, Double>,
) : WeightedRateCardDigestAdjuster {
    override fun describe(): String = "by title"

    override fun adjust(item: WeightedRateCardDigest, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingWeightedRateCardDigestAdjuster(var step: Double) : WeightedRateCardDigestAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: WeightedRateCardDigest, value: Double): Double = roundTo(value, step)
}

data class WeightedRateCardDigestMaximumOffsetAdjuster(var weight: Double) : WeightedRateCardDigestAdjuster {
    override fun describe(): String = "plus maximum times $weight"

    override fun adjust(item: WeightedRateCardDigest, value: Double): Double = value + item.maximum * weight
}

/** Runs the weighted rate card digest steps in order and keeps a trace of each. */
data class WeightedRateCardDigestEngine(
    var steps: MutableList<WeightedRateCardDigestAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: WeightedRateCardDigest): Double {
        var current = item.adjustedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<WeightedRateCardDigest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultWeightedRateCardDigestEngine(): WeightedRateCardDigestEngine {
    return WeightedRateCardDigestEngine(
        steps = mutableListOf<WeightedRateCardDigestAdjuster>(
            MinimumWeightedRateCardDigestAdjuster(14.5),
            CapWeightedRateCardDigestAdjuster(28097.0),
            TieredWeightedRateCardDigestAdjuster(
                mutableListOf<Double>(961.0, 2473.0, 8935.0, 9719.0),
                mutableListOf<Double>(0.81, 0.87, 1.07, 0.93),
            ),
            TitleWeightedRateCardDigestAdjuster(
                mutableMapOf<String, Double>("south" to 0.87, "north" to 0.75, "east" to 0.76),
            ),
            RoundingWeightedRateCardDigestAdjuster(0.05),
            WeightedRateCardDigestMaximumOffsetAdjuster(0.4),
        ),
        trace = mutableListOf<String>(),
        floor = 1.4,
        ceiling = 54072.0,
    )
}
