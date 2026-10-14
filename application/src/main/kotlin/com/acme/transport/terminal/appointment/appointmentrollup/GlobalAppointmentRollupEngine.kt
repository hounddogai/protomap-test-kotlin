package com.acme.transport.terminal.appointment.appointmentrollup

import com.acme.transport.kit.flags.Registry

/** One step of the global appointment rollup calculation. */
interface GlobalAppointmentRollupFactor {
    fun describe(): String
    fun adjust(item: GlobalAppointmentRollup, value: Double): Double
}

data class TieredGlobalAppointmentRollupAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : GlobalAppointmentRollupFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: GlobalAppointmentRollup, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapGlobalAppointmentRollupAdjuster(var cap: Double) : GlobalAppointmentRollupFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: GlobalAppointmentRollup, value: Double): Double = minOf(value, cap)
}

data class FlaggedGlobalAppointmentRollupAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : GlobalAppointmentRollupFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: GlobalAppointmentRollup, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the global appointment rollup steps in order and keeps a trace of each. */
data class GlobalAppointmentRollupEngine(
    var steps: MutableList<GlobalAppointmentRollupFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: GlobalAppointmentRollup): Double {
        var current = item.adjustedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: GlobalAppointmentRollupFactor = steps[index]
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

    fun evaluateAll(items: MutableList<GlobalAppointmentRollup>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultGlobalAppointmentRollupEngine(flags: Registry): GlobalAppointmentRollupEngine {
    return GlobalAppointmentRollupEngine(
        steps = mutableListOf<GlobalAppointmentRollupFactor>(
            TieredGlobalAppointmentRollupAdjuster(
                mutableListOf<Double>(6396.0, 6465.0, 7755.0, 8172.0),
                mutableListOf<Double>(1.16, 0.88, 1.18, 0.96),
            ),
            CapGlobalAppointmentRollupAdjuster(3565.0),
            FlaggedGlobalAppointmentRollupAdjuster(flags, "global-appointment-rollup-pending", 9.0),
        ),
        trace = mutableListOf<String>(),
        floor = 4.8,
        ceiling = 44183.8,
    )
}
