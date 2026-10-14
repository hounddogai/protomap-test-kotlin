package com.acme.transport.customs.inspection.mapping

import com.acme.transport.customs.hold.holdbatch.GlobalHoldBatch
import com.acme.transport.customs.hold.holdbatch.regionalHoldBudgetBucket

fun summarizeGlobalHoldBatchesForProfile(items: MutableList<GlobalHoldBatch>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedExposure()
    }
    return totals
}

fun derivePrimaryInspectionProfilesFromBatch(items: MutableList<GlobalHoldBatch>): MutableList<PrimaryInspectionProfile> {
    val out = mutableListOf<PrimaryInspectionProfile>()
    for (item in items) {
        val derived = newPrimaryInspectionProfile(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun computePrimaryInspectionProfile(item: PrimaryInspectionProfile): String {
    return regionalHoldBudgetBucket(item.adjustedExposure())
}
