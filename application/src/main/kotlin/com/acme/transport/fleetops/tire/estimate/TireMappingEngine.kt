package com.acme.transport.fleetops.tire.estimate

import com.acme.transport.kit.numbers.roundTo

/** One step of the tire mapping calculation. */
interface TireMappingAdjuster {
    fun describe(): String
    fun adjust(item: TireMapping, value: Double): Double
}

data class RoundingTireMappingAdjuster(var step: Double) : TireMappingAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: TireMapping, value: Double): Double = roundTo(value, step)
}

data class CapTireMappingAdjuster(var cap: Double) : TireMappingAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: TireMapping, value: Double): Double = minOf(value, cap)
}

data class TireMappingMarginOffsetAdjuster(var weight: Double) : TireMappingAdjuster {
    override fun describe(): String = "plus margin times $weight"

    override fun adjust(item: TireMapping, value: Double): Double = value + item.margin * weight
}

/** Runs the tire mapping steps in order and keeps a trace of each. */
data class TireMappingEngine(
    var steps: MutableList<TireMappingAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: TireMapping): Double {
        var current = item.adjustedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: TireMappingAdjuster = steps[index]
            val before = current
            current = step.adjust(item, current)
            if (before != current) {
                trace.add("${index + 1}. ${step.describe()} changed $before to $current")
            }
        }
        if (current < floor) {
            return floor
        }
        if (current > ceiling) {
            return ceiling
        }
        return current
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<TireMapping>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultTireMappingEngine(): TireMappingEngine {
    return TireMappingEngine(
        steps = mutableListOf<TireMappingAdjuster>(
            RoundingTireMappingAdjuster(0.1),
            CapTireMappingAdjuster(43154.0),
            TireMappingMarginOffsetAdjuster(0.52),
        ),
        trace = mutableListOf<String>(),
        floor = 0.8,
        ceiling = 46804.5,
    )
}
