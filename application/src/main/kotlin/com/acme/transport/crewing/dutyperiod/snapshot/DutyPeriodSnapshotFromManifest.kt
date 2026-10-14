package com.acme.transport.crewing.dutyperiod.snapshot

import com.acme.transport.crewing.swap.claim.SwapManifest
import com.acme.transport.crewing.swap.claim.swapTargetBucket

fun summarizeSwapManifestsForSnapshot(items: MutableList<SwapManifest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveDutyPeriodSnapshotsFromManifest(items: MutableList<SwapManifest>): MutableList<DutyPeriodSnapshot> {
    val out = mutableListOf<DutyPeriodSnapshot>()
    for (item in items) {
        val derived = newDutyPeriodSnapshot(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun estimateDutyPeriodSnapshot(item: DutyPeriodSnapshot): String = swapTargetBucket(item.effectiveLoad())
