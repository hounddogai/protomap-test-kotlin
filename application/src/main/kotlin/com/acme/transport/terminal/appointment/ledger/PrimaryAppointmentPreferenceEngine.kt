package com.acme.transport.terminal.appointment.ledger

import com.acme.transport.kit.numbers.roundTo

/** One step of the primary appointment preference calculation. */
interface PrimaryAppointmentPreferenceAdjuster {
    fun describe(): String
    fun adjust(item: PrimaryAppointmentPreference, value: Double): Double
}

data class UnitPrimaryAppointmentPreferenceAdjuster(
    var factors: MutableMap<String, Double>,
) : PrimaryAppointmentPreferenceAdjuster {
    override fun describe(): String = "by unit"

    override fun adjust(item: PrimaryAppointmentPreference, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentPrimaryAppointmentPreferenceAdjuster(
    var percent: Double,
) : PrimaryAppointmentPreferenceAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: PrimaryAppointmentPreference, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapPrimaryAppointmentPreferenceAdjuster(var cap: Double) : PrimaryAppointmentPreferenceAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: PrimaryAppointmentPreference, value: Double): Double = minOf(value, cap)
}

data class RoundingPrimaryAppointmentPreferenceAdjuster(
    var step: Double,
) : PrimaryAppointmentPreferenceAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: PrimaryAppointmentPreference, value: Double): Double = roundTo(value, step)
}

/** Runs the primary appointment preference steps in order and keeps a trace of each. */
data class PrimaryAppointmentPreferenceEngine(
    var steps: MutableList<PrimaryAppointmentPreferenceAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: PrimaryAppointmentPreference): Double {
        var current = item.effectiveVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: PrimaryAppointmentPreferenceAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<PrimaryAppointmentPreference>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultPrimaryAppointmentPreferenceEngine(): PrimaryAppointmentPreferenceEngine {
    return PrimaryAppointmentPreferenceEngine(
        steps = mutableListOf<PrimaryAppointmentPreferenceAdjuster>(
            UnitPrimaryAppointmentPreferenceAdjuster(
                mutableMapOf<String, Double>("south" to 1.02, "west" to 0.71, "north" to 0.73),
            ),
            PercentPrimaryAppointmentPreferenceAdjuster(2.0),
            CapPrimaryAppointmentPreferenceAdjuster(44722.0),
            RoundingPrimaryAppointmentPreferenceAdjuster(0.5),
        ),
        trace = mutableListOf<String>(),
        floor = 3.4,
        ceiling = 77936.6,
    )
}
