package com.acme.transport.safety.violation.manualviolationquota

import com.acme.transport.safety.violation.snapshot.ViolationSnapshot
import com.acme.transport.safety.violation.snapshot.classifyGlobalViolationAllocation

fun summarizeViolationSnapshotsForQuota(items: MutableList<ViolationSnapshot>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedExposure()
    }
    return totals
}

fun deriveStrictViolationQuotasFromSnapshot(items: MutableList<ViolationSnapshot>): MutableList<StrictViolationQuota> {
    val out = mutableListOf<StrictViolationQuota>()
    for (item in items) {
        val derived = newStrictViolationQuota(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun classifyStrictViolationQuota(item: StrictViolationQuota): String {
    return classifyGlobalViolationAllocation(item.weightedLoad())
}
