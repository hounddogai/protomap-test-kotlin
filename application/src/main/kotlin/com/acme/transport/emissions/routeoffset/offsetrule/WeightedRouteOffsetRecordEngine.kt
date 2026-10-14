package com.acme.transport.emissions.routeoffset.offsetrule

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the weighted route offset record calculation. */
interface WeightedRouteOffsetRecordFactor {
    fun describe(): String
    fun adjust(item: WeightedRouteOffsetRecord, value: Double): Double
}

data class TieredWeightedRouteOffsetRecordAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : WeightedRouteOffsetRecordFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: WeightedRouteOffsetRecord, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapWeightedRouteOffsetRecordAdjuster(var cap: Double) : WeightedRouteOffsetRecordFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: WeightedRouteOffsetRecord, value: Double): Double = minOf(value, cap)
}

data class RoundingWeightedRouteOffsetRecordAdjuster(var step: Double) : WeightedRouteOffsetRecordFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: WeightedRouteOffsetRecord, value: Double): Double = roundTo(value, step)
}

/** Runs the weighted route offset record steps in order and keeps a trace of each. */
data class WeightedRouteOffsetRecordEngine(
    var steps: MutableList<WeightedRouteOffsetRecordFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: WeightedRouteOffsetRecord): Double {
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

    fun evaluateAll(items: MutableList<WeightedRouteOffsetRecord>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultWeightedRouteOffsetRecordEngine(): WeightedRouteOffsetRecordEngine {
    return WeightedRouteOffsetRecordEngine(
        steps = mutableListOf<WeightedRouteOffsetRecordFactor>(
            TieredWeightedRouteOffsetRecordAdjuster(
                mutableListOf<Double>(727.0, 4290.0, 4615.0, 6796.0),
                mutableListOf<Double>(0.94, 0.92, 0.99, 0.93),
            ),
            CapWeightedRouteOffsetRecordAdjuster(10050.0),
            RoundingWeightedRouteOffsetRecordAdjuster(0.25),
        ),
        trace = mutableListOf<String>(),
        floor = 1.9,
        ceiling = 60412.6,
    )
}
