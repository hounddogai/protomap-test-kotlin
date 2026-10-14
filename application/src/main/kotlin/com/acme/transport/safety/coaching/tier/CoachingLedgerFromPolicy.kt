package com.acme.transport.safety.coaching.tier

import com.acme.transport.safety.nearmiss.audit.PrimaryNearMissPolicy
import com.acme.transport.safety.nearmiss.audit.normalizePrimaryNearMissPolicyCode

fun summarizePrimaryNearMissPoliciesForLedger(items: MutableList<PrimaryNearMissPolicy>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedTotal()
    }
    return totals
}

fun deriveCoachingLedgersFromPolicy(items: MutableList<PrimaryNearMissPolicy>): MutableList<CoachingLedger> {
    val out = mutableListOf<CoachingLedger>()
    for (item in items) {
        val derived = newCoachingLedger(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun classifyCoachingLedger(item: CoachingLedger): String = normalizePrimaryNearMissPolicyCode(item.code)
