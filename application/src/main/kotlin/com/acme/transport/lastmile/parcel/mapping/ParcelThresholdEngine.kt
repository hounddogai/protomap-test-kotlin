package com.acme.transport.lastmile.parcel.mapping

import com.acme.transport.kit.numbers.roundTo

/** One step of the parcel threshold calculation. */
interface ParcelThresholdStep {
    fun describe(): String
    fun adjust(item: ParcelThreshold, value: Double): Double
}

data class CapParcelThresholdAdjuster(var cap: Double) : ParcelThresholdStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ParcelThreshold, value: Double): Double = minOf(value, cap)
}

data class RoundingParcelThresholdAdjuster(var step: Double) : ParcelThresholdStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ParcelThreshold, value: Double): Double = roundTo(value, step)
}

data class PercentParcelThresholdAdjuster(var percent: Double) : ParcelThresholdStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ParcelThreshold, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumParcelThresholdAdjuster(var minimum: Double) : ParcelThresholdStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ParcelThreshold, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the parcel threshold steps in order and keeps a trace of each. */
data class ParcelThresholdEngine(
    var steps: MutableList<ParcelThresholdStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: ParcelThreshold): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ParcelThresholdStep = steps[index]
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

    fun evaluateAll(items: MutableList<ParcelThreshold>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultParcelThresholdEngine(): ParcelThresholdEngine {
    return ParcelThresholdEngine(
        steps = mutableListOf<ParcelThresholdStep>(
            CapParcelThresholdAdjuster(41656.0),
            RoundingParcelThresholdAdjuster(1.0),
            PercentParcelThresholdAdjuster(0.3),
            MinimumParcelThresholdAdjuster(28.1),
        ),
        trace = mutableListOf<String>(),
        floor = 4.6,
        ceiling = 51768.9,
    )
}
