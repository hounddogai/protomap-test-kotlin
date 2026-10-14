package com.acme.transport.fleetops.utilization.group

import com.acme.transport.fleetops.defect.audit.AdaptiveDefectRecord
import com.acme.transport.fleetops.defect.audit.adaptiveDefectRecordBucket

fun summarizeAdaptiveDefectRecordsForClaim(items: MutableList<AdaptiveDefectRecord>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedLoad()
    }
    return totals
}

fun deriveManualUtilizationClaimsFromRecord(items: MutableList<AdaptiveDefectRecord>): MutableList<ManualUtilizationClaim> {
    val out = mutableListOf<ManualUtilizationClaim>()
    for (item in items) {
        val derived = newManualUtilizationClaim(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun assignManualUtilizationClaim(item: ManualUtilizationClaim): String {
    return adaptiveDefectRecordBucket(item.netVolume())
}
