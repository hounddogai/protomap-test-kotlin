package com.acme.transport.terminal.traileryard.quota

import com.acme.transport.kit.flags.Registry

/** One step of the trailer yard segment calculation. */
interface TrailerYardSegmentStep {
    fun describe(): String
    fun adjust(item: TrailerYardSegment, value: Double): Double
}

data class FlaggedTrailerYardSegmentAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TrailerYardSegmentStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TrailerYardSegment, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapTrailerYardSegmentAdjuster(var cap: Double) : TrailerYardSegmentStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: TrailerYardSegment, value: Double): Double = minOf(value, cap)
}

data class TieredTrailerYardSegmentAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : TrailerYardSegmentStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: TrailerYardSegment, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the trailer yard segment steps in order and keeps a trace of each. */
data class TrailerYardSegmentEngine(
    var steps: MutableList<TrailerYardSegmentStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: TrailerYardSegment): Double {
        var current = item.adjustedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: TrailerYardSegmentStep = steps[index]
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

    fun evaluateAll(items: MutableList<TrailerYardSegment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultTrailerYardSegmentEngine(flags: Registry): TrailerYardSegmentEngine {
    return TrailerYardSegmentEngine(
        steps = mutableListOf<TrailerYardSegmentStep>(
            FlaggedTrailerYardSegmentAdjuster(flags, "trailer-yard-segment-seasonal", 7.1),
            CapTrailerYardSegmentAdjuster(41238.0),
            TieredTrailerYardSegmentAdjuster(
                mutableListOf<Double>(3729.0, 5346.0, 7006.0, 9904.0),
                mutableListOf<Double>(1.14, 1.1, 1.07, 1.03),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 3.2,
        ceiling = 52334.4,
    )
}
