package com.acme.transport.network.transittime.threshold

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the transit time review calculation. */
interface TransitTimeReviewStep {
    fun describe(): String
    fun adjust(item: TransitTimeReview, value: Double): Double
}

data class RoundingTransitTimeReviewAdjuster(var step: Double) : TransitTimeReviewStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: TransitTimeReview, value: Double): Double = roundTo(value, step)
}

data class TieredTransitTimeReviewAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : TransitTimeReviewStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: TransitTimeReview, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentTransitTimeReviewAdjuster(var percent: Double) : TransitTimeReviewStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: TransitTimeReview, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumTransitTimeReviewAdjuster(var minimum: Double) : TransitTimeReviewStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: TransitTimeReview, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TransitTimeReviewFuelLitersOffsetAdjuster(var weight: Double) : TransitTimeReviewStep {
    override fun describe(): String = "plus fuel liters times $weight"

    override fun adjust(item: TransitTimeReview, value: Double): Double = value + item.fuelLiters * weight
}

data class CapTransitTimeReviewAdjuster(var cap: Double) : TransitTimeReviewStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: TransitTimeReview, value: Double): Double = minOf(value, cap)
}

/** Runs the transit time review steps in order and keeps a trace of each. */
data class TransitTimeReviewEngine(
    var steps: MutableList<TransitTimeReviewStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: TransitTimeReview): Double {
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

    fun evaluateAll(items: MutableList<TransitTimeReview>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultTransitTimeReviewEngine(): TransitTimeReviewEngine {
    return TransitTimeReviewEngine(
        steps = mutableListOf<TransitTimeReviewStep>(
            RoundingTransitTimeReviewAdjuster(0.05),
            TieredTransitTimeReviewAdjuster(
                mutableListOf<Double>(1855.0, 2050.0),
                mutableListOf<Double>(0.98, 0.93),
            ),
            PercentTransitTimeReviewAdjuster(-1.3),
            MinimumTransitTimeReviewAdjuster(21.6),
            TransitTimeReviewFuelLitersOffsetAdjuster(0.26),
            CapTransitTimeReviewAdjuster(19474.0),
        ),
        trace = mutableListOf<String>(),
        floor = 1.1,
        ceiling = 58532.3,
    )
}
