package com.acme.transport.fleetops.utilization.utilizationreservation

import com.acme.transport.fleetops.utilization.reservation.UtilizationAssignment
import com.acme.transport.fleetops.utilization.reservation.normalizeUtilizationReservationCode

fun summarizeUtilizationAssignmentsForMapping(items: MutableList<UtilizationAssignment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveLoad()
    }
    return totals
}

fun deriveGlobalUtilizationMappingsFromAssignment(items: MutableList<UtilizationAssignment>): MutableList<GlobalUtilizationMapping> {
    val out = mutableListOf<GlobalUtilizationMapping>()
    for (item in items) {
        val derived = newGlobalUtilizationMapping(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun matchGlobalUtilizationMapping(item: GlobalUtilizationMapping): String {
    return normalizeUtilizationReservationCode(item.code)
}
