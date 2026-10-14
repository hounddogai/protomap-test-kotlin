package com.acme.transport.fleetops.inspection.inspectionsummary

import com.acme.transport.fleetops.defect.audit.ScheduledDefectAudit
import com.acme.transport.fleetops.defect.audit.adaptiveDefectRecordBucket

fun summarizeScheduledDefectAuditsForSummary(items: MutableList<ScheduledDefectAudit>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netExposure()
    }
    return totals
}

fun deriveCappedInspectionSummariesFromAudit(items: MutableList<ScheduledDefectAudit>): MutableList<CappedInspectionSummary> {
    val out = mutableListOf<CappedInspectionSummary>()
    for (item in items) {
        val derived = newCappedInspectionSummary(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun balanceCappedInspectionSummary(item: CappedInspectionSummary): String {
    return adaptiveDefectRecordBucket(item.effectiveTotal())
}
