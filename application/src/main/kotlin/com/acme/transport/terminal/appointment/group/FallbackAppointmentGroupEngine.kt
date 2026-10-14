package com.acme.transport.terminal.appointment.group

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the fallback appointment group calculation. */
interface FallbackAppointmentGroupStep {
    fun describe(): String
    fun adjust(item: FallbackAppointmentGroup, value: Double): Double
}

data class RoundingFallbackAppointmentGroupAdjuster(var step: Double) : FallbackAppointmentGroupStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: FallbackAppointmentGroup, value: Double): Double = roundTo(value, step)
}

data class PercentFallbackAppointmentGroupAdjuster(var percent: Double) : FallbackAppointmentGroupStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: FallbackAppointmentGroup, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredFallbackAppointmentGroupAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : FallbackAppointmentGroupStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: FallbackAppointmentGroup, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedFallbackAppointmentGroupAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : FallbackAppointmentGroupStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: FallbackAppointmentGroup, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the fallback appointment group steps in order and keeps a trace of each. */
data class FallbackAppointmentGroupEngine(
    var steps: MutableList<FallbackAppointmentGroupStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: FallbackAppointmentGroup): Double {
        var current = item.weightedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<FallbackAppointmentGroup>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultFallbackAppointmentGroupEngine(flags: Registry): FallbackAppointmentGroupEngine {
    return FallbackAppointmentGroupEngine(
        steps = mutableListOf<FallbackAppointmentGroupStep>(
            RoundingFallbackAppointmentGroupAdjuster(5.0),
            PercentFallbackAppointmentGroupAdjuster(9.7),
            TieredFallbackAppointmentGroupAdjuster(
                mutableListOf<Double>(6744.0, 7650.0, 8601.0, 9026.0),
                mutableListOf<Double>(0.82, 0.83, 1.05, 0.9),
            ),
            FlaggedFallbackAppointmentGroupAdjuster(flags, "fallback-appointment-group-capped", 7.7),
        ),
        trace = mutableListOf<String>(),
        floor = 3.8,
        ceiling = 63286.6,
    )
}
