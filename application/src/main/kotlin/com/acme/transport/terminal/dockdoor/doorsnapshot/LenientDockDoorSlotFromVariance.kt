package com.acme.transport.terminal.dockdoor.doorsnapshot

import com.acme.transport.terminal.appointment.seasonalappointmentvariance.PendingAppointmentVariance
import com.acme.transport.terminal.appointment.seasonalappointmentvariance.normalizePendingAppointmentVarianceCode

fun summarizePendingAppointmentVariancesForSlot(items: MutableList<PendingAppointmentVariance>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveLenientDockDoorSlotsFromVariance(items: MutableList<PendingAppointmentVariance>): MutableList<LenientDockDoorSlot> {
    val out = mutableListOf<LenientDockDoorSlot>()
    for (item in items) {
        val derived = newLenientDockDoorSlot(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun computeLenientDockDoorSlot(item: LenientDockDoorSlot): String {
    return normalizePendingAppointmentVarianceCode(item.code)
}
