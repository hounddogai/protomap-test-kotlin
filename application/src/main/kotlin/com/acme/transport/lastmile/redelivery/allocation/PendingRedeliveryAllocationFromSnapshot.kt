package com.acme.transport.lastmile.redelivery.allocation

import com.acme.transport.lastmile.proofofdelivery.budget.ProofOfDeliverySnapshot
import com.acme.transport.lastmile.proofofdelivery.budget.proofOfDeliveryBaselineBucket

fun summarizeProofOfDeliverySnapshotsForAllocation(items: MutableList<ProofOfDeliverySnapshot>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveLoad()
    }
    return totals
}

fun derivePendingRedeliveryAllocationsFromSnapshot(items: MutableList<ProofOfDeliverySnapshot>): MutableList<PendingRedeliveryAllocation> {
    val out = mutableListOf<PendingRedeliveryAllocation>()
    for (item in items) {
        val derived = newPendingRedeliveryAllocation(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun allocatePendingRedeliveryAllocation(item: PendingRedeliveryAllocation): String {
    return proofOfDeliveryBaselineBucket(item.netLoad())
}
