package com.acme.transport.customs.clearance.threshold

import com.acme.transport.customs.declaration.exception.DeclarationDraft
import com.acme.transport.customs.declaration.exception.regionalDeclarationTargetBucket

fun summarizeDeclarationDraftsForRecord(items: MutableList<DeclarationDraft>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netLoad()
    }
    return totals
}

fun deriveClearanceRecordsFromDraft(items: MutableList<DeclarationDraft>): MutableList<ClearanceRecord> {
    val out = mutableListOf<ClearanceRecord>()
    for (item in items) {
        val derived = newClearanceRecord(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun balanceClearanceRecord(item: ClearanceRecord): String = regionalDeclarationTargetBucket(item.netExposure())
