package com.acme.transport.terminal.appointment.appointmentrollup

import com.acme.transport.terminal.crossdock.template.CrossDockThreshold
import com.acme.transport.terminal.crossdock.template.crossDockTemplateWeight

fun summarizeCrossDockThresholdsForEntry(items: MutableList<CrossDockThreshold>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedValue()
    }
    return totals
}

fun deriveAppointmentEntriesFromThreshold(items: MutableList<CrossDockThreshold>): MutableList<AppointmentEntry> {
    val out = mutableListOf<AppointmentEntry>()
    for (item in items) {
        val derived = newAppointmentEntry(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun resolveAppointmentEntry(item: AppointmentEntry): Double = crossDockTemplateWeight(3, true)
