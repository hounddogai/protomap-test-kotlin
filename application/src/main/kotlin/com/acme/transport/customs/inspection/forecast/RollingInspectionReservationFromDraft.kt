package com.acme.transport.customs.inspection.forecast

import com.acme.transport.customs.clearance.tier.TieredClearanceDraft
import com.acme.transport.customs.clearance.tier.clearanceEstimateWeight

fun summarizeTieredClearanceDraftsForReservation(items: MutableList<TieredClearanceDraft>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netLoad()
    }
    return totals
}

fun deriveRollingInspectionReservationsFromDraft(items: MutableList<TieredClearanceDraft>): MutableList<RollingInspectionReservation> {
    val out = mutableListOf<RollingInspectionReservation>()
    for (item in items) {
        val derived = newRollingInspectionReservation(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun settleRollingInspectionReservation(item: RollingInspectionReservation): Double {
    return clearanceEstimateWeight(4, true)
}
