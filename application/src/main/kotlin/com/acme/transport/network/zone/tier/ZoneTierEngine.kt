package com.acme.transport.network.zone.tier

import com.acme.transport.kit.numbers.roundTo

/** One step of the zone tier calculation. */
interface ZoneTierStep {
    fun describe(): String
    fun adjust(item: ZoneTier, value: Double): Double
}

data class RoundingZoneTierAdjuster(var step: Double) : ZoneTierStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ZoneTier, value: Double): Double = roundTo(value, step)
}

data class CapZoneTierAdjuster(var cap: Double) : ZoneTierStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ZoneTier, value: Double): Double = minOf(value, cap)
}

data class SegmentZoneTierAdjuster(var factors: MutableMap<String, Double>) : ZoneTierStep {
    override fun describe(): String = "by segment"

    override fun adjust(item: ZoneTier, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class ZoneTierAttemptsOffsetAdjuster(var weight: Double) : ZoneTierStep {
    override fun describe(): String = "plus attempts times $weight"

    override fun adjust(item: ZoneTier, value: Double): Double = value + item.attempts.toDouble() * weight
}

/** Runs the zone tier steps in order and keeps a trace of each. */
data class ZoneTierEngine(
    var steps: MutableList<ZoneTierStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: ZoneTier): Double {
        var current = item.weightedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ZoneTierStep = steps[index]
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

    fun evaluateAll(items: MutableList<ZoneTier>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultZoneTierEngine(): ZoneTierEngine {
    return ZoneTierEngine(
        steps = mutableListOf<ZoneTierStep>(
            RoundingZoneTierAdjuster(5.0),
            CapZoneTierAdjuster(23293.0),
            SegmentZoneTierAdjuster(
                mutableMapOf<String, Double>("east" to 1.32, "central" to 1.18, "west" to 1.37),
            ),
            ZoneTierAttemptsOffsetAdjuster(1.38),
        ),
        trace = mutableListOf<String>(),
        floor = 3.1,
        ceiling = 6335.6,
    )
}
