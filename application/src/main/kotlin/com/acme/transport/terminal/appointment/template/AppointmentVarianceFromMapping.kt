package com.acme.transport.terminal.appointment.template

import com.acme.transport.terminal.appointment.mapping.AppointmentMapping
import com.acme.transport.terminal.appointment.mapping.appointmentMappingWeight

fun summarizeAppointmentMappingsForVariance(items: MutableList<AppointmentMapping>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netValue()
    }
    return totals
}

fun deriveAppointmentVariancesFromMapping(items: MutableList<AppointmentMapping>): MutableList<AppointmentVariance> {
    val out = mutableListOf<AppointmentVariance>()
    for (item in items) {
        val derived = newAppointmentVariance(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun reconcileAppointmentVariance(item: AppointmentVariance): Double = appointmentMappingWeight(8, true)
