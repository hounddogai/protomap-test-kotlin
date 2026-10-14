package com.acme.transport.lastmile.redelivery.redeliveryclaim

import com.acme.transport.lastmile.proofofdelivery.allocation.GlobalProofOfDeliveryAllocation
import com.acme.transport.lastmile.proofofdelivery.allocation.classifySeasonalProofOfDeliveryCheckpoint

fun summarizeGlobalProofOfDeliveryAllocationsForRule(items: MutableList<GlobalProofOfDeliveryAllocation>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveRedeliveryRulesFromAllocation(items: MutableList<GlobalProofOfDeliveryAllocation>): MutableList<RedeliveryRule> {
    val out = mutableListOf<RedeliveryRule>()
    for (item in items) {
        val derived = newRedeliveryRule(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun computeRedeliveryRule(item: RedeliveryRule): String {
    return classifySeasonalProofOfDeliveryCheckpoint(item.projectedValue())
}
