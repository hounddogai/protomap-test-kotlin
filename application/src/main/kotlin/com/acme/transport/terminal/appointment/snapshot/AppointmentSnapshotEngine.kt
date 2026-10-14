package com.acme.transport.terminal.appointment.snapshot

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the appointment snapshot calculation. */
interface AppointmentSnapshotFactor {
    fun describe(): String
    fun adjust(item: AppointmentSnapshot, value: Double): Double
}

data class RoundingAppointmentSnapshotAdjuster(var step: Double) : AppointmentSnapshotFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: AppointmentSnapshot, value: Double): Double = roundTo(value, step)
}

data class MinimumAppointmentSnapshotAdjuster(var minimum: Double) : AppointmentSnapshotFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AppointmentSnapshot, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredAppointmentSnapshotAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : AppointmentSnapshotFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: AppointmentSnapshot, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentAppointmentSnapshotAdjuster(var percent: Double) : AppointmentSnapshotFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: AppointmentSnapshot, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapAppointmentSnapshotAdjuster(var cap: Double) : AppointmentSnapshotFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AppointmentSnapshot, value: Double): Double = minOf(value, cap)
}

data class FlaggedAppointmentSnapshotAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : AppointmentSnapshotFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: AppointmentSnapshot, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the appointment snapshot steps in order and keeps a trace of each. */
data class AppointmentSnapshotEngine(
    var steps: MutableList<AppointmentSnapshotFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: AppointmentSnapshot): Double {
        var current = item.adjustedVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<AppointmentSnapshot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultAppointmentSnapshotEngine(flags: Registry): AppointmentSnapshotEngine {
    return AppointmentSnapshotEngine(
        steps = mutableListOf<AppointmentSnapshotFactor>(
            RoundingAppointmentSnapshotAdjuster(1.0),
            MinimumAppointmentSnapshotAdjuster(33.9),
            TieredAppointmentSnapshotAdjuster(
                mutableListOf<Double>(5183.0, 8384.0, 8994.0),
                mutableListOf<Double>(1.14, 1.06, 1.15),
            ),
            PercentAppointmentSnapshotAdjuster(-3.2),
            CapAppointmentSnapshotAdjuster(39291.0),
            FlaggedAppointmentSnapshotAdjuster(flags, "appointment-snapshot-lenient", -7.8),
        ),
        trace = mutableListOf<String>(),
        floor = 3.7,
        ceiling = 99854.7,
    )
}
