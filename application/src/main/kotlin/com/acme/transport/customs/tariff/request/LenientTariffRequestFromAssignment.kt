package com.acme.transport.customs.tariff.request

import com.acme.transport.customs.declaration.declarationsnapshot.SeasonalDeclarationAssignment
import com.acme.transport.customs.declaration.declarationsnapshot.globalDeclarationSnapshotBucket

fun summarizeSeasonalDeclarationAssignmentsForRequest(items: MutableList<SeasonalDeclarationAssignment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedTotal()
    }
    return totals
}

fun deriveLenientTariffRequestsFromAssignment(items: MutableList<SeasonalDeclarationAssignment>): MutableList<LenientTariffRequest> {
    val out = mutableListOf<LenientTariffRequest>()
    for (item in items) {
        val derived = newLenientTariffRequest(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun normalizeLenientTariffRequest(item: LenientTariffRequest): String {
    return globalDeclarationSnapshotBucket(item.adjustedValue())
}
