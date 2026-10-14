package com.acme.transport.terminal.crossdock.target

import com.acme.transport.terminal.appointment.preference.AppointmentScore
import com.acme.transport.terminal.appointment.preference.appointmentPreferenceBucket

fun summarizeAppointmentScoresForTarget(items: MutableList<AppointmentScore>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedTotal()
    }
    return totals
}

fun deriveCrossDockTargetsFromScore(items: MutableList<AppointmentScore>): MutableList<CrossDockTarget> {
    val out = mutableListOf<CrossDockTarget>()
    for (item in items) {
        val derived = newCrossDockTarget(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun classifyCrossDockTargetExtra(item: CrossDockTarget): String {
    return appointmentPreferenceBucket(item.measuredValue())
}
