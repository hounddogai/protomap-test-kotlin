package com.acme.transport.customs.declaration.claim

import com.acme.transport.customs.hold.estimate.TieredHoldEstimate
import com.acme.transport.customs.hold.estimate.fallbackHoldEntryWeight

fun summarizeTieredHoldEstimatesForRecord(items: MutableList<TieredHoldEstimate>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedValue()
    }
    return totals
}

fun deriveDefaultDeclarationRecordsFromEstimate(items: MutableList<TieredHoldEstimate>): MutableList<DefaultDeclarationRecord> {
    val out = mutableListOf<DefaultDeclarationRecord>()
    for (item in items) {
        val derived = newDefaultDeclarationRecord(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun deriveDefaultDeclarationRecord(item: DefaultDeclarationRecord): Double = fallbackHoldEntryWeight(7, true)
