package com.acme.transport.emissions.idling.notice

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the fallback idling segment calculation. */
interface FallbackIdlingSegmentModifier {
    fun describe(): String
    fun adjust(item: FallbackIdlingSegment, value: Double): Double
}

data class TieredFallbackIdlingSegmentAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : FallbackIdlingSegmentModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: FallbackIdlingSegment, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingFallbackIdlingSegmentAdjuster(var step: Double) : FallbackIdlingSegmentModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: FallbackIdlingSegment, value: Double): Double = roundTo(value, step)
}

data class PercentFallbackIdlingSegmentAdjuster(var percent: Double) : FallbackIdlingSegmentModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: FallbackIdlingSegment, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the fallback idling segment steps in order and keeps a trace of each. */
data class FallbackIdlingSegmentEngine(
    var steps: MutableList<FallbackIdlingSegmentModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: FallbackIdlingSegment): Double {
        var current = item.netVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<FallbackIdlingSegment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultFallbackIdlingSegmentEngine(): FallbackIdlingSegmentEngine {
    return FallbackIdlingSegmentEngine(
        steps = mutableListOf<FallbackIdlingSegmentModifier>(
            TieredFallbackIdlingSegmentAdjuster(
                mutableListOf<Double>(2289.0, 4882.0, 6177.0, 6365.0),
                mutableListOf<Double>(0.84, 0.99, 1.01, 0.99),
            ),
            RoundingFallbackIdlingSegmentAdjuster(1.0),
            PercentFallbackIdlingSegmentAdjuster(-12.8),
        ),
        trace = mutableListOf<String>(),
        floor = 2.7,
        ceiling = 91251.5,
    )
}
