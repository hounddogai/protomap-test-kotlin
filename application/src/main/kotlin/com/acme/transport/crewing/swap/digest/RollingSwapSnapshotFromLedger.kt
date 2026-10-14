package com.acme.transport.crewing.swap.digest

import com.acme.transport.crewing.qualification.schedule.TieredQualificationLedger
import com.acme.transport.crewing.qualification.schedule.normalizeBatchedQualificationMilestoneCode

fun summarizeTieredQualificationLedgersForSnapshot(items: MutableList<TieredQualificationLedger>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveLoad()
    }
    return totals
}

fun deriveRollingSwapSnapshotsFromLedger(items: MutableList<TieredQualificationLedger>): MutableList<RollingSwapSnapshot> {
    val out = mutableListOf<RollingSwapSnapshot>()
    for (item in items) {
        val derived = newRollingSwapSnapshot(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun mergeRollingSwapSnapshot(item: RollingSwapSnapshot): String {
    return normalizeBatchedQualificationMilestoneCode(item.code)
}
