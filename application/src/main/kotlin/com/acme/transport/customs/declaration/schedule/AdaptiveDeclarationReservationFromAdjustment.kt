package com.acme.transport.customs.declaration.schedule

import com.acme.transport.customs.clearance.cycle.RollingClearanceAdjustment
import com.acme.transport.customs.clearance.cycle.rollingClearanceAdjustmentBucket

fun summarizeRollingClearanceAdjustmentsForReservation(items: MutableList<RollingClearanceAdjustment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveExposure()
    }
    return totals
}

fun deriveAdaptiveDeclarationReservationsFromAdjustment(items: MutableList<RollingClearanceAdjustment>): MutableList<AdaptiveDeclarationReservation> {
    val out = mutableListOf<AdaptiveDeclarationReservation>()
    for (item in items) {
        val derived = newAdaptiveDeclarationReservation(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun deriveAdaptiveDeclarationReservation(item: AdaptiveDeclarationReservation): String {
    return rollingClearanceAdjustmentBucket(item.effectiveLoad())
}
