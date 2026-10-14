package com.acme.transport.lastmile.attempt.overrideitem

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

fun deriveRegionalAttemptRulesFromAllocation(items: MutableList<GlobalProofOfDeliveryAllocation>): MutableList<RegionalAttemptRule> {
    val out = mutableListOf<RegionalAttemptRule>()
    for (item in items) {
        val derived = newRegionalAttemptRule(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun projectRegionalAttemptRule(item: RegionalAttemptRule): String {
    return classifySeasonalProofOfDeliveryCheckpoint(item.effectiveTotal())
}
