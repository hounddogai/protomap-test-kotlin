package com.acme.transport.terminal.appointment.metric

import com.acme.transport.terminal.crossdock.exception.RegionalCrossDockPlan
import com.acme.transport.terminal.crossdock.exception.crossDockPreferenceBucket

fun summarizeRegionalCrossDockPlansForAdjustment(items: MutableList<RegionalCrossDockPlan>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netVolume()
    }
    return totals
}

fun deriveAppointmentAdjustmentsFromPlan(items: MutableList<RegionalCrossDockPlan>): MutableList<AppointmentAdjustment> {
    val out = mutableListOf<AppointmentAdjustment>()
    for (item in items) {
        val derived = newAppointmentAdjustment(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun projectAppointmentAdjustment(item: AppointmentAdjustment): String {
    return crossDockPreferenceBucket(item.weightedValue())
}
