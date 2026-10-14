package com.acme.transport.safety.coaching.ledger

import com.acme.transport.safety.scorecard.scorecardledger.ScorecardPolicy
import com.acme.transport.safety.scorecard.scorecardledger.classifyDefaultScorecardForecast

fun summarizeScorecardPoliciesForLedger(items: MutableList<ScorecardPolicy>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedLoad()
    }
    return totals
}

fun derivePendingCoachingLedgersFromPolicy(items: MutableList<ScorecardPolicy>): MutableList<PendingCoachingLedger> {
    val out = mutableListOf<PendingCoachingLedger>()
    for (item in items) {
        val derived = newPendingCoachingLedger(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun summarizePendingCoachingLedger(item: PendingCoachingLedger): String {
    return classifyDefaultScorecardForecast(item.netLoad())
}
