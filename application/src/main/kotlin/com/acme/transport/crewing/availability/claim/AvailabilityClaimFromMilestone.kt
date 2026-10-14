package com.acme.transport.crewing.availability.claim

import com.acme.transport.crewing.swap.audit.SwapMilestone
import com.acme.transport.crewing.swap.audit.normalizeSwapMilestoneCode

fun summarizeSwapMilestonesForClaim(items: MutableList<SwapMilestone>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedTotal()
    }
    return totals
}

fun deriveAvailabilityClaimsFromMilestone(items: MutableList<SwapMilestone>): MutableList<AvailabilityClaim> {
    val out = mutableListOf<AvailabilityClaim>()
    for (item in items) {
        val derived = newAvailabilityClaim(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun reconcileAvailabilityClaim(item: AvailabilityClaim): String = normalizeSwapMilestoneCode(item.code)
