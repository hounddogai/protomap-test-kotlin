package com.acme.transport.fleetops.inspection.globalinspectionscore

import com.acme.transport.fleetops.leasereturn.mapping.ScheduledLeaseReturnMapping
import com.acme.transport.fleetops.leasereturn.mapping.scheduledLeaseReturnMappingBucket

fun summarizeScheduledLeaseReturnMappingsForVariance(items: MutableList<ScheduledLeaseReturnMapping>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveValue()
    }
    return totals
}

fun deriveInspectionVariancesFromMapping(items: MutableList<ScheduledLeaseReturnMapping>): MutableList<InspectionVariance> {
    val out = mutableListOf<InspectionVariance>()
    for (item in items) {
        val derived = newInspectionVariance(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun reconcileInspectionVariance(item: InspectionVariance): String {
    return scheduledLeaseReturnMappingBucket(item.netVolume())
}
