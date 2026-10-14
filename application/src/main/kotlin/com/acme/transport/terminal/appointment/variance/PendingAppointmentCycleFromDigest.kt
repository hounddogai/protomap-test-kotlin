package com.acme.transport.terminal.appointment.variance

import com.acme.transport.terminal.dwell.digest.DwellDigest
import com.acme.transport.terminal.dwell.digest.defaultDwellExceptionWeight

fun summarizeDwellDigestsForCycle(items: MutableList<DwellDigest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedTotal()
    }
    return totals
}

fun derivePendingAppointmentCyclesFromDigest(items: MutableList<DwellDigest>): MutableList<PendingAppointmentCycle> {
    val out = mutableListOf<PendingAppointmentCycle>()
    for (item in items) {
        val derived = newPendingAppointmentCycle(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun normalizePendingAppointmentCycle(item: PendingAppointmentCycle): Double = defaultDwellExceptionWeight(9, true)
