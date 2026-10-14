package com.acme.transport.terminal.appointment.summary

import com.acme.transport.terminal.dwell.ledger.TieredDwellLedger
import com.acme.transport.terminal.dwell.ledger.tieredDwellLedgerBucket

fun summarizeTieredDwellLedgersForCheckpoint(items: MutableList<TieredDwellLedger>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedLoad()
    }
    return totals
}

fun deriveAppointmentCheckpointsFromLedger(items: MutableList<TieredDwellLedger>): MutableList<AppointmentCheckpoint> {
    val out = mutableListOf<AppointmentCheckpoint>()
    for (item in items) {
        val derived = newAppointmentCheckpoint(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun resolveAppointmentCheckpoint(item: AppointmentCheckpoint): String {
    return tieredDwellLedgerBucket(item.weightedExposure())
}
