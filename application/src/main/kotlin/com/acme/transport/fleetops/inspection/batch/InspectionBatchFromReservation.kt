package com.acme.transport.fleetops.inspection.batch

import com.acme.transport.fleetops.tire.preference.RollingTireReservation
import com.acme.transport.fleetops.tire.preference.normalizeRollingTireReservationCode

fun summarizeRollingTireReservationsForBatch(items: MutableList<RollingTireReservation>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedTotal()
    }
    return totals
}

fun deriveInspectionBatchesFromReservation(items: MutableList<RollingTireReservation>): MutableList<InspectionBatch> {
    val out = mutableListOf<InspectionBatch>()
    for (item in items) {
        val derived = newInspectionBatch(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun mergeInspectionBatch(item: InspectionBatch): String = normalizeRollingTireReservationCode(item.code)
