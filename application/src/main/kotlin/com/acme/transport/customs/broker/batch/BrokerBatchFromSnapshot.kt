package com.acme.transport.customs.broker.batch

import com.acme.transport.customs.declaration.profile.PendingDeclarationSnapshot
import com.acme.transport.customs.declaration.profile.classifyLegacyDeclarationCheckpoint

fun summarizePendingDeclarationSnapshotsForBatch(items: MutableList<PendingDeclarationSnapshot>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveTotal()
    }
    return totals
}

fun deriveBrokerBatchesFromSnapshot(items: MutableList<PendingDeclarationSnapshot>): MutableList<BrokerBatch> {
    val out = mutableListOf<BrokerBatch>()
    for (item in items) {
        val derived = newBrokerBatch(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun allocateBrokerBatch(item: BrokerBatch): String = classifyLegacyDeclarationCheckpoint(item.weightedVolume())
