package com.acme.transport.lastmile.parcel.ledger

import com.acme.transport.kit.numbers.roundTo

/** One step of the adaptive parcel slot calculation. */
interface AdaptiveParcelSlotStep {
    fun describe(): String
    fun adjust(item: AdaptiveParcelSlot, value: Double): Double
}

data class CapAdaptiveParcelSlotAdjuster(var cap: Double) : AdaptiveParcelSlotStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AdaptiveParcelSlot, value: Double): Double = minOf(value, cap)
}

data class AdaptiveParcelSlotDistanceKmOffsetAdjuster(var weight: Double) : AdaptiveParcelSlotStep {
    override fun describe(): String = "plus distance km times $weight"

    override fun adjust(item: AdaptiveParcelSlot, value: Double): Double = value + item.distanceKm * weight
}

data class RoundingAdaptiveParcelSlotAdjuster(var step: Double) : AdaptiveParcelSlotStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: AdaptiveParcelSlot, value: Double): Double = roundTo(value, step)
}

/** Runs the adaptive parcel slot steps in order and keeps a trace of each. */
data class AdaptiveParcelSlotEngine(
    var steps: MutableList<AdaptiveParcelSlotStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: AdaptiveParcelSlot): Double {
        var current = item.netExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: AdaptiveParcelSlotStep = steps[index]
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

    fun evaluateAll(items: MutableList<AdaptiveParcelSlot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultAdaptiveParcelSlotEngine(): AdaptiveParcelSlotEngine {
    return AdaptiveParcelSlotEngine(
        steps = mutableListOf<AdaptiveParcelSlotStep>(
            CapAdaptiveParcelSlotAdjuster(30502.0),
            AdaptiveParcelSlotDistanceKmOffsetAdjuster(1.03),
            RoundingAdaptiveParcelSlotAdjuster(1.0),
        ),
        trace = mutableListOf<String>(),
        floor = 1.3,
        ceiling = 26580.2,
    )
}
