package com.acme.transport.terminal.appointment.score

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the appointment cycle calculation. */
interface AppointmentCycleModifier {
    fun describe(): String
    fun adjust(item: AppointmentCycle, value: Double): Double
}

data class TieredAppointmentCycleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : AppointmentCycleModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: AppointmentCycle, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapAppointmentCycleAdjuster(var cap: Double) : AppointmentCycleModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AppointmentCycle, value: Double): Double = minOf(value, cap)
}

data class RoundingAppointmentCycleAdjuster(var step: Double) : AppointmentCycleModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: AppointmentCycle, value: Double): Double = roundTo(value, step)
}

data class FlaggedAppointmentCycleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : AppointmentCycleModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: AppointmentCycle, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentAppointmentCycleAdjuster(var percent: Double) : AppointmentCycleModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: AppointmentCycle, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class SourceAppointmentCycleAdjuster(var factors: MutableMap<String, Double>) : AppointmentCycleModifier {
    override fun describe(): String = "by source"

    override fun adjust(item: AppointmentCycle, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the appointment cycle steps in order and keeps a trace of each. */
data class AppointmentCycleEngine(
    var steps: MutableList<AppointmentCycleModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: AppointmentCycle): Double {
        var current = item.adjustedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: AppointmentCycleModifier = steps[index]
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

    fun evaluateAll(items: MutableList<AppointmentCycle>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultAppointmentCycleEngine(flags: Registry): AppointmentCycleEngine {
    return AppointmentCycleEngine(
        steps = mutableListOf<AppointmentCycleModifier>(
            TieredAppointmentCycleAdjuster(
                mutableListOf<Double>(1295.0, 6766.0, 8458.0),
                mutableListOf<Double>(0.96, 1.01, 0.81),
            ),
            CapAppointmentCycleAdjuster(22320.0),
            RoundingAppointmentCycleAdjuster(1.0),
            FlaggedAppointmentCycleAdjuster(flags, "appointment-cycle-batched", -8.2),
            PercentAppointmentCycleAdjuster(-6.5),
            SourceAppointmentCycleAdjuster(mutableMapOf<String, Double>("north" to 1.34)),
        ),
        trace = mutableListOf<String>(),
        floor = 0.8,
        ceiling = 80290.1,
    )
}
