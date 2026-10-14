package com.acme.transport.terminal.appointment.allocation

import com.acme.transport.terminal.appointment.preference.AppointmentScore
import com.acme.transport.terminal.appointment.preference.appointmentPreferenceWeight

fun summarizeAppointmentScoresForBaseline(items: MutableList<AppointmentScore>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedTotal()
    }
    return totals
}

fun deriveAppointmentBaselinesFromScore(items: MutableList<AppointmentScore>): MutableList<AppointmentBaseline> {
    val out = mutableListOf<AppointmentBaseline>()
    for (item in items) {
        val derived = newAppointmentBaseline(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun normalizeAppointmentBaseline(item: AppointmentBaseline): Double = appointmentPreferenceWeight(5, true)
