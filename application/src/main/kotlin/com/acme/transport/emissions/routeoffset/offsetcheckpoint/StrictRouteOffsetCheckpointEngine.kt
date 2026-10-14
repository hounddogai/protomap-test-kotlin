package com.acme.transport.emissions.routeoffset.offsetcheckpoint

import com.acme.transport.kit.numbers.roundTo

/** One step of the strict route offset checkpoint calculation. */
interface StrictRouteOffsetCheckpointAdjuster {
    fun describe(): String
    fun adjust(item: StrictRouteOffsetCheckpoint, value: Double): Double
}

data class PercentStrictRouteOffsetCheckpointAdjuster(
    var percent: Double,
) : StrictRouteOffsetCheckpointAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: StrictRouteOffsetCheckpoint, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapStrictRouteOffsetCheckpointAdjuster(var cap: Double) : StrictRouteOffsetCheckpointAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: StrictRouteOffsetCheckpoint, value: Double): Double = minOf(value, cap)
}

data class RoundingStrictRouteOffsetCheckpointAdjuster(var step: Double) : StrictRouteOffsetCheckpointAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: StrictRouteOffsetCheckpoint, value: Double): Double = roundTo(value, step)
}

/** Runs the strict route offset checkpoint steps in order and keeps a trace of each. */
data class StrictRouteOffsetCheckpointEngine(
    var steps: MutableList<StrictRouteOffsetCheckpointAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: StrictRouteOffsetCheckpoint): Double {
        var current = item.projectedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: StrictRouteOffsetCheckpointAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<StrictRouteOffsetCheckpoint>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultStrictRouteOffsetCheckpointEngine(): StrictRouteOffsetCheckpointEngine {
    return StrictRouteOffsetCheckpointEngine(
        steps = mutableListOf<StrictRouteOffsetCheckpointAdjuster>(
            PercentStrictRouteOffsetCheckpointAdjuster(-11.9),
            CapStrictRouteOffsetCheckpointAdjuster(6694.0),
            RoundingStrictRouteOffsetCheckpointAdjuster(0.05),
        ),
        trace = mutableListOf<String>(),
        floor = 4.0,
        ceiling = 84635.1,
    )
}
