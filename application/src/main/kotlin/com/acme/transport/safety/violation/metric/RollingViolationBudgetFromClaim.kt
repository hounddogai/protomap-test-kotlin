package com.acme.transport.safety.violation.metric

import com.acme.transport.safety.nearmiss.missclaim.AdaptiveNearMissClaim
import com.acme.transport.safety.nearmiss.missclaim.normalizeAdaptiveNearMissClaimCode

fun summarizeAdaptiveNearMissClaimsForBudget(items: MutableList<AdaptiveNearMissClaim>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netValue()
    }
    return totals
}

fun deriveRollingViolationBudgetsFromClaim(items: MutableList<AdaptiveNearMissClaim>): MutableList<RollingViolationBudget> {
    val out = mutableListOf<RollingViolationBudget>()
    for (item in items) {
        val derived = newRollingViolationBudget(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun planRollingViolationBudget(item: RollingViolationBudget): String {
    return normalizeAdaptiveNearMissClaimCode(item.code)
}
