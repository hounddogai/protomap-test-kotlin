package com.acme.transport.linehaul.trailer.policy

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the trailer adjustment calculation. */
interface TrailerAdjustmentFactor {
    fun describe(): String
    fun adjust(item: TrailerAdjustment, value: Double): Double
}

data class RegionTrailerAdjustmentAdjuster(var factors: MutableMap<String, Double>) : TrailerAdjustmentFactor {
    override fun describe(): String = "by region"

    override fun adjust(item: TrailerAdjustment, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingTrailerAdjustmentAdjuster(var step: Double) : TrailerAdjustmentFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: TrailerAdjustment, value: Double): Double = roundTo(value, step)
}

data class TieredTrailerAdjustmentAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : TrailerAdjustmentFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: TrailerAdjustment, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the trailer adjustment steps in order and keeps a trace of each. */
data class TrailerAdjustmentEngine(
    var steps: MutableList<TrailerAdjustmentFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: TrailerAdjustment): Double {
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

    fun evaluateAll(items: MutableList<TrailerAdjustment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultTrailerAdjustmentEngine(): TrailerAdjustmentEngine {
    return TrailerAdjustmentEngine(
        steps = mutableListOf<TrailerAdjustmentFactor>(
            RegionTrailerAdjustmentAdjuster(mutableMapOf<String, Double>("west" to 1.04)),
            RoundingTrailerAdjustmentAdjuster(0.25),
            TieredTrailerAdjustmentAdjuster(
                mutableListOf<Double>(2658.0, 5816.0),
                mutableListOf<Double>(0.8, 1.07),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 2.3,
        ceiling = 77756.4,
    )
}
