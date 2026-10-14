package com.acme.transport.fleetops.inspection.inspectiongroup

import com.acme.transport.fleetops.serviceplan.planrollup.ServicePlanRecord
import com.acme.transport.fleetops.serviceplan.planrollup.normalizePrimaryServicePlanAssignmentCode

fun summarizeServicePlanRecordsForEntry(items: MutableList<ServicePlanRecord>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedLoad()
    }
    return totals
}

fun deriveInspectionEntriesFromRecord(items: MutableList<ServicePlanRecord>): MutableList<InspectionEntry> {
    val out = mutableListOf<InspectionEntry>()
    for (item in items) {
        val derived = newInspectionEntry(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun reconcileInspectionEntry(item: InspectionEntry): String = normalizePrimaryServicePlanAssignmentCode(item.code)
