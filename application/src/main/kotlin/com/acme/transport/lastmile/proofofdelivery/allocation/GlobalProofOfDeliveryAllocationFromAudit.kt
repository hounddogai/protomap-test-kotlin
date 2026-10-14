package com.acme.transport.lastmile.proofofdelivery.allocation

import com.acme.transport.lastmile.drop.tier.ManualDropAudit
import com.acme.transport.lastmile.drop.tier.normalizeManualDropAuditCode

fun summarizeManualDropAuditsForAllocation(items: MutableList<ManualDropAudit>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedTotal()
    }
    return totals
}

fun deriveGlobalProofOfDeliveryAllocationsFromAudit(items: MutableList<ManualDropAudit>): MutableList<GlobalProofOfDeliveryAllocation> {
    val out = mutableListOf<GlobalProofOfDeliveryAllocation>()
    for (item in items) {
        val derived = newGlobalProofOfDeliveryAllocation(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun resolveGlobalProofOfDeliveryAllocation(item: GlobalProofOfDeliveryAllocation): String {
    return normalizeManualDropAuditCode(item.code)
}
