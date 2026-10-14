package com.acme.transport.terminal.appointment.bundle

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the default appointment manifest calculation. */
interface DefaultAppointmentManifestStep {
    fun describe(): String
    fun adjust(item: DefaultAppointmentManifest, value: Double): Double
}

data class CategoryDefaultAppointmentManifestAdjuster(
    var factors: MutableMap<String, Double>,
) : DefaultAppointmentManifestStep {
    override fun describe(): String = "by category"

    override fun adjust(item: DefaultAppointmentManifest, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingDefaultAppointmentManifestAdjuster(var step: Double) : DefaultAppointmentManifestStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DefaultAppointmentManifest, value: Double): Double = roundTo(value, step)
}

data class MinimumDefaultAppointmentManifestAdjuster(var minimum: Double) : DefaultAppointmentManifestStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DefaultAppointmentManifest, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedDefaultAppointmentManifestAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DefaultAppointmentManifestStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DefaultAppointmentManifest, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the default appointment manifest steps in order and keeps a trace of each. */
data class DefaultAppointmentManifestEngine(
    var steps: MutableList<DefaultAppointmentManifestStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: DefaultAppointmentManifest): Double {
        var current = item.effectiveVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: DefaultAppointmentManifestStep = steps[index]
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

    fun evaluateAll(items: MutableList<DefaultAppointmentManifest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultDefaultAppointmentManifestEngine(flags: Registry): DefaultAppointmentManifestEngine {
    return DefaultAppointmentManifestEngine(
        steps = mutableListOf<DefaultAppointmentManifestStep>(
            CategoryDefaultAppointmentManifestAdjuster(mutableMapOf<String, Double>("south" to 1.0)),
            RoundingDefaultAppointmentManifestAdjuster(0.5),
            MinimumDefaultAppointmentManifestAdjuster(30.2),
            FlaggedDefaultAppointmentManifestAdjuster(flags, "default-appointment-manifest-weighted", 8.2),
        ),
        trace = mutableListOf<String>(),
        floor = 2.8,
        ceiling = 87987.0,
    )
}
