package com.acme.transport.terminal.appointment.regionalappointmentmapping

import com.acme.transport.kit.numbers.roundTo

/** One step of the lenient appointment mapping calculation. */
interface LenientAppointmentMappingAdjuster {
    fun describe(): String
    fun adjust(item: LenientAppointmentMapping, value: Double): Double
}

data class LenientAppointmentMappingWindowDaysOffsetAdjuster(
    var weight: Double,
) : LenientAppointmentMappingAdjuster {
    override fun describe(): String = "plus window days times $weight"

    override fun adjust(item: LenientAppointmentMapping, value: Double): Double {
        return value + item.windowDays.toDouble() * weight
    }
}

data class CapLenientAppointmentMappingAdjuster(var cap: Double) : LenientAppointmentMappingAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LenientAppointmentMapping, value: Double): Double = minOf(value, cap)
}

data class RoundingLenientAppointmentMappingAdjuster(var step: Double) : LenientAppointmentMappingAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LenientAppointmentMapping, value: Double): Double = roundTo(value, step)
}

data class MinimumLenientAppointmentMappingAdjuster(var minimum: Double) : LenientAppointmentMappingAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LenientAppointmentMapping, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the lenient appointment mapping steps in order and keeps a trace of each. */
data class LenientAppointmentMappingEngine(
    var steps: MutableList<LenientAppointmentMappingAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: LenientAppointmentMapping): Double {
        var current = item.projectedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LenientAppointmentMappingAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<LenientAppointmentMapping>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultLenientAppointmentMappingEngine(): LenientAppointmentMappingEngine {
    return LenientAppointmentMappingEngine(
        steps = mutableListOf<LenientAppointmentMappingAdjuster>(
            LenientAppointmentMappingWindowDaysOffsetAdjuster(0.09),
            CapLenientAppointmentMappingAdjuster(33953.0),
            RoundingLenientAppointmentMappingAdjuster(0.25),
            MinimumLenientAppointmentMappingAdjuster(40.2),
        ),
        trace = mutableListOf<String>(),
        floor = 0.6,
        ceiling = 17381.5,
    )
}
