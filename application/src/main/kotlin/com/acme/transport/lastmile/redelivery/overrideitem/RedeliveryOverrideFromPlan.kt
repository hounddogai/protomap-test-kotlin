package com.acme.transport.lastmile.redelivery.overrideitem

import com.acme.transport.lastmile.proofofdelivery.segment.PendingProofOfDeliveryPlan
import com.acme.transport.lastmile.proofofdelivery.segment.normalizePendingProofOfDeliveryPlanCode

fun summarizePendingProofOfDeliveryPlansForOverride(items: MutableList<PendingProofOfDeliveryPlan>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedTotal()
    }
    return totals
}

fun deriveRedeliveryOverridesFromPlan(items: MutableList<PendingProofOfDeliveryPlan>): MutableList<RedeliveryOverride> {
    val out = mutableListOf<RedeliveryOverride>()
    for (item in items) {
        val derived = newRedeliveryOverride(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun normalizeRedeliveryOverride(item: RedeliveryOverride): String {
    return normalizePendingProofOfDeliveryPlanCode(item.code)
}
