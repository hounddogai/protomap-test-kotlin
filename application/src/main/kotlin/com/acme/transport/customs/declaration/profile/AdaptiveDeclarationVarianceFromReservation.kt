package com.acme.transport.customs.declaration.profile

import com.acme.transport.customs.inspection.forecast.RollingInspectionReservation
import com.acme.transport.customs.inspection.forecast.classifyRollingInspectionReservation

fun summarizeRollingInspectionReservationsForVariance(items: MutableList<RollingInspectionReservation>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedTotal()
    }
    return totals
}

fun deriveAdaptiveDeclarationVariancesFromReservation(items: MutableList<RollingInspectionReservation>): MutableList<AdaptiveDeclarationVariance> {
    val out = mutableListOf<AdaptiveDeclarationVariance>()
    for (item in items) {
        val derived = newAdaptiveDeclarationVariance(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun projectAdaptiveDeclarationVariance(item: AdaptiveDeclarationVariance): String {
    return classifyRollingInspectionReservation(item.weightedLoad())
}
