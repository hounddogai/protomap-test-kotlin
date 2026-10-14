package com.acme.transport.fleetops.serviceplan.target

import com.acme.transport.fleetops.utilization.utilizationreservation.ManualUtilizationReservation
import com.acme.transport.fleetops.utilization.utilizationreservation.normalizeUtilizationProfileCode

fun summarizeManualUtilizationReservationsForTarget(items: MutableList<ManualUtilizationReservation>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveVolume()
    }
    return totals
}

fun deriveServicePlanTargetsFromReservation(items: MutableList<ManualUtilizationReservation>): MutableList<ServicePlanTarget> {
    val out = mutableListOf<ServicePlanTarget>()
    for (item in items) {
        val derived = newServicePlanTarget(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun allocateServicePlanTarget(item: ServicePlanTarget): String = normalizeUtilizationProfileCode(item.code)
