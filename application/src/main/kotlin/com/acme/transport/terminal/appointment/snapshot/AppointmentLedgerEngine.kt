package com.acme.transport.terminal.appointment.snapshot

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the appointment ledger calculation. */
interface AppointmentLedgerFactor {
    fun describe(): String
    fun adjust(item: AppointmentLedger, value: Double): Double
}

data class CapAppointmentLedgerAdjuster(var cap: Double) : AppointmentLedgerFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AppointmentLedger, value: Double): Double = minOf(value, cap)
}

data class FlaggedAppointmentLedgerAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : AppointmentLedgerFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: AppointmentLedger, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentAppointmentLedgerAdjuster(var percent: Double) : AppointmentLedgerFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: AppointmentLedger, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the appointment ledger steps in order and keeps a trace of each. */
data class AppointmentLedgerEngine(
    var steps: MutableList<AppointmentLedgerFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: AppointmentLedger): Double {
        var current = item.effectiveLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<AppointmentLedger>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultAppointmentLedgerEngine(flags: Registry): AppointmentLedgerEngine {
    return AppointmentLedgerEngine(
        steps = mutableListOf<AppointmentLedgerFactor>(
            CapAppointmentLedgerAdjuster(20517.0),
            FlaggedAppointmentLedgerAdjuster(flags, "appointment-ledger-primary", 7.8),
            PercentAppointmentLedgerAdjuster(23.8),
        ),
        trace = mutableListOf<String>(),
        floor = 2.7,
        ceiling = 7447.9,
    )
}
