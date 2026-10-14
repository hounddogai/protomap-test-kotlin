package com.acme.transport.customs.inspection.inspectionplan

import com.acme.transport.customs.inspection.plan.AdaptiveInspectionThreshold
import com.acme.transport.customs.inspection.plan.pendingInspectionAdjustmentWeight

fun summarizeAdaptiveInspectionThresholdsForReservation(items: MutableList<AdaptiveInspectionThreshold>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netVolume()
    }
    return totals
}

fun derivePrimaryInspectionReservationsFromThreshold(items: MutableList<AdaptiveInspectionThreshold>): MutableList<PrimaryInspectionReservation> {
    val out = mutableListOf<PrimaryInspectionReservation>()
    for (item in items) {
        val derived = newPrimaryInspectionReservation(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun planPrimaryInspectionReservation(item: PrimaryInspectionReservation): Double {
    return pendingInspectionAdjustmentWeight(7, true)
}
