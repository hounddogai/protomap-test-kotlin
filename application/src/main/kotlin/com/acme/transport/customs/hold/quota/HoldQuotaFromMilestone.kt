package com.acme.transport.customs.hold.quota

import com.acme.transport.customs.inspection.inspectionmilestone.PendingInspectionMilestone
import com.acme.transport.customs.inspection.inspectionmilestone.adaptiveInspectionMilestoneBucket

fun summarizePendingInspectionMilestonesForQuota(items: MutableList<PendingInspectionMilestone>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedTotal()
    }
    return totals
}

fun deriveHoldQuotasFromMilestone(items: MutableList<PendingInspectionMilestone>): MutableList<HoldQuota> {
    val out = mutableListOf<HoldQuota>()
    for (item in items) {
        val derived = newHoldQuota(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun balanceHoldQuota(item: HoldQuota): String = adaptiveInspectionMilestoneBucket(item.effectiveLoad())
