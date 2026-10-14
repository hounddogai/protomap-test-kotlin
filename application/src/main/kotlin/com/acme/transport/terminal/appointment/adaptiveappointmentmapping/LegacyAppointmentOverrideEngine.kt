package com.acme.transport.terminal.appointment.adaptiveappointmentmapping

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the legacy appointment override calculation. */
interface LegacyAppointmentOverrideStep {
    fun describe(): String
    fun adjust(item: LegacyAppointmentOverride, value: Double): Double
}

data class DepotLegacyAppointmentOverrideAdjuster(
    var factors: MutableMap<String, Double>,
) : LegacyAppointmentOverrideStep {
    override fun describe(): String = "by depot"

    override fun adjust(item: LegacyAppointmentOverride, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingLegacyAppointmentOverrideAdjuster(var step: Double) : LegacyAppointmentOverrideStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LegacyAppointmentOverride, value: Double): Double = roundTo(value, step)
}

data class LegacyAppointmentOverrideScoreOffsetAdjuster(var weight: Double) : LegacyAppointmentOverrideStep {
    override fun describe(): String = "plus score times $weight"

    override fun adjust(item: LegacyAppointmentOverride, value: Double): Double = value + item.score * weight
}

data class FlaggedLegacyAppointmentOverrideAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LegacyAppointmentOverrideStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LegacyAppointmentOverride, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapLegacyAppointmentOverrideAdjuster(var cap: Double) : LegacyAppointmentOverrideStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LegacyAppointmentOverride, value: Double): Double = minOf(value, cap)
}

/** Runs the legacy appointment override steps in order and keeps a trace of each. */
data class LegacyAppointmentOverrideEngine(
    var steps: MutableList<LegacyAppointmentOverrideStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: LegacyAppointmentOverride): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LegacyAppointmentOverrideStep = steps[index]
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

    fun evaluateAll(items: MutableList<LegacyAppointmentOverride>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultLegacyAppointmentOverrideEngine(flags: Registry): LegacyAppointmentOverrideEngine {
    return LegacyAppointmentOverrideEngine(
        steps = mutableListOf<LegacyAppointmentOverrideStep>(
            DepotLegacyAppointmentOverrideAdjuster(mutableMapOf<String, Double>("central" to 0.86)),
            RoundingLegacyAppointmentOverrideAdjuster(0.05),
            LegacyAppointmentOverrideScoreOffsetAdjuster(1.46),
            FlaggedLegacyAppointmentOverrideAdjuster(flags, "legacy-appointment-override-scheduled", -5.0),
            CapLegacyAppointmentOverrideAdjuster(47320.0),
        ),
        trace = mutableListOf<String>(),
        floor = 5.0,
        ceiling = 66767.0,
    )
}
