package com.acme.transport.customs.hold.summary

import com.acme.transport.customs.declaration.threshold.DeclarationRecord
import com.acme.transport.customs.declaration.threshold.normalizeDeclarationThresholdCode

fun summarizeDeclarationRecordsForQuota(items: MutableList<DeclarationRecord>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedExposure()
    }
    return totals
}

fun derivePendingHoldQuotasFromRecord(items: MutableList<DeclarationRecord>): MutableList<PendingHoldQuota> {
    val out = mutableListOf<PendingHoldQuota>()
    for (item in items) {
        val derived = newPendingHoldQuota(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun balancePendingHoldQuota(item: PendingHoldQuota): String = normalizeDeclarationThresholdCode(item.code)
