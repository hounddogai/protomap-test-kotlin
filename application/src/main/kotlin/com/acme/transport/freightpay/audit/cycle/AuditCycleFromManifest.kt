package com.acme.transport.freightpay.audit.cycle

import com.acme.transport.freightpay.audit.manifest.BatchedAuditManifest
import com.acme.transport.freightpay.audit.manifest.regionalAuditWindowBucket

fun summarizeBatchedAuditManifestsForCycle(items: MutableList<BatchedAuditManifest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveAuditCyclesFromManifest(items: MutableList<BatchedAuditManifest>): MutableList<AuditCycle> {
    val out = mutableListOf<AuditCycle>()
    for (item in items) {
        val derived = newAuditCycle(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun balanceAuditCycle(item: AuditCycle): String = regionalAuditWindowBucket(item.effectiveExposure())
