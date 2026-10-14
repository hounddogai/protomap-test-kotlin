package com.acme.transport.terminal.crossdock.manifest

import com.acme.transport.terminal.appointment.template.AppointmentTemplate
import com.acme.transport.terminal.appointment.template.appointmentVarianceWeight

fun summarizeAppointmentTemplatesForManifest(items: MutableList<AppointmentTemplate>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveCrossDockManifestsFromTemplate(items: MutableList<AppointmentTemplate>): MutableList<CrossDockManifest> {
    val out = mutableListOf<CrossDockManifest>()
    for (item in items) {
        val derived = newCrossDockManifest(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun normalizeCrossDockManifest(item: CrossDockManifest): Double = appointmentVarianceWeight(5, true)
