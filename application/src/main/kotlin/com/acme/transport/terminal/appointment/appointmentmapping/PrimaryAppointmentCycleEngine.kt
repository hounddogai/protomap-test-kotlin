package com.acme.transport.terminal.appointment.appointmentmapping

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the primary appointment cycle calculation. */
interface PrimaryAppointmentCycleFactor {
    fun describe(): String
    fun adjust(item: PrimaryAppointmentCycle, value: Double): Double
}

data class PercentPrimaryAppointmentCycleAdjuster(var percent: Double) : PrimaryAppointmentCycleFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: PrimaryAppointmentCycle, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumPrimaryAppointmentCycleAdjuster(var minimum: Double) : PrimaryAppointmentCycleFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PrimaryAppointmentCycle, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapPrimaryAppointmentCycleAdjuster(var cap: Double) : PrimaryAppointmentCycleFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: PrimaryAppointmentCycle, value: Double): Double = minOf(value, cap)
}

/** Runs the primary appointment cycle steps in order and keeps a trace of each. */
data class PrimaryAppointmentCycleEngine(
    var steps: MutableList<PrimaryAppointmentCycleFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: PrimaryAppointmentCycle): Double {
        var current = item.weightedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<PrimaryAppointmentCycle>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultPrimaryAppointmentCycleEngine(): PrimaryAppointmentCycleEngine {
    return PrimaryAppointmentCycleEngine(
        steps = mutableListOf<PrimaryAppointmentCycleFactor>(
            PercentPrimaryAppointmentCycleAdjuster(21.5),
            MinimumPrimaryAppointmentCycleAdjuster(25.3),
            CapPrimaryAppointmentCycleAdjuster(23006.0),
        ),
        trace = mutableListOf<String>(),
        floor = 4.3,
        ceiling = 3204.1,
    )
}
