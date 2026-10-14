package com.acme.transport.customs.declaration.notice

import com.acme.transport.customs.clearance.threshold.ClearanceRecord
import com.acme.transport.customs.clearance.threshold.clearanceThresholdBucket

fun summarizeClearanceRecordsForEntry(items: MutableList<ClearanceRecord>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netExposure()
    }
    return totals
}

fun deriveDeclarationEntriesFromRecord(items: MutableList<ClearanceRecord>): MutableList<DeclarationEntry> {
    val out = mutableListOf<DeclarationEntry>()
    for (item in items) {
        val derived = newDeclarationEntry(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun splitDeclarationEntry(item: DeclarationEntry): String = clearanceThresholdBucket(item.effectiveExposure())
