package com.acme.transport.fleetops.utilization.recorditem

import com.acme.transport.fleetops.serviceplan.planprofile.AdaptiveServicePlanRecord
import com.acme.transport.fleetops.serviceplan.planprofile.classifyFallbackServicePlanProfile

fun summarizeAdaptiveServicePlanRecordsForReservation(items: MutableList<AdaptiveServicePlanRecord>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedTotal()
    }
    return totals
}

fun deriveTieredUtilizationReservationsFromRecord(items: MutableList<AdaptiveServicePlanRecord>): MutableList<TieredUtilizationReservation> {
    val out = mutableListOf<TieredUtilizationReservation>()
    for (item in items) {
        val derived = newTieredUtilizationReservation(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun normalizeTieredUtilizationReservation(item: TieredUtilizationReservation): String {
    return classifyFallbackServicePlanProfile(item.projectedLoad())
}
