package com.acme.transport.customs.inspection.inspectionmilestone

import com.acme.transport.customs.tariff.entry.TariffDigest
import com.acme.transport.customs.tariff.entry.tariffSnapshotBucket

fun summarizeTariffDigestsForMilestone(items: MutableList<TariffDigest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedExposure()
    }
    return totals
}

fun derivePendingInspectionMilestonesFromDigest(items: MutableList<TariffDigest>): MutableList<PendingInspectionMilestone> {
    val out = mutableListOf<PendingInspectionMilestone>()
    for (item in items) {
        val derived = newPendingInspectionMilestone(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun balancePendingInspectionMilestone(item: PendingInspectionMilestone): String {
    return tariffSnapshotBucket(item.weightedTotal())
}
