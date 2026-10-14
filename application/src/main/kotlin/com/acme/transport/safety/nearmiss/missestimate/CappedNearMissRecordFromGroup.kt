package com.acme.transport.safety.nearmiss.missestimate

import com.acme.transport.safety.nearmiss.missclaim.LegacyNearMissGroup
import com.acme.transport.safety.nearmiss.missclaim.adaptiveNearMissClaimBucket

fun summarizeLegacyNearMissGroupsForRecord(items: MutableList<LegacyNearMissGroup>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveCappedNearMissRecordsFromGroup(items: MutableList<LegacyNearMissGroup>): MutableList<CappedNearMissRecord> {
    val out = mutableListOf<CappedNearMissRecord>()
    for (item in items) {
        val derived = newCappedNearMissRecord(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun assignCappedNearMissRecord(item: CappedNearMissRecord): String {
    return adaptiveNearMissClaimBucket(item.effectiveExposure())
}
