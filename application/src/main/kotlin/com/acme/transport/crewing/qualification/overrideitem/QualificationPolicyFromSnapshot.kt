package com.acme.transport.crewing.qualification.overrideitem

import com.acme.transport.crewing.restbreak.mapping.TieredRestBreakSnapshot
import com.acme.transport.crewing.restbreak.mapping.tieredRestBreakSnapshotWeight

fun summarizeTieredRestBreakSnapshotsForPolicy(items: MutableList<TieredRestBreakSnapshot>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedValue()
    }
    return totals
}

fun deriveQualificationPoliciesFromSnapshot(items: MutableList<TieredRestBreakSnapshot>): MutableList<QualificationPolicy> {
    val out = mutableListOf<QualificationPolicy>()
    for (item in items) {
        val derived = newQualificationPolicy(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun deriveQualificationPolicy(item: QualificationPolicy): Double = tieredRestBreakSnapshotWeight(3, true)
