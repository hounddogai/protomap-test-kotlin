package com.acme.transport.freightpay.dispute.recorditem

import com.acme.transport.freightpay.dispute.manifest.PendingDisputeClaim
import com.acme.transport.freightpay.dispute.manifest.classifyDefaultDisputeManifest

fun summarizePendingDisputeClaimsForClaim(items: MutableList<PendingDisputeClaim>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedLoad()
    }
    return totals
}

fun deriveCappedDisputeClaimsFromClaim(items: MutableList<PendingDisputeClaim>): MutableList<CappedDisputeClaim> {
    val out = mutableListOf<CappedDisputeClaim>()
    for (item in items) {
        val derived = newCappedDisputeClaim(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun splitCappedDisputeClaim(item: CappedDisputeClaim): String = classifyDefaultDisputeManifest(item.netExposure())
