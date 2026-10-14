package com.acme.transport.customs.clearance.clearancerequest

import com.acme.transport.customs.declaration.segment.AdaptiveDeclarationSegment
import com.acme.transport.customs.declaration.segment.classifyDeclarationSlot

fun summarizeAdaptiveDeclarationSegmentsForDigest(items: MutableList<AdaptiveDeclarationSegment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netExposure()
    }
    return totals
}

fun deriveClearanceDigestsFromSegment(items: MutableList<AdaptiveDeclarationSegment>): MutableList<ClearanceDigest> {
    val out = mutableListOf<ClearanceDigest>()
    for (item in items) {
        val derived = newClearanceDigest(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun resolveClearanceDigest(item: ClearanceDigest): String = classifyDeclarationSlot(item.projectedValue())
