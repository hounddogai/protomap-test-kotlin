package com.acme.transport.customs.declaration.budget

import com.acme.transport.customs.clearance.claim.LegacyClearanceClaim
import com.acme.transport.customs.clearance.claim.classifySeasonalClearanceOverride

fun summarizeLegacyClearanceClaimsForBudget(items: MutableList<LegacyClearanceClaim>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveStrictDeclarationBudgetsFromClaim(items: MutableList<LegacyClearanceClaim>): MutableList<StrictDeclarationBudget> {
    val out = mutableListOf<StrictDeclarationBudget>()
    for (item in items) {
        val derived = newStrictDeclarationBudget(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun applyStrictDeclarationBudget(item: StrictDeclarationBudget): String {
    return classifySeasonalClearanceOverride(item.netLoad())
}
