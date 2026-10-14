package com.acme.transport.terminal.appointment.threshold

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the strict appointment mapping calculation. */
interface StrictAppointmentMappingStep {
    fun describe(): String
    fun adjust(item: StrictAppointmentMapping, value: Double): Double
}

data class PercentStrictAppointmentMappingAdjuster(var percent: Double) : StrictAppointmentMappingStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: StrictAppointmentMapping, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class StrictAppointmentMappingEtaMinutesOffsetAdjuster(var weight: Double) : StrictAppointmentMappingStep {
    override fun describe(): String = "plus eta minutes times $weight"

    override fun adjust(item: StrictAppointmentMapping, value: Double): Double {
        return value + item.etaMinutes.toDouble() * weight
    }
}

data class FlaggedStrictAppointmentMappingAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : StrictAppointmentMappingStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: StrictAppointmentMapping, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingStrictAppointmentMappingAdjuster(var step: Double) : StrictAppointmentMappingStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: StrictAppointmentMapping, value: Double): Double = roundTo(value, step)
}

data class CapStrictAppointmentMappingAdjuster(var cap: Double) : StrictAppointmentMappingStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: StrictAppointmentMapping, value: Double): Double = minOf(value, cap)
}

data class TieredStrictAppointmentMappingAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : StrictAppointmentMappingStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: StrictAppointmentMapping, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the strict appointment mapping steps in order and keeps a trace of each. */
data class StrictAppointmentMappingEngine(
    var steps: MutableList<StrictAppointmentMappingStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: StrictAppointmentMapping): Double {
        var current = item.adjustedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: StrictAppointmentMappingStep = steps[index]
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

    fun evaluateAll(items: MutableList<StrictAppointmentMapping>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultStrictAppointmentMappingEngine(flags: Registry): StrictAppointmentMappingEngine {
    return StrictAppointmentMappingEngine(
        steps = mutableListOf<StrictAppointmentMappingStep>(
            PercentStrictAppointmentMappingAdjuster(4.9),
            StrictAppointmentMappingEtaMinutesOffsetAdjuster(1.86),
            FlaggedStrictAppointmentMappingAdjuster(flags, "strict-appointment-mapping-weighted", 7.2),
            RoundingStrictAppointmentMappingAdjuster(1.0),
            CapStrictAppointmentMappingAdjuster(44724.0),
            TieredStrictAppointmentMappingAdjuster(
                mutableListOf<Double>(2738.0, 2935.0, 5876.0, 6216.0),
                mutableListOf<Double>(0.93, 0.99, 0.95, 0.97),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 4.3,
        ceiling = 27275.2,
    )
}
