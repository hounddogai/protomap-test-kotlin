package com.acme.transport.customs.inspection.inspectiondigest

import com.acme.transport.customs.declaration.assignment.TieredDeclarationGroup
import com.acme.transport.customs.declaration.assignment.seasonalDeclarationReviewBucket

fun summarizeTieredDeclarationGroupsForRequest(items: MutableList<TieredDeclarationGroup>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveFallbackInspectionRequestsFromGroup(items: MutableList<TieredDeclarationGroup>): MutableList<FallbackInspectionRequest> {
    val out = mutableListOf<FallbackInspectionRequest>()
    for (item in items) {
        val derived = newFallbackInspectionRequest(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun deriveFallbackInspectionRequest(item: FallbackInspectionRequest): String {
    return seasonalDeclarationReviewBucket(item.netLoad())
}
