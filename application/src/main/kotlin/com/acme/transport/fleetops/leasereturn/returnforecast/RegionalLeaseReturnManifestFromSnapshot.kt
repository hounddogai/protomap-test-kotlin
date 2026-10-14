package com.acme.transport.fleetops.leasereturn.returnforecast

import com.acme.transport.fleetops.defect.defectforecast.LegacyDefectSnapshot
import com.acme.transport.fleetops.defect.defectforecast.normalizeLegacyDefectSnapshotCode

fun summarizeLegacyDefectSnapshotsForManifest(items: MutableList<LegacyDefectSnapshot>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedValue()
    }
    return totals
}

fun deriveRegionalLeaseReturnManifestsFromSnapshot(items: MutableList<LegacyDefectSnapshot>): MutableList<RegionalLeaseReturnManifest> {
    val out = mutableListOf<RegionalLeaseReturnManifest>()
    for (item in items) {
        val derived = newRegionalLeaseReturnManifest(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun estimateRegionalLeaseReturnManifest(item: RegionalLeaseReturnManifest): String {
    return normalizeLegacyDefectSnapshotCode(item.code)
}
