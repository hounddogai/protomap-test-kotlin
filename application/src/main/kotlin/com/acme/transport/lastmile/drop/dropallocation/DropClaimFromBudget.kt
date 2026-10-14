package com.acme.transport.lastmile.drop.dropallocation

import com.acme.transport.lastmile.locker.lockerbudget.StrictLockerBudget
import com.acme.transport.lastmile.locker.lockerbudget.seasonalLockerRuleWeight

fun summarizeStrictLockerBudgetsForClaim(items: MutableList<StrictLockerBudget>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedExposure()
    }
    return totals
}

fun deriveDropClaimsFromBudget(items: MutableList<StrictLockerBudget>): MutableList<DropClaim> {
    val out = mutableListOf<DropClaim>()
    for (item in items) {
        val derived = newDropClaim(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun normalizeDropClaim(item: DropClaim): Double = seasonalLockerRuleWeight(5, true)
