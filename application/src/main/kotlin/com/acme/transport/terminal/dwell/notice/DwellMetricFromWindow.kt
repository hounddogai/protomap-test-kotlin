package com.acme.transport.terminal.dwell.notice

import com.acme.transport.terminal.appointment.allocation.AppointmentWindow
import com.acme.transport.terminal.appointment.allocation.appointmentClaimBucket

fun summarizeAppointmentWindowsForMetric(items: MutableList<AppointmentWindow>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedVolume()
    }
    return totals
}

fun deriveDwellMetricsFromWindow(items: MutableList<AppointmentWindow>): MutableList<DwellMetric> {
    val out = mutableListOf<DwellMetric>()
    for (item in items) {
        val derived = newDwellMetric(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun planDwellMetric(item: DwellMetric): String = appointmentClaimBucket(item.effectiveValue())
