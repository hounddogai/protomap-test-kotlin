package com.acme.transport.network.zone.entry

import com.acme.transport.kit.numbers.roundTo

/** One step of the adaptive zone allocation calculation. */
interface AdaptiveZoneAllocationAdjuster {
    fun describe(): String
    fun adjust(item: AdaptiveZoneAllocation, value: Double): Double
}

data class AdaptiveZoneAllocationLoadKgOffsetAdjuster(var weight: Double) : AdaptiveZoneAllocationAdjuster {
    override fun describe(): String = "plus load kg times $weight"

    override fun adjust(item: AdaptiveZoneAllocation, value: Double): Double = value + item.loadKg * weight
}

data class RoundingAdaptiveZoneAllocationAdjuster(var step: Double) : AdaptiveZoneAllocationAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: AdaptiveZoneAllocation, value: Double): Double = roundTo(value, step)
}

data class CapAdaptiveZoneAllocationAdjuster(var cap: Double) : AdaptiveZoneAllocationAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AdaptiveZoneAllocation, value: Double): Double = minOf(value, cap)
}

/** Runs the adaptive zone allocation steps in order and keeps a trace of each. */
data class AdaptiveZoneAllocationEngine(
    var steps: MutableList<AdaptiveZoneAllocationAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: AdaptiveZoneAllocation): Double {
        var current = item.weightedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: AdaptiveZoneAllocationAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<AdaptiveZoneAllocation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultAdaptiveZoneAllocationEngine(): AdaptiveZoneAllocationEngine {
    return AdaptiveZoneAllocationEngine(
        steps = mutableListOf<AdaptiveZoneAllocationAdjuster>(
            AdaptiveZoneAllocationLoadKgOffsetAdjuster(1.28),
            RoundingAdaptiveZoneAllocationAdjuster(0.05),
            CapAdaptiveZoneAllocationAdjuster(5933.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.4,
        ceiling = 88323.7,
    )
}
