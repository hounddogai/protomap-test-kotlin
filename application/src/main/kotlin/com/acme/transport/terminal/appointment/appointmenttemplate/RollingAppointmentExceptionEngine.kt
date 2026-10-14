package com.acme.transport.terminal.appointment.appointmenttemplate

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the rolling appointment exception calculation. */
interface RollingAppointmentExceptionAdjuster {
    fun describe(): String
    fun adjust(item: RollingAppointmentException, value: Double): Double
}

data class CapRollingAppointmentExceptionAdjuster(var cap: Double) : RollingAppointmentExceptionAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RollingAppointmentException, value: Double): Double = minOf(value, cap)
}

data class PercentRollingAppointmentExceptionAdjuster(
    var percent: Double,
) : RollingAppointmentExceptionAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RollingAppointmentException, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingRollingAppointmentExceptionAdjuster(var step: Double) : RollingAppointmentExceptionAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RollingAppointmentException, value: Double): Double = roundTo(value, step)
}

data class MinimumRollingAppointmentExceptionAdjuster(
    var minimum: Double,
) : RollingAppointmentExceptionAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RollingAppointmentException, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the rolling appointment exception steps in order and keeps a trace of each. */
data class RollingAppointmentExceptionEngine(
    var steps: MutableList<RollingAppointmentExceptionAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: RollingAppointmentException): Double {
        var current = item.netVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RollingAppointmentException>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultRollingAppointmentExceptionEngine(): RollingAppointmentExceptionEngine {
    return RollingAppointmentExceptionEngine(
        steps = mutableListOf<RollingAppointmentExceptionAdjuster>(
            CapRollingAppointmentExceptionAdjuster(30378.0),
            PercentRollingAppointmentExceptionAdjuster(10.5),
            RoundingRollingAppointmentExceptionAdjuster(0.1),
            MinimumRollingAppointmentExceptionAdjuster(21.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.1,
        ceiling = 96713.5,
    )
}
