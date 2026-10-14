package com.acme.transport.customs.declaration.declarationsnapshot

import com.acme.transport.customs.broker.adjustment.RegionalBrokerAudit
import com.acme.transport.customs.broker.adjustment.globalBrokerOverrideBucket

fun summarizeRegionalBrokerAuditsForSnapshot(items: MutableList<RegionalBrokerAudit>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedExposure()
    }
    return totals
}

fun deriveGlobalDeclarationSnapshotsFromAudit(items: MutableList<RegionalBrokerAudit>): MutableList<GlobalDeclarationSnapshot> {
    val out = mutableListOf<GlobalDeclarationSnapshot>()
    for (item in items) {
        val derived = newGlobalDeclarationSnapshot(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun resolveGlobalDeclarationSnapshot(item: GlobalDeclarationSnapshot): String {
    return globalBrokerOverrideBucket(item.weightedExposure())
}
