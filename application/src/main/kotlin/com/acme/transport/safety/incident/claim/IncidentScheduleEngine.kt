package com.acme.transport.safety.incident.claim

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the incident schedule calculation. */
interface IncidentScheduleFactor {
    fun describe(): String
    fun adjust(item: IncidentSchedule, value: Double): Double
}

data class RoundingIncidentScheduleAdjuster(var step: Double) : IncidentScheduleFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: IncidentSchedule, value: Double): Double = roundTo(value, step)
}

data class CapIncidentScheduleAdjuster(var cap: Double) : IncidentScheduleFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: IncidentSchedule, value: Double): Double = minOf(value, cap)
}

data class FlaggedIncidentScheduleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : IncidentScheduleFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: IncidentSchedule, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredIncidentScheduleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : IncidentScheduleFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: IncidentSchedule, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the incident schedule steps in order and keeps a trace of each. */
data class IncidentScheduleEngine(
    var steps: MutableList<IncidentScheduleFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: IncidentSchedule): Double {
        var current = item.projectedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<IncidentSchedule>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultIncidentScheduleEngine(flags: Registry): IncidentScheduleEngine {
    return IncidentScheduleEngine(
        steps = mutableListOf<IncidentScheduleFactor>(
            RoundingIncidentScheduleAdjuster(0.05),
            CapIncidentScheduleAdjuster(47538.0),
            FlaggedIncidentScheduleAdjuster(flags, "incident-schedule-strict", 3.9),
            TieredIncidentScheduleAdjuster(
                mutableListOf<Double>(3142.0, 5169.0, 9597.0),
                mutableListOf<Double>(1.19, 1.13, 1.08),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 3.0,
        ceiling = 88314.9,
    )
}
