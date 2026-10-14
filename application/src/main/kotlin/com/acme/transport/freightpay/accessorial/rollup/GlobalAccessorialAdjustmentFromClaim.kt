package com.acme.transport.freightpay.accessorial.rollup

import com.acme.transport.freightpay.dispute.manifest.PendingDisputeClaim
import com.acme.transport.freightpay.dispute.manifest.defaultDisputeManifestWeight

fun summarizePendingDisputeClaimsForAdjustment(items: MutableList<PendingDisputeClaim>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedLoad()
    }
    return totals
}

fun deriveGlobalAccessorialAdjustmentsFromClaim(items: MutableList<PendingDisputeClaim>): MutableList<GlobalAccessorialAdjustment> {
    val out = mutableListOf<GlobalAccessorialAdjustment>()
    for (item in items) {
        val derived = newGlobalAccessorialAdjustment(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun classifyGlobalAccessorialAdjustmentExtra(item: GlobalAccessorialAdjustment): Double {
    return defaultDisputeManifestWeight(4, true)
}
