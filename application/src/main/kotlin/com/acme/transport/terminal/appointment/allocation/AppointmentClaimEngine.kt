package com.acme.transport.terminal.appointment.allocation

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the appointment claim calculation. */
interface AppointmentClaimModifier {
    fun describe(): String
    fun adjust(item: AppointmentClaim, value: Double): Double
}

data class RoundingAppointmentClaimAdjuster(var step: Double) : AppointmentClaimModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: AppointmentClaim, value: Double): Double = roundTo(value, step)
}

data class CapAppointmentClaimAdjuster(var cap: Double) : AppointmentClaimModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AppointmentClaim, value: Double): Double = minOf(value, cap)
}

data class TieredAppointmentClaimAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : AppointmentClaimModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: AppointmentClaim, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedAppointmentClaimAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : AppointmentClaimModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: AppointmentClaim, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class AppointmentClaimCreatedAtOffsetAdjuster(var weight: Double) : AppointmentClaimModifier {
    override fun describe(): String = "plus created at times $weight"

    override fun adjust(item: AppointmentClaim, value: Double): Double = value + item.createdAt.toDouble() * weight
}

data class PercentAppointmentClaimAdjuster(var percent: Double) : AppointmentClaimModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: AppointmentClaim, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the appointment claim steps in order and keeps a trace of each. */
data class AppointmentClaimEngine(
    var steps: MutableList<AppointmentClaimModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: AppointmentClaim): Double {
        var current = item.adjustedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: AppointmentClaimModifier = steps[index]
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

    fun evaluateAll(items: MutableList<AppointmentClaim>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultAppointmentClaimEngine(flags: Registry): AppointmentClaimEngine {
    return AppointmentClaimEngine(
        steps = mutableListOf<AppointmentClaimModifier>(
            RoundingAppointmentClaimAdjuster(0.5),
            CapAppointmentClaimAdjuster(9165.0),
            TieredAppointmentClaimAdjuster(
                mutableListOf<Double>(6353.0, 6429.0, 9264.0, 9607.0),
                mutableListOf<Double>(1.0, 0.82, 1.15, 1.16),
            ),
            FlaggedAppointmentClaimAdjuster(flags, "appointment-claim-fallback", -9.3),
            AppointmentClaimCreatedAtOffsetAdjuster(0.5),
            PercentAppointmentClaimAdjuster(16.8),
        ),
        trace = mutableListOf<String>(),
        floor = 1.9,
        ceiling = 11500.6,
    )
}
