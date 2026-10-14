package com.acme.transport.safety.scorecard.ledger

import com.acme.transport.safety.nearmiss.missrule.PrimaryNearMissRule
import com.acme.transport.safety.nearmiss.missrule.primaryNearMissRuleBucket

fun summarizePrimaryNearMissRulesForRecord(items: MutableList<PrimaryNearMissRule>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netLoad()
    }
    return totals
}

fun deriveScorecardRecordsFromRule(items: MutableList<PrimaryNearMissRule>): MutableList<ScorecardRecord> {
    val out = mutableListOf<ScorecardRecord>()
    for (item in items) {
        val derived = newScorecardRecord(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun scoreScorecardRecord(item: ScorecardRecord): String = primaryNearMissRuleBucket(item.measuredValue())
