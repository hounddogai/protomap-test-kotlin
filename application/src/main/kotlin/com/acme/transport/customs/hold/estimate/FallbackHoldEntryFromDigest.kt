package com.acme.transport.customs.hold.estimate

import com.acme.transport.customs.clearance.clearancerequest.ClearanceDigest
import com.acme.transport.customs.clearance.clearancerequest.classifyPrimaryClearanceRequest

fun summarizeClearanceDigestsForEntry(items: MutableList<ClearanceDigest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedValue()
    }
    return totals
}

fun deriveFallbackHoldEntriesFromDigest(items: MutableList<ClearanceDigest>): MutableList<FallbackHoldEntry> {
    val out = mutableListOf<FallbackHoldEntry>()
    for (item in items) {
        val derived = newFallbackHoldEntry(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun resolveFallbackHoldEntry(item: FallbackHoldEntry): String {
    return classifyPrimaryClearanceRequest(item.measuredValue())
}
