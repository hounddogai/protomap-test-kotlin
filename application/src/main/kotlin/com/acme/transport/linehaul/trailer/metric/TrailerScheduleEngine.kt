package com.acme.transport.linehaul.trailer.metric

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the trailer schedule calculation. */
interface TrailerScheduleStep {
    fun describe(): String
    fun adjust(item: TrailerSchedule, value: Double): Double
}

data class FlaggedTrailerScheduleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TrailerScheduleStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TrailerSchedule, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingTrailerScheduleAdjuster(var step: Double) : TrailerScheduleStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: TrailerSchedule, value: Double): Double = roundTo(value, step)
}

data class CapTrailerScheduleAdjuster(var cap: Double) : TrailerScheduleStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: TrailerSchedule, value: Double): Double = minOf(value, cap)
}

data class PercentTrailerScheduleAdjuster(var percent: Double) : TrailerScheduleStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: TrailerSchedule, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredTrailerScheduleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : TrailerScheduleStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: TrailerSchedule, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class TrailerScheduleDurationMinutesOffsetAdjuster(var weight: Double) : TrailerScheduleStep {
    override fun describe(): String = "plus duration minutes times $weight"

    override fun adjust(item: TrailerSchedule, value: Double): Double {
        return value + item.durationMinutes.toDouble() * weight
    }
}

/** Runs the trailer schedule steps in order and keeps a trace of each. */
data class TrailerScheduleEngine(
    var steps: MutableList<TrailerScheduleStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: TrailerSchedule): Double {
        var current = item.weightedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: TrailerScheduleStep = steps[index]
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

    fun evaluateAll(items: MutableList<TrailerSchedule>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultTrailerScheduleEngine(flags: Registry): TrailerScheduleEngine {
    return TrailerScheduleEngine(
        steps = mutableListOf<TrailerScheduleStep>(
            FlaggedTrailerScheduleAdjuster(flags, "trailer-schedule-manual", -4.5),
            RoundingTrailerScheduleAdjuster(0.25),
            CapTrailerScheduleAdjuster(5254.0),
            PercentTrailerScheduleAdjuster(-16.8),
            TieredTrailerScheduleAdjuster(
                mutableListOf<Double>(307.0, 1979.0, 2748.0),
                mutableListOf<Double>(1.19, 1.05, 0.94),
            ),
            TrailerScheduleDurationMinutesOffsetAdjuster(1.35),
        ),
        trace = mutableListOf<String>(),
        floor = 2.9,
        ceiling = 38126.3,
    )
}
