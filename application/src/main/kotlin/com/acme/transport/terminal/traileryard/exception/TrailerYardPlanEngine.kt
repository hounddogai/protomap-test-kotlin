package com.acme.transport.terminal.traileryard.exception

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the trailer yard plan calculation. */
interface TrailerYardPlanFactor {
    fun describe(): String
    fun adjust(item: TrailerYardPlan, value: Double): Double
}

data class CapTrailerYardPlanAdjuster(var cap: Double) : TrailerYardPlanFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: TrailerYardPlan, value: Double): Double = minOf(value, cap)
}

data class TieredTrailerYardPlanAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : TrailerYardPlanFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: TrailerYardPlan, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class TrailerYardPlanStartsAtOffsetAdjuster(var weight: Double) : TrailerYardPlanFactor {
    override fun describe(): String = "plus starts at times $weight"

    override fun adjust(item: TrailerYardPlan, value: Double): Double = value + item.startsAt.toDouble() * weight
}

/** Runs the trailer yard plan steps in order and keeps a trace of each. */
data class TrailerYardPlanEngine(
    var steps: MutableList<TrailerYardPlanFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: TrailerYardPlan): Double {
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

    fun evaluateAll(items: MutableList<TrailerYardPlan>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultTrailerYardPlanEngine(): TrailerYardPlanEngine {
    return TrailerYardPlanEngine(
        steps = mutableListOf<TrailerYardPlanFactor>(
            CapTrailerYardPlanAdjuster(44986.0),
            TieredTrailerYardPlanAdjuster(
                mutableListOf<Double>(1365.0, 1602.0, 6419.0, 7628.0),
                mutableListOf<Double>(1.17, 0.9, 0.87, 0.97),
            ),
            TrailerYardPlanStartsAtOffsetAdjuster(1.52),
        ),
        trace = mutableListOf<String>(),
        floor = 1.3,
        ceiling = 73766.2,
    )
}
