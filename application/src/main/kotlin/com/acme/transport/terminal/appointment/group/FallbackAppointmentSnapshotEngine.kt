package com.acme.transport.terminal.appointment.group

import com.acme.transport.kit.flags.Registry

/** One step of the fallback appointment snapshot calculation. */
interface FallbackAppointmentSnapshotStep {
    fun describe(): String
    fun adjust(item: FallbackAppointmentSnapshot, value: Double): Double
}

data class FallbackAppointmentSnapshotAttemptsOffsetAdjuster(
    var weight: Double,
) : FallbackAppointmentSnapshotStep {
    override fun describe(): String = "plus attempts times $weight"

    override fun adjust(item: FallbackAppointmentSnapshot, value: Double): Double {
        return value + item.attempts.toDouble() * weight
    }
}

data class CapFallbackAppointmentSnapshotAdjuster(var cap: Double) : FallbackAppointmentSnapshotStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: FallbackAppointmentSnapshot, value: Double): Double = minOf(value, cap)
}

data class FlaggedFallbackAppointmentSnapshotAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : FallbackAppointmentSnapshotStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: FallbackAppointmentSnapshot, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentFallbackAppointmentSnapshotAdjuster(var percent: Double) : FallbackAppointmentSnapshotStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: FallbackAppointmentSnapshot, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class LabelFallbackAppointmentSnapshotAdjuster(
    var factors: MutableMap<String, Double>,
) : FallbackAppointmentSnapshotStep {
    override fun describe(): String = "by label"

    override fun adjust(item: FallbackAppointmentSnapshot, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumFallbackAppointmentSnapshotAdjuster(var minimum: Double) : FallbackAppointmentSnapshotStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: FallbackAppointmentSnapshot, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the fallback appointment snapshot steps in order and keeps a trace of each. */
data class FallbackAppointmentSnapshotEngine(
    var steps: MutableList<FallbackAppointmentSnapshotStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: FallbackAppointmentSnapshot): Double {
        var current = item.adjustedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: FallbackAppointmentSnapshotStep = steps[index]
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

    fun evaluateAll(items: MutableList<FallbackAppointmentSnapshot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultFallbackAppointmentSnapshotEngine(flags: Registry): FallbackAppointmentSnapshotEngine {
    return FallbackAppointmentSnapshotEngine(
        steps = mutableListOf<FallbackAppointmentSnapshotStep>(
            FallbackAppointmentSnapshotAttemptsOffsetAdjuster(0.5),
            CapFallbackAppointmentSnapshotAdjuster(25940.0),
            FlaggedFallbackAppointmentSnapshotAdjuster(flags, "fallback-appointment-snapshot-strict", 9.8),
            PercentFallbackAppointmentSnapshotAdjuster(13.2),
            LabelFallbackAppointmentSnapshotAdjuster(
                mutableMapOf<String, Double>("north" to 0.99, "online" to 1.25),
            ),
            MinimumFallbackAppointmentSnapshotAdjuster(19.6),
        ),
        trace = mutableListOf<String>(),
        floor = 4.7,
        ceiling = 45971.6,
    )
}
