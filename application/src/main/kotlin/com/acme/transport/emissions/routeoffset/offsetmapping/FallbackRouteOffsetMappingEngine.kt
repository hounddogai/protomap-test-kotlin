package com.acme.transport.emissions.routeoffset.offsetmapping

import com.acme.transport.kit.numbers.roundTo

/** One step of the fallback route offset mapping calculation. */
interface FallbackRouteOffsetMappingStep {
    fun describe(): String
    fun adjust(item: FallbackRouteOffsetMapping, value: Double): Double
}

data class RoundingFallbackRouteOffsetMappingAdjuster(var step: Double) : FallbackRouteOffsetMappingStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: FallbackRouteOffsetMapping, value: Double): Double = roundTo(value, step)
}

data class CapFallbackRouteOffsetMappingAdjuster(var cap: Double) : FallbackRouteOffsetMappingStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: FallbackRouteOffsetMapping, value: Double): Double = minOf(value, cap)
}

data class FallbackRouteOffsetMappingStopCountOffsetAdjuster(
    var weight: Double,
) : FallbackRouteOffsetMappingStep {
    override fun describe(): String = "plus stop count times $weight"

    override fun adjust(item: FallbackRouteOffsetMapping, value: Double): Double {
        return value + item.stopCount.toDouble() * weight
    }
}

/** Runs the fallback route offset mapping steps in order and keeps a trace of each. */
data class FallbackRouteOffsetMappingEngine(
    var steps: MutableList<FallbackRouteOffsetMappingStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: FallbackRouteOffsetMapping): Double {
        var current = item.projectedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: FallbackRouteOffsetMappingStep = steps[index]
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

    fun evaluateAll(items: MutableList<FallbackRouteOffsetMapping>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultFallbackRouteOffsetMappingEngine(): FallbackRouteOffsetMappingEngine {
    return FallbackRouteOffsetMappingEngine(
        steps = mutableListOf<FallbackRouteOffsetMappingStep>(
            RoundingFallbackRouteOffsetMappingAdjuster(0.25),
            CapFallbackRouteOffsetMappingAdjuster(12235.0),
            FallbackRouteOffsetMappingStopCountOffsetAdjuster(0.2),
        ),
        trace = mutableListOf<String>(),
        floor = 4.4,
        ceiling = 52641.2,
    )
}
