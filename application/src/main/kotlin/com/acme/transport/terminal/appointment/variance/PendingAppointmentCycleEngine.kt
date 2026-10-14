package com.acme.transport.terminal.appointment.variance

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the pending appointment cycle calculation. */
interface PendingAppointmentCycleModifier {
    fun describe(): String
    fun adjust(item: PendingAppointmentCycle, value: Double): Double
}

data class RoundingPendingAppointmentCycleAdjuster(var step: Double) : PendingAppointmentCycleModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: PendingAppointmentCycle, value: Double): Double = roundTo(value, step)
}

data class MinimumPendingAppointmentCycleAdjuster(var minimum: Double) : PendingAppointmentCycleModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PendingAppointmentCycle, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedPendingAppointmentCycleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PendingAppointmentCycleModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PendingAppointmentCycle, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PendingAppointmentCycleOdometerOffsetAdjuster(var weight: Double) : PendingAppointmentCycleModifier {
    override fun describe(): String = "plus odometer times $weight"

    override fun adjust(item: PendingAppointmentCycle, value: Double): Double {
        return value + item.odometer.toDouble() * weight
    }
}

/** Runs the pending appointment cycle steps in order and keeps a trace of each. */
data class PendingAppointmentCycleEngine(
    var steps: MutableList<PendingAppointmentCycleModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: PendingAppointmentCycle): Double {
        var current = item.netLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<PendingAppointmentCycle>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultPendingAppointmentCycleEngine(flags: Registry): PendingAppointmentCycleEngine {
    return PendingAppointmentCycleEngine(
        steps = mutableListOf<PendingAppointmentCycleModifier>(
            RoundingPendingAppointmentCycleAdjuster(0.25),
            MinimumPendingAppointmentCycleAdjuster(31.5),
            FlaggedPendingAppointmentCycleAdjuster(flags, "pending-appointment-cycle-scheduled", 7.5),
            PendingAppointmentCycleOdometerOffsetAdjuster(1.7),
        ),
        trace = mutableListOf<String>(),
        floor = 0.6,
        ceiling = 83651.8,
    )
}
