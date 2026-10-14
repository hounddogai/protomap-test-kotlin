package com.acme.transport.linehaul.trailer.threshold

import com.acme.transport.kit.numbers.roundTo

/** One step of the scheduled trailer window calculation. */
interface ScheduledTrailerWindowAdjuster {
    fun describe(): String
    fun adjust(item: ScheduledTrailerWindow, value: Double): Double
}

data class PercentScheduledTrailerWindowAdjuster(var percent: Double) : ScheduledTrailerWindowAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ScheduledTrailerWindow, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class ScheduledTrailerWindowOdometerOffsetAdjuster(var weight: Double) : ScheduledTrailerWindowAdjuster {
    override fun describe(): String = "plus odometer times $weight"

    override fun adjust(item: ScheduledTrailerWindow, value: Double): Double {
        return value + item.odometer.toDouble() * weight
    }
}

data class RoundingScheduledTrailerWindowAdjuster(var step: Double) : ScheduledTrailerWindowAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ScheduledTrailerWindow, value: Double): Double = roundTo(value, step)
}

/** Runs the scheduled trailer window steps in order and keeps a trace of each. */
data class ScheduledTrailerWindowEngine(
    var steps: MutableList<ScheduledTrailerWindowAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: ScheduledTrailerWindow): Double {
        var current = item.adjustedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ScheduledTrailerWindowAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<ScheduledTrailerWindow>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultScheduledTrailerWindowEngine(): ScheduledTrailerWindowEngine {
    return ScheduledTrailerWindowEngine(
        steps = mutableListOf<ScheduledTrailerWindowAdjuster>(
            PercentScheduledTrailerWindowAdjuster(-17.2),
            ScheduledTrailerWindowOdometerOffsetAdjuster(1.61),
            RoundingScheduledTrailerWindowAdjuster(5.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.0,
        ceiling = 45647.4,
    )
}
